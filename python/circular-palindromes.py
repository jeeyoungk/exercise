import heapq

def s(cell):
    if cell: return 'O'
    else: return '.'

def valid(i, j, wrap_start):
    return (
        (wrap_start <= i <= j) or
        (i <= j < wrap_start) or
        (wrap_start <= i and j < wrap_start)
    )

assert valid(0, 3, 0)
assert not valid(2, 1, 0)
assert valid(1, 3, 1)
assert not valid(0, 3, 1)
assert valid(1, 2, 1)
assert valid(1, 0, 1)
assert not valid(1, 0, 10)
assert valid(20, 0, 10)
assert valid(20, 40, 10)
assert valid(20, 9, 10)
assert not valid(20, 10, 10)
assert not valid(20, 11, 10)

def find_palindromes(string):
    N = len(string)
    table = [[False for i in xrange(N)] for j in xrange(N)]
    def wrap(i): return i % N
    def wrap_len(i, j):
        if j < i: return N + j - i + 1
        else: return j - i + 1 # TODO

    for i in xrange(N):
        table[i][i] = True

    for i in xrange(N):
        j = wrap(i+1)
        table[i][j] = string[i] == string[j]
    for length in xrange(2, len(string)):
        print length
        for i in xrange(N):
            i_inner = wrap(i + 1)
            j_inner = wrap(i + length - 1)
            j = wrap(i + length)
            table[i][j] = string[i] == string[j] and table[i_inner][j_inner]
    h = []
    for i in xrange(N):
        for j in xrange(i, N):
            length = wrap_len(i, j)
            # print i, j, table[i][j]
            if table[i][j]:
                heapq.heappush(h, (-length, (i, j)))
    return
    # print string
    # print "\n".join(["".join([s(cell) for cell in row]) for row in table])
    # initial heap.
    # print h
    tpl = h[0]
    length, (i, j) = tpl
    print -length
    # 0 1 2 3 4
    for wrap_start in xrange(1, N):
        # print "wrap-start %d" % wrap_start
        # i = tarting point.
        j = wrap(wrap_start - 1)
        for i in xrange(N):
            if i == j:
                continue
            assert valid(i, j, wrap_start)
            assert not valid(i, j, wrap_start - 1)
            if table[i][j]:
                # print 'inserting', (i, j), wrap_start
                length = wrap_len(i ,j)
                heapq.heappush(h, (-length, (i, j)))
            else:
                pass
                # print '__serting', (i, j), wrap_start
        while True:
            length, (i, j) = h[0]
            # print 'validity', i, j, wrap_start, valid(i, j, wrap_start)
            if not valid(i, j, wrap_start):
                if len(h) == 0:
                    assert False, "popped all the stack"
                heapq.heappop(h)
            else:
                print -length
                break


def main():
    q = int(raw_input().strip())
    s = raw_input().strip()
    result = find_palindromes(s)

if __name__ == '__main__':
    # main()
    pass

