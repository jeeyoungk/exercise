package main

import "math"
import "math/rand"
import "testing"

func TestMatrix_1(t *testing.T) {
	matrix := [][]float64{
		[]float64{2, 0, 0, 2},
		[]float64{0, 4, 0, 3},
		[]float64{0, 0, 3, 5},
	}
	Gaussian(matrix, 3, 3)

	if !approx(matrix[0][3], 1) {
		t.Error(matrix)
	}
	if !approx(matrix[1][3], 3.0/4) {
		t.Error(matrix)
	}
	if !approx(matrix[2][3], 5.0/3) {
		t.Error(matrix)
	}
}

func TestMatrix_2(t *testing.T) {
	matrix := [][]float64{
		[]float64{1, 0, 0, 1},
		[]float64{0, 0, 1, 1},
		[]float64{0, 1, 0, 1},
	}
	Gaussian(matrix, 3, 3)

	if matrix[0][3] != 1 {
		t.Error(matrix)
	}
	if matrix[1][3] != 1 {
		t.Error(matrix)
	}
	if matrix[2][3] != 1 {
		t.Error(matrix)
	}
}

func TestMatrix_3(t *testing.T) {
	matrix := [][]float64{
		[]float64{2, 1, 0, 2},
		[]float64{0, 4, 0, 3},
		[]float64{0, 1, 3, 5},
	}
	Gaussian(matrix, 3, 3)

	if !approx(matrix[0][3], 5.0/8) {
		t.Error(matrix)
	}
	if !approx(matrix[1][3], 3.0/4) {
		t.Error(matrix)
	}
	if !approx(matrix[2][3], 1.4166) {
		t.Error(matrix)
	}
}

func TestMatrix_4(t *testing.T) {
	matrix := [][]float64{
		[]float64{1, 0, 2, 3},
		[]float64{2, 0, 4, 6},
		[]float64{0, 1, 0, 1},
	}
	Gaussian(matrix, 3, 3)

	if matrix[0][3] != 3 {
		t.Error(matrix)
	}
	if matrix[1][3] != 1 {
		t.Error(matrix)
	}
	if matrix[2][3] != 0 {
		t.Error(matrix)
	}
}

func TestDijkstraTest_1(t *testing.T) {
	points := []Point{
		Point{0, 0, 1},
		Point{10, 10, 2},
	}
	result := dijkstra(points)
	if result != 14.142135623730951 {
		t.Error("Actual: ", result)
	}
}

func TestDijkstraTest_2(t *testing.T) {
	points := []Point{
		Point{0, 0, 1},
		Point{0, 10, 2},
		Point{10, 10, 2},
		Point{10, 20, 3},
	}
	result := dijkstra(points)
	if result != 20 {
		t.Error("Actual: ", result)
	}
}

func TestDijkstraTest_3(t *testing.T) {
	points := []Point{
		Point{0, 0, 1},
		Point{0, 10, 2},
		Point{10, 10, 2},
		Point{10, 20, 3},
		Point{100, 100, 3},
		Point{100, 200, 3},
		Point{100, 149, 4},
	}
	result := dijkstra(points)
	if result != 69 {
		t.Error("Actual: ", result)
	}
}

