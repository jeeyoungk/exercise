// bloom filter implementation in go.
package main

import (
	"fmt"
	"math/rand"
)

type Bloom struct {
	size   uint // size of bloom filter, for testing.
	filter uint // accumulated bit mask so far.
}

func New(size uint) Bloom {
	return Bloom{size: size, filter: 0}
}

func (bloom Bloom) Add(elem uint) Bloom {
	hash := elem & (1<<bloom.size - 1)
	bloom.filter |= hash
	return bloom
}

func (bloom Bloom) MaybeContains(elem uint) bool {
	hash := elem & (1<<bloom.size - 1)
	return (bloom.filter & hash) == hash
}

func TestBloomFilter(r *rand.Rand, size, numAdd, numSample, iterations uint) {
	falsePositive := 0
	falseNegative := 0 // should not occur!
	truePositive := 0
	trueNegative := 0
	for iteration := uint(0); iteration < iterations; iteration++ {
		bloom := New(size)
		realset := make(map[uint]bool)
		generate := func() uint {
			return uint(r.Intn(1<<size - 1))
		}
		for i := uint(0); i < numAdd; i++ {
			generated := generate()
			bloom = bloom.Add(generated)
			realset[generated] = true
		}
		for i := uint(0); i < numSample; i++ {
			generated := generate()
			bloomResult := bloom.MaybeContains(generated)
			_, realResult := realset[generated]
			if bloomResult && realResult {
				truePositive++
			} else if !bloomResult && !realResult {
				trueNegative++
			} else if bloomResult && !realResult {
				falsePositive++
			} else if !bloomResult && realResult {
				falseNegative++
			}
		}
	}
	fmt.Println("Experiment:")
	fmt.Printf(" size       : %d\n", size)
	fmt.Printf(" additions  : %d\n", numAdd)
	fmt.Printf(" samples    : %d\n", numSample)
	fmt.Printf(" iterations : %d\n", iterations)
	fmt.Printf("  true positive  : %.2f\n", float32(truePositive)/float32(numSample)*100/float32(iterations))
	fmt.Printf("  true negative  : %.2f\n", float32(trueNegative)/float32(numSample)*100/float32(iterations))
	fmt.Printf("  false positive : %.2f\n", float32(falsePositive)/float32(numSample)*100/float32(iterations))
}

func main() {
	r := rand.New(rand.NewSource(0))
	TestBloomFilter(r, 32, 1, 10000, 100)
	TestBloomFilter(r, 4, 1, 1000, 100)
	TestBloomFilter(r, 5, 1, 1000, 100)
	TestBloomFilter(r, 6, 1, 1000, 100)
	TestBloomFilter(r, 7, 1, 1000, 100)
	TestBloomFilter(r, 8, 1, 1000, 100)
}
