// arrays are copied by values, slices are copied by references.
package main

import "fmt"

func main() {
	array := [1]int{1}
	slice := make([]int, 1, 1)
	arrayCopy := array
	sliceCopy := slice

	arrayCopy[0] = 2
	slice[0] = 2

	fmt.Printf("array is %d\n", array[0])
	fmt.Printf("arrayCopy is %d\n", arrayCopy[0])
	fmt.Printf("slice is %d\n", slice[0])
	fmt.Printf("sliceCopy is %d\n", sliceCopy[0])
}
