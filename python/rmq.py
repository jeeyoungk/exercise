# range median queries
import math
import random
import argparse
try:
    import six
    range = six.moves.range
except ImportError:
    range = xrange

# Helper Functions
def log2(v):
    return int(math.log(v, 2))

def min_index_for_array(ary):
    def min_index(*args):
        idx = None
        for arg in args:
            if idx is None or ary[idx] > ary[arg]:
                idx = arg
        return idx
    return min_index

def pairwise_min_index_for_array(ary):
    def min_index(i, j):
        if ary[i] < ary[j]:
            return i
        return j
    return min_index

class Node(object):
    __slots__ = ('v', 'left', 'right')
    def __init__(self, v):
        self.v = v
        self.left = self.right = None

    def inorder(self):
        if self.left:
            for v in self.left.inorder():
                yield v
        yield self.v
        if self.right:
            for v in self.right.inorder():
                yield v

    def _signature(self, v):
        v = v * 2
        if self.left: v = self.left._signature(v + 1)
        v = v * 2
        if self.right: v = self.right._signature(v + 1)
        return v

    def signature(self):
        return self._signature(1) / 4

def cartesian_tree(ary):
    stack = []
    for v in ary:
        node = Node(v)
        # stack invariants:
        # left to right order.
        # higher on stack = higher value
        while stack and stack[-1].v >= node.v:
            if len(stack) == 1 or stack[-2].v < node.v:
                left = stack.pop()
                node.left = left
            else:
                right = stack.pop()
                stack[-1].right = right
        stack.append(node)
    while len(stack) > 1:
        right = stack.pop()
        stack[-1].right = right
    node = stack[0]
    # assert ary == list(node.inorder()) # ensures that the construction is correct.
    return node

def naive_rmq(ary):
    """
    prep - O(1)
    exec - O(n)
    """
    def inner(start, end):
        v = ary[start]
        vidx = start
        for i in range(start+1, end+1):
            if ary[i] < v:
                v = ary[i]
                vidx = i
        return vidx
    return inner

def rmq_pow_2(ary):
    """
    prep - O(n log n)
    exec - O(1)
    """
    N = len(ary)
    P = log2(N)
    width = 1
    last_row = list(range(N))
    mins = [last_row]
    min_index = pairwise_min_index_for_array(ary)
    for power in range(P):
        new_mins = [0] * (len(last_row) - width)
        for i in range(len(last_row) - width):
            min_value = min_index(last_row[i], last_row[i + width])
            new_mins[i] = min_value
        mins.append(new_mins)
        last_row = new_mins
        width *= 2 
    def inner(start, end):
        if start == end: return start
        delta = end - start
        length = log2(delta)
        end_index = end - (2 ** length - 1)
        cur_mins = mins[length]
        return min_index(cur_mins[start], cur_mins[end_index])
    return inner

def rmq_by_block(ary, index_subblock=False, block_rmq_algorithm=rmq_pow_2):
    N = len(ary)
    block_width = max(5, log2(N) / 4)
    num_blocks = N / block_width
    if num_blocks * block_width < N: num_blocks += 1 # round up
    min_block_indices = [None] * num_blocks
    min_index = min_index_for_array(ary)
    for i in range(N):
        block_idx = i / block_width
        if min_block_indices[block_idx] is None:
            min_block_indices[block_idx] = i
        else:
            min_block_indices[block_idx] = min_index(i, min_block_indices[block_idx])
    min_block_values = [ary[idx] for idx in min_block_indices]
    inner_rmq = block_rmq_algorithm(min_block_values)

    trees = {}
    block_signatures = []
    if index_subblock:
        for i in range(num_blocks):
            subarray =  ary[i * block_width : (i + 1) * block_width]
            while len(subarray) < block_width:
                subarray.append(float('inf'))
            signature = cartesian_tree(subarray).signature()
            block_signatures.append(signature)
            if signature not in trees:
                trees[signature] = rmq_pow_2(subarray)

    def min_block(start, end):
        block_idx = start / block_width
        signature = block_signatures[block_idx]
        sub_rmq = trees[signature]
        return sub_rmq(start % block_width, end % block_width) + block_idx * block_width

    def inner(start, end):
        start_block = start / block_width
        end_block = end / block_width
        if start_block == end_block:
            if index_subblock:
                return min_block(start, end)
            else:
                return min_index(*range(start, end + 1))
        if index_subblock:
            start_min = min_block(start, (start_block + 1) * block_width - 1)
            end_min = min_block(end_block * block_width, end)
        else:
            start_min = min_index(*range(start, (start_block + 1) * block_width))
            end_min = min_index(*range(end_block * block_width, end + 1))

        if start_block + 1 <= end_block - 1:
            block_min = min_block_indices[inner_rmq(start_block + 1, end_block - 1)]
            return min_index(start_min, block_min, end_min)
        return min_index(start_min, end_min)
    return inner

def rmq_by_subblock(ary):
    return rmq_by_block(ary, True)

def rmq_tester_1(rmq_maker, N):
    # test 1 - sequential
    rmq = rmq_maker(range(N))
    for i in range(N):
        for j in range(i, N):
            assert rmq(i, j) == min(i, j)

def rmq_tester_2(rmq_maker, N):
    # test 2 - reverse sequential
    rmq = rmq_maker(list(reversed(range(N))))
    for i in range(N):
        for j in range(i, N):
            assert rmq(i, j) == max(i, j)

def rmq_tester_3(rmq_maker, N):
    # test 3 - 1 minimum
    test = [1] * N
    test[10] = 0
    rmq = rmq_maker(test)
    for i in range(N):
        for j in range(i, N):
            assert test[rmq(i, j)] == (0 if (i <= 10 <= j) else 1)

def rmq_tester_random(rmq_maker, N, iteration = 1000):
    # test 4 - random test
    test = list(range(N))
    random.shuffle(test)
    rmq = rmq_maker(test)
    canonical = rmq_pow_2(test)
    for it in range(iteration):
        x = random.randint(0, N - 1)
        y = random.randint(0, N - 1)
        i = min(x, y)
        j = max(x, y)
        assert rmq(i, j) == canonical(i, j)

def rmq_tester(rmq_maker):
    for N in (16, 32, 64):
        rmq_tester_1(rmq_maker, N)
        rmq_tester_2(rmq_maker, N)
        rmq_tester_3(rmq_maker, N)
    rmq_tester_random(rmq_maker, 1000)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--algorithm', type=str)
    parser.add_argument('--size', type=int, default=1000)
    parser.add_argument('--run', type=int, default=1000)
    args = parser.parse_args()
    algorithms = {
            'naive': naive_rmq,
            'pow2': rmq_pow_2,
            'blocks': rmq_by_block,
            'subblocks': rmq_by_subblock
    }
    if args.algorithm == 'test':
        test()
    else:
        rmq_tester_random(algorithms[args.algorithm], args.size, args.run)

if __name__ == '__main__':
    main()
    

