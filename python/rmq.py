# range median queries

import math
import random

def log2(v):
    return int(math.log(v, 2))

def naive_rmq(array):
    """
    prep - O(1)
    exec - O(n)
    """
    def inner(start, end):
        v = array[start]
        for i in xrange(start+1, end+1):
            v = min(v, array[i])
        return v
    return inner

def rmq_by_block(array):
    N = len(array)
    block_width = log2(N)
    num_blocks = N / block_width
    if num_blocks * block_width < N: num_blocks += 1
    min_blocks = [None] * num_blocks
    for i in xrange(N):
        block_idx = i / block_width
        if min_blocks[block_idx] is None or min_blocks[block_idx] > array[i]:
            min_blocks[block_idx] = array[i]
    
    inner_rmq = rmq_pow_2(min_blocks)

    def inner(start, end):
        start_block = start / block_width
        end_block = end / block_width
        if start_block == end_block:
            return min(array[start:end + 1])
        # O(log(N))
        start_min = min(array[start:(start_block + 1) * block_width])
        end_min = min(array[end_block * block_width:end + 1])
        if start_block + 1 <= end_block - 1:
            # O(1)
            block_min = inner_rmq(start_block + 1, end_block - 1)
            return min(start_min, block_min, end_min)
        return min(start_min, end_min)
    return inner

def rmq_pow_2(array):
    """
    prep - O(n log n)
    exec - O(1)
    """
    N = len(array)
    P = log2(N)
    width = 1
    last_row = array
    mins = [array]
    for power in xrange(P):
        new_mins = []
        for i in xrange(len(last_row) - width):
            min_value = min(last_row[i], last_row[i + width])
            new_mins.append(min_value)
        mins.append(new_mins)
        last_row = new_mins
        width *= 2 
    def inner(start, end):
        if start == end: return array[start]
        delta = end - start
        length = log2(delta)
        end_index = end - (2 ** length - 1)
        cur_mins = mins[length]
        return min(cur_mins[start], cur_mins[end_index])
    return inner

def rmq_tester_1(rmq_maker, N):
    # test 1 - sequential
    rmq = rmq_maker(range(N))
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == min(i, j)

def rmq_tester_2(rmq_maker, N):
    # test 2 - reverse sequential
    rmq = rmq_maker(list(reversed(range(N))))
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == N - max(i, j) - 1

def rmq_tester_3(rmq_maker, N):
    # test 3 - 1 minimum
    test = [1] * N
    test[10] = 0
    rmq = rmq_maker(test)
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == (0 if (i <= 10 <= j) else 1)

def rmq_tester_4(rmq_maker, N):
    # test 4 - random test
    test = range(N)
    random.shuffle(test)
    rmq = rmq_maker(test)
    canonical = naive_rmq(test)
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == canonical(i, j)

def rmq_tester(rmq_maker):
    for N in (16, 32, 64):
        rmq_tester_1(rmq_maker, N)
        rmq_tester_2(rmq_maker, N)
        rmq_tester_3(rmq_maker, N)
        rmq_tester_4(rmq_maker, N)

rmq_tester(naive_rmq)
rmq_tester(rmq_pow_2)
rmq_tester(rmq_by_block)
