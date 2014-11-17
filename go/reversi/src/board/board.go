package board

import (
	"strings"
)

var xdirections = []int{-1, -1, 0, 1, 1, 1, 0, -1}
var ydirections = []int{0, 1, 1, 1, 0, -1, -1, -1}

const N = 8

const Empty = 0
const Black = 1
const White = 2

type Board struct {
	pos  [N][N]int // status of the board.
	turn int       // current turn.
	// TODO - event stream.
}

type Event struct {
	// all hidden.
}

// Constructor
func NewBoard() *Board {
	b := &Board{
		turn: Black,
	}
	// initial position of the game.
	b.pos[3][3] = Black
	b.pos[4][4] = Black
	b.pos[4][3] = White
	b.pos[3][4] = White
	return b
}

func (b *Board) Finished() bool {
	return false
}

func (b *Board) Score(color int) int {
	sum := 0
	for _, row := range b.pos {
		for _, pos := range row {
			if pos == color {
				sum++
			}
		}
	}
	return sum
}

func (b *Board) Turn() int {
	return b.turn
}

func (b *Board) ToString() string {
	rows := make([]string, N, N)
	row := make([]string, N, N)
	for rowIdx, posrow := range b.pos {
		for colIdx, colValue := range posrow {
			switch colValue {
			case Empty:
				row[colIdx] = "."
			case Black:
				row[colIdx] = "X"
			case White:
				row[colIdx] = "O"
			}
		}
		rows[rowIdx] = strings.Join(row, "")
	}
	return strings.Join(rows, "\n")
}

// returns true if the playing has succeeded.
func (b *Board) CanPlay(color int, row int, col int) bool {
	if color != b.turn {
		return false
	}
	if !(in_bound(row, col)) {
		return false
	}
	if b.pos[row][col] != 0 {
		return false
	}
	// see if i can flip any elements by doing this.
	found := false
	for direction := 0; direction < 8 && !found; direction++ {
		xdirection := xdirections[direction]
		ydirection := ydirections[direction]
		for moves := 1; !found; moves++ {
			currow := row + xdirection*moves
			curcol := col + ydirection*moves
			if !in_bound(currow, curcol) {
				break // went outside the board.
			}
			curval := b.pos[currow][curcol]
			if curval == Empty {
				break
			} else if curval == color {
				if moves != 1 {
					found = true
				}
				break
			}
		}
	}
	return found
}

func (b *Board) Play(color int, row int, col int) bool {
	if !b.CanPlay(color, row, col) {
		return false
	}
	// modify the board
	for direction := 0; direction < 8; direction++ {
		xdirection := xdirections[direction]
		ydirection := ydirections[direction]
		for moves := 1; ; moves++ {
			currow := row + xdirection*moves
			curcol := col + ydirection*moves
			if !in_bound(currow, curcol) {
				break // went outside the board.
			}
			curval := b.pos[currow][curcol]
			if curval == Empty {
				break // found Empty.
			} else if curval == color {
				for steps := 1; steps < moves; steps++ {
					currow := row + xdirection*steps
					curcol := col + ydirection*steps
					b.pos[currow][curcol] = color
				}
				break
			}
		}
	}
	if b.turn == Black {
		b.turn = White
	} else {
		b.turn = Black
	}
	b.pos[row][col] = color
	return true
}

// utility functions
func in_bound(row int, col int) bool {
	return row >= 0 && col >= 0 && row < N && col < N
}
