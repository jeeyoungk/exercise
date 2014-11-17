package server

import (
	"board"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"sync"
)

type service interface {
	Start() // called at most once.
	Stop()  // called at most once.
}

type ServerContext struct {
	gamesMutex  sync.RWMutex
	games       map[int]*GameContext // accessed by the mutex.
	gameCounter int                  // accessed by the mutex.
	stopEvent   chan interface{}
	running     *sync.WaitGroup
}

type GameContext struct {
	id    int
	board *board.Board
}

type GameEntity struct {
	Id    int    `json: id`
	Board string `json: string`
}

func (ge *GameEntity) FromContext(gc *GameContext) {
	ge.Id = gc.id
	ge.Board = gc.board.ToString()
}

func newServerContext(running *sync.WaitGroup) *ServerContext {
	return &ServerContext{
		games:     make(map[int]*GameContext),
		stopEvent: make(chan interface{}),
		running:   running,
	}
}

func newGameContext(id int) *GameContext {
	return &GameContext{
		id:    id,
		board: board.NewBoard(),
	}
}

// handlers.
func (sc *ServerContext) NewGameHandler(rw http.ResponseWriter, r *http.Request) {
	gc := sc.NewGameContext()
	response := GameEntity{}
	response.FromContext(gc)
	bytes, _ := json.Marshal(response)
	rw.Write(bytes)
}

func (sc *ServerContext) GetGameHandler(rw http.ResponseWriter, r *http.Request) {
	idStr := r.URL.Query().Get("id")
	id, err := strconv.Atoi(idStr)
	if err != nil {
		rw.WriteHeader(400)
		return
	}
	gc, ok := sc.GetGameContext(id)
	if !ok {
		rw.WriteHeader(400)
		return
	}
	response := GameEntity{}
	response.FromContext(gc)
	bytes, _ := json.Marshal(response)
	rw.Write(bytes)
}

func (sc *ServerContext) Start() {
	sc.running.Add(1)
	fmt.Println("Starting...")
	mux := http.NewServeMux()
	mux.HandleFunc("/game/", sc.GetGameHandler)
	mux.HandleFunc("/game/play", sc.GetGameHandler)
	mux.HandleFunc("/game/new", sc.NewGameHandler)
	s := http.Server{
		Addr:    ":8080",
		Handler: mux,
	}
	go func() {
		log.Fatal(s.ListenAndServe())
	}()
	go func() {
		defer sc.running.Done()
		for {
			select {
			case <-sc.stopEvent:
				return
			}
		}
	}()
}

func (sc *ServerContext) NewGameContext() *GameContext {
	sc.gamesMutex.Lock()
	defer sc.gamesMutex.Unlock()
	sc.gameCounter++
	gc := newGameContext(sc.gameCounter)
	sc.games[sc.gameCounter] = gc
	return gc
}

func (sc *ServerContext) GetGameContext(id int) (*GameContext, bool) {
	sc.gamesMutex.RLock()
	defer sc.gamesMutex.RUnlock()
	game, ok := sc.games[id]
	return game, ok
}

func (sc *ServerContext) Stop() {
	close(sc.stopEvent)
	fmt.Println("Stopping..")
}

func Start() {
	// TODO - make them into variables.
	var running sync.WaitGroup
	sc := newServerContext(&running)
	sc.Start()
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	go func() {
		for _ = range c {
			sc.Stop()
		}
	}()
	running.Wait()
}
