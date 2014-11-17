package main

import (
	"board"
	"fmt"
	"server"
)

func main() {
	server.Start()
}

func unused() {
	b := board.NewBoard()
	for {
		fmt.Println(b.ToString())
		fmt.Printf("It is %d's turn.\n", b.Turn())
		played := false
		for !played {
			row := -1
			col := -1
			var err error = nil
			for err != nil || row == -1 {
				fmt.Println("row: ")
				_, err = fmt.Scanf("%d", &row)
			}
			for err != nil || col == -1 {
				fmt.Println("col: ")
				_, err = fmt.Scanf("%d", &col)
			}
			if !b.CanPlay(b.Turn(), row, col) {
				fmt.Println("You can't play there. try again!")
			} else {
				b.Play(b.Turn(), row, col)
				played = true
			}
		}
	}
}
