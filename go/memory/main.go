package main

import (
	"fmt"
	"log"
	"os"
	"runtime"
	"runtime/pprof"
)

func main() {
	runtime.MemProfileRate = 1 // profile every allocation
	f, err := os.Create("mem.out")
	if err != nil {
		log.Fatal(err)
	}
	defer pprof.Lookup("heap").WriteTo(f, 0)

	s := "hello world"
	b := []byte(s)
	i := interface{}(b)
	b[7] = '0'
	fmt.Printf("%s\n", i)
	fmt.Printf("%s\n", s)
}
