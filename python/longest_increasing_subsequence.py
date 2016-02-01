# implements longest increasing subsequence
# from https://en.wikipedia.org/wiki/Longest_increasing_subsequence

import bisect

def lis(seq):
    N = len(seq)
    # predecessor list
    P = [None for _ in xrange(N)]
    # memo list
    M = [None for _ in xrange(N + 1)]
    L = 0

    for i in range(N):
        newL = bisect.bisect_left(seq, seq[i], lo=0, hi=L+1)
        P[i] = M[newL]
        M[newL] = i
        print 'newL', newL
        if newL > L:
            L = newL
    k = M[L]
    S = []
    for i in range(L+1):
        S.append(seq[k])
        print P[k]
        print L
        k = P[k]
    S.reverse()
    return S
        
# print lis([1,2,4,5,3])
# print lis([1])
# print lis([3, 1, 2])
# print lis([10, 20, 30, 40, 41, 42, 43, 30, 31, 32, 33, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29])
# print lis([21, 22, 23, 24, 25, 26, 27, 28, 29])
