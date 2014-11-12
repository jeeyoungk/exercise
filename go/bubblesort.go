package main

import "math"

// sums up the given array.
func sum(a *[]int) (sum int) {
	for _, v := range *a {
		sum += v
	}
	return
}

// find a max element in the given array.
func max(a *[]int) (max int) {
	max = math.MinInt32
	for _, v := range *a {
		if v > max {
			max = v
		}
	}
	return
}

// sort the given array via bubble sort.
func mysort(a []int) {
	for i := 1; i < len(a); i++ {
		for j := 0; j < len(a)-i; j++ {
			if a[j] > a[j+1] {
				tmp := a[j]
				a[j] = a[j+1]
				a[j+1] = tmp
			}
		}
	}
}

// distributed merge sort.
func dist_merge_sort(a []int) {
	dist_merge_sort_impl(a, 0, len(a), 0)
}

func dist_merge_sort_impl(a []int, start int, end int, depth int) {
	length := end - start
	if length <= 1 {
		return
	}
	if length <= 32 {
		mysort(a[start:end])
	}
	mid := (start + end) / 2
	done := make(chan bool, 2)
	left := func() {
		dist_merge_sort_impl(a, start, mid, depth+1)
		done <- true
	}
	right := func() {
		dist_merge_sort_impl(a, mid, end, depth+1)
		done <- true
	}
	if depth == 1 {
		go left()
		go right()
	} else {
		left()
		right()
	}
	// wait for the distribution to be done.
	<-done
	<-done
	merge(a, start, mid, end)
}

func merge(a []int, start int, mid int, end int) {
	// merges the two adjacent sorted sublist into one.
	// [start, mid), [mid, end) (two sorted sublist)
	// becomes [start, end) (one sorted sublist)
	// where it is sorted.
	leftIdx := start
	rightIdx := mid
	result := make([]int, 0, end-start)

	for leftIdx < mid && rightIdx < end {
		left := a[leftIdx]
		right := a[rightIdx]
		if left <= right {
			result = append(result, left)
			leftIdx++
		} else {
			result = append(result, right)
			rightIdx++
		}
	}
	for leftIdx < mid {
		result = append(result, a[leftIdx])
		leftIdx++
	}
	for rightIdx < end {
		result = append(result, a[rightIdx])
		rightIdx++
	}
	copy(a[start:end], result)
}

type Interval struct {
	start int
	end   int
}

type IntervalVector []Interval

func (v IntervalVector) Len() int {
	return len(v)
}

func (v IntervalVector) Swap(i, j int) {
	v[i], v[j] = v[j], v[i]
}

func (v IntervalVector) Less(i, j int) bool {
	if v[i].start < v[j].start {
		return true
	}
	if v[i].start > v[j].start {
		return false
	}
	return v[i].end < v[j].start
}

func maxstack(v IntervalVector) (max int) {
	startmap := make(map[int]int)
	endmap := make(map[int]int)
	keys := []int{}
	for _, interval := range v {
		startmap[interval.start] = startmap[interval.start] + 1
		endmap[interval.end] = endmap[interval.end] + 1
		keys = append(keys, interval.start, interval.end)
	}
	mysort(keys)
	curstack := 0
	for i := 0; i < len(keys); i++ {
		if i < len(keys)-1 && keys[i] == keys[i+1] {
			continue
		}
		curstack += startmap[keys[i]]
		if max < curstack {
			max = curstack
		}
		curstack -= endmap[keys[i]]
	}
	return
}
