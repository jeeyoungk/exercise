package main

import (
	"bytes"
	"fmt"
)

const (
	edge_empty = iota
	edge_wall  = iota
	edge_taken = iota
)
const (
	up    = iota
	right = iota
	down  = iota
	left  = iota
)

type board struct {
	x      int
	y      int
	edge_h [][]int
	edge_v [][]int
}

func New(x, y int) *board {
	b := &board{x: x, y: y}
	b.edge_h = make([][]int, x+1)
	b.edge_v = make([][]int, x+1)
	for i := 0; i < x+1; i++ {
		b.edge_h[i] = make([]int, y+1)
		b.edge_v[i] = make([]int, y+1)
	}
	return b
}

func (b *board) getEdge(x, y, direction int) *int {
	switch direction {
	case up:
		return &b.edge_h[x+1][y]
	case down:
		return &b.edge_h[x][y]
	case left:
		return &b.edge_v[x][y]
	case right:
		return &b.edge_v[x][y+1]
	}
	return nil
}

func (b *board) markWall(x, y, direction int) bool {
	edge := b.getEdge(x, y, direction)
	if edge == nil {
		return false
	}
	if *edge != edge_empty {
		return false
	}
	*edge = edge_wall
	return true
}

func (b *board) printBoard() string {
	var buffer bytes.Buffer

	w := func(w string) {
		buffer.WriteString(w)
	}
	isEdge := func(x, y, direction int) bool {
		return *b.getEdge(x, y, direction) == edge_wall
	}
	w("█")
	for y := 0; y < b.y; y++ {
		if isEdge(b.x-1, y, up) {
			w("██")
		} else {
			w(" █")
		}
	}
	w("\n")
	for x := b.x - 1; x >= 0; x-- {
		if isEdge(x, 0, left) {
			w("█")
		} else {
			w(" ")
		}
		for y := 0; y < b.y; y++ {
			if isEdge(x, y, right) {
				w(" █")
			} else {
				w("  ")
			}
		}
		w("\n")
		w("█")
		for y := 0; y < b.y; y++ {
			if isEdge(x, y, down) {
				w("██")
			} else {
				w(" ")
				if isEdge(x, y, right) {
					w("█")
				} else {
					w(" ")
				}
			}
		}
		w("\n")
	}
	return buffer.String()
}

func main() {
	b := New(2, 2)
	b.markWall(0, 0, down)
	b.markWall(0, 0, left)
	b.markWall(0, 1, right)
	b.markWall(1, 0, left)
	b.markWall(1, 0, up)
	b.markWall(1, 1, right)
	fmt.Println(b.printBoard())
}
