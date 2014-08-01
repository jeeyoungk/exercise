package main

import "fmt"
import "time"

func main() {
	runGoRoutines()
}

func ProducerActor(itemChannel chan int, doneChannel chan int, items int) {
	for i := 0; i < items; i++ {
		itemChannel <- i
	}
	fmt.Println("Finished production")
	doneChannel <- 0
}

func ConsumerActor(itemChannel chan int, doneChannel chan int, id int) {
	for i := 0; true; i++ {
		if i%100 == 0 {
			fmt.Printf("[consumer %d] consumed %d.\n", id, i)
		}
		time.Sleep(10 * time.Millisecond)
		<-itemChannel
		doneChannel <- 1
	}
}

func runGoRoutines() {
	itemChannel := make(chan int, 100)
	doneChannel := make(chan int)
	const PRODUCERS = 5
	const CONSUMERS = 3
	const N = 1000

	fmt.Printf("Starting. C=%d P=%d, N=%d\n", CONSUMERS, PRODUCERS, N)
	for i := 0; i < PRODUCERS; i++ {
		go ProducerActor(itemChannel, doneChannel, N)
	}

	for i := 0; i < CONSUMERS; i++ {
		go ConsumerActor(itemChannel, doneChannel, i)
	}

	result := 0
	for result < N*PRODUCERS {
		result += <-doneChannel
	}
	fmt.Println("Finished", result)
}
