#!/usr/bin/env bash
SIZE=1000000
echo "Setting up"
time pypy rmq.py --algorithm pow2      --run 1 --size $SIZE
time pypy rmq.py --algorithm subblocks --run 1 --size $SIZE
time pypy rmq.py --algorithm blocks    --run 1 --size $SIZE

echo "Runs"
time pypy rmq.py --algorithm pow2      --run 1000000 --size $SIZE
time pypy rmq.py --algorithm subblocks --run 1000000 --size $SIZE
time pypy rmq.py --algorithm blocks    --run 1000000 --size $SIZE