func TestArraySlice(t *testing.T) {
	// testing the functionality of array and slices in go.
	var array [4]int // this is an array.
	array[0] = 4
	if array[0] != 4 {
		t.Error()
	}
	if array[1] != 0 {
		t.Error("Arrays in go are zero'ed out")
	}
	if len(array) != 4 {
		t.Error()
	}
	if cap(array) != 4 {
		t.Error()
	}

	var arrayCopy = array
	if arrayCopy[0] != 4 {
		t.Error()
	}
	arrayCopy[1] = 3
	if array[1] != 0 {
		t.Error("Arrays are copied, not referenced")
	}

	var slice []int
	slice = []int{1, 2, 3}
	if slice[0] != 1 {
		t.Error()
	}
	var sliceCopy = slice
	if sliceCopy[0] != 1 {
		t.Error()
	}

	sliceCopy[0] = 100
	if slice[0] != 100 {
		t.Error("Slices are referenced. They share the same internal state")
	}

	if len(slice) != 3 {
		t.Error()
	}
	if cap(slice) != 3 {
		t.Error()
	}

	var largeSlice []int = make([]int, 10, 30)
	if len(largeSlice) != 10 {
		t.Error()
	}
	if cap(largeSlice) != 30 {
		t.Error()
	}
	var appended = append(largeSlice, 5)
	if len(appended) != 11 {
		t.Error()
	}
	if cap(appended) != 30 {
		t.Error()
	}
	var secondAppended = append(largeSlice, 7)
	if len(secondAppended) != 11 {
		t.Error()
	}
	if cap(secondAppended) != 30 {
		t.Error()
	}
	// i can just increase the size of the slice to its capacity.
	largeSlice = largeSlice[:30]
	if largeSlice[10] != 7 {
		t.Error()
	}
	if appended[10] != 7 {
		t.Error()
	}
}

func TestBalanced(t *testing.T) {
	goodInput := []string{
		"()",
		"",
	}

	badInput := []string{
		"(()",
		"(()))(",
	}

	for _, good := range goodInput {
		if !is_balanced(good) {
			t.Error("Failed is_balanced", good)
		}
	}
	for _, bad := range badInput {
		if is_balanced(bad) {
			t.Error("Failed is_balanced", bad)
		}
	}
}
func TestSum(t *testing.T) {
	if sum(&[]int{}) != 0 {
		t.Error("Unexpected result.")
	}
	if sum(&[]int{10, 20}) != 30 {
		t.Error("Unexpected result.")
	}
	if sum(&[]int{10, 20, -31}) != -1 {
		t.Error("Unexpected result.")
	}
}

func TestMax(t *testing.T) {
	if max(&[]int{}) != math.MinInt32 {
		t.Error()
	}
	if max(&[]int{10, 20}) != 20 {
		t.Error()
	}
	if max(&[]int{10, 30, 20}) != 30 {
		t.Error()
	}
}

func TestSort_1(t *testing.T) {
	testdata := [][]int{
		[]int{1, 3, 2},
		[]int{1, 100, 2, 5, 4, 78, 24, 3, 6},
		[]int{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
		[]int{10, 9, 8, 7, 6, 5, 4, 3, 2, 1},
	}

	for _, ary := range testdata {
		mysort(ary)
		assertSorted(t, &ary)
	}
}

func TestSort_2(t *testing.T) {
  N := 10000000
	testArray := make([]int, N, N)
  rand.Seed(42)
  for i := 0; i < N; i++ {
    testArray[i] = rand.Int()
  }

  dist_merge_sort(testArray)
  assertSorted(t, &testArray)
}

func TestMaxStack(t *testing.T) {
	testdata1 := []Interval{
		Interval{0, 5},
		Interval{1, 6},
		Interval{2, 7},
	}
	testdata2 := []Interval{
		Interval{0, 5},
		Interval{5, 10},
	}
	testdata3 := []Interval{}
	testdata4 := []Interval{
		Interval{0, 4},
		Interval{6, 10},
	}
	if maxstack(testdata1) != 3 {
		t.Error()
	}
	if maxstack(testdata2) != 2 {
		t.Error()
	}
	if maxstack(testdata3) != 0 {
		t.Error()
	}
	if maxstack(testdata4) != 1 {
		t.Error()
	}
}

func assertSorted(t *testing.T, ary *[]int) {
	for i := 0; i < len(*ary)-1; i++ {
		if (*ary)[i] > (*ary)[i+1] {
			t.Error("Array is not sorted. Indexes:", i, i+1)
		}
	}
}

func approx(lhs float64, rhs float64) bool {
	return math.Abs(lhs-rhs) < 0.001
}
