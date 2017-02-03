#!/usr/bin/env bash
SIZE=1000000
echo "Setting up"
time pypy rmq.py --algorithm pow2      --run 1 --size $SIZE
time pypy rmq.py --algorithm subblocks --run 1 --size $SIZE
time pypy rmq.py --algorithm blocks    --run 1 --size $SIZE

echo "Runs - pypy"
time pypy rmq.py --algorithm pow2      --run 100000 --size $SIZE
time pypy rmq.py --algorithm subblocks --run 100000 --size $SIZE
time pypy rmq.py --algorithm blocks    --run 100000 --size $SIZE

echo "Runs - cpython 2"
time python2 rmq.py --algorithm pow2      --run 100000 --size $SIZE
time python2 rmq.py --algorithm subblocks --run 100000 --size $SIZE
time python2 rmq.py --algorithm blocks    --run 100000 --size $SIZE

echo "Runs - cpython 3"
time python3 rmq.py --algorithm pow2      --run 100000 --size $SIZE
time python3 rmq.py --algorithm subblocks --run 100000 --size $SIZE
time python3 rmq.py --algorithm blocks    --run 100000 --size $SIZE
