# range median queries

import math
import random

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

def pow_2_rmq(array):
    """
    prep - O(n log n)
    exec - O(1)
    """
    N = len(array)
    P = int(math.floor(math.log(N, 2)))
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
        if start == end:
            return array[start]
        delta = end - start
        length = int(math.floor(math.log(delta, 2)))
        start_index = start
        end_index = end - (2 ** length - 1)
        cur_mins = mins[length]
        return min(cur_mins[start_index], cur_mins[end_index])
    return inner

def rmq_tester(rmq_maker):
    N = 16
    # test 1 - sequential
    rmq = rmq_maker(range(N))
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == min(i, j)
    # test 2 - reverse sequential
    rmq = rmq_maker(list(reversed(range(N))))
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == N - max(i, j) - 1
    # test 3 - 1 minimum
    test = [1] * N
    test[10] = 0
    rmq = rmq_maker(test)
    for i in range(N):
        for j in range(i, N):
            assert rmq(i, j) == (0 if (i <= 10 <= j) else 1)
    # test 4 - random test
    test = range(N)
    random.shuffle(test)
    rmq = rmq_maker(test)
    canonical = naive_rmq(test)
    for i in xrange(N):
        for j in xrange(i, N):
            assert rmq(i, j) == canonical(i, j)
rmq_tester(pow_2_rmq)
