// bloom filter implementation in go.
package main

import (
	"encoding/binary"
	"fmt"
	"hash/fnv"
	"math"
	"math/rand"
)

type Bloom struct {
	size   uint     // size of bloom filter, for testing.
	filter uint64   // accumulated bit mask so far.
	hashes []uint64 // different seeds to FNV hash function.
}

func (bloom Bloom) hashKey(elem uint64) uint64 {
	result := uint64(0)
	for _, initialValue := range bloom.hashes {
		buffer := make([]byte, 24, 24)
		binary.PutUvarint(buffer, initialValue)
		binary.PutUvarint(buffer[8:], elem)
		hash := fnv.New32()
		hash.Write(buffer)
		hashValue := hash.Sum32()
		indexValue := hashValue % uint32(bloom.size)
		result |= 1 << indexValue
	}
	return result
}

func New(size uint, hashes []uint64) Bloom {
	return Bloom{size: size, filter: 0, hashes: hashes}
}

func (bloom Bloom) Add(elem uint64) Bloom {
	bloom.filter |= bloom.hashKey(elem)
	return bloom
}

func (bloom Bloom) MaybeContains(elem uint64) bool {
	hash := bloom.hashKey(elem)
	return (bloom.filter & hash) == hash
}

func TestBloomFilter(r *rand.Rand, size, numAdd, numSample, numHash uint) {
	falsePositive := 0
	falseNegative := 0 // should not occur!
	truePositive := 0
	trueNegative := 0

	iterations := 10000
	for iteration := 0; iteration < iterations; iteration++ {
		hashes := make([]uint64, numHash, numHash)
		for i := uint(0); i < numHash; i++ {
			hashes[i] = uint64(r.Uint32())
		}
		bloom := New(size, hashes)
		realset := make(map[uint64]bool)
		generate := func() uint64 {
			return uint64(r.Int63())
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
	fmt.Printf(" %12d %12d %12d | %02.4f\n", size, numAdd, numHash,
		float32(falsePositive)/float32(numSample)*100/float32(iterations),
	)
}

func main() {
	ln2 := 0.69314718056
	fmt.Printf(" %12s %12s %12s | %s\n", "size", "# elements", "# hashes", "false +")
	r := rand.New(rand.NewSource(1))
	sample_size := uint(10)

	for _, size := range []uint{64} {
		for _, num_elements := range []uint{2, 4, 8, 16, 32, 64, 128} {
			raw := float64(size) / float64(num_elements) * ln2
			num_hashes := uint(0)
			if (raw - math.Floor(raw)) > 0.5 {
				num_hashes = uint(math.Ceil(raw))
			} else {
				num_hashes = uint(math.Floor(raw))
			}
			if num_hashes == 0 {
				num_hashes = 1
			}
			fmt.Println("")
			if num_hashes > 1 {
				TestBloomFilter(r, size, num_elements, sample_size, num_hashes-1)
			}
			TestBloomFilter(r, size, num_elements, sample_size, num_hashes)
			TestBloomFilter(r, size, num_elements, sample_size, num_hashes+1)
		}
	}
	fmt.Println("")
	return
	for _, size := range []uint{4, 8, 16, 32, 64} {
		for _, num_hashes := range []uint{1, 2, 3, 4} {
			for _, num_elements := range []uint{1, 10, 100} {
				TestBloomFilter(r, size, num_elements, sample_size, num_hashes)
			}
		}
	}
}
