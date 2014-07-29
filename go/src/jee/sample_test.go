package main

import "math"
import "testing"

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

func TestSort(t *testing.T) {
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
