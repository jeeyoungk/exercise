# implements longest increasing subsequence
# from https://en.wikipedia.org/wiki/Longest_increasing_subsequence

ef lis(seq):
    N = len(seq)
    P = [] # previous list
    M = [None] * (N + 1) # history list.
    L = 0

    for i in range(N):
        current = seq[i]
        lo = 1; hi = L
        # binary search - find the largest j <= L s.t. seq[M[j]] < current
        while lo <= hi:
            mid = (lo + hi + 1) // 2
            if seq[M[mid]] < current: lo = mid + 1
            else: hi = mid - 1
        newL = lo
        P.append(M[newL-1])
        M[newL] = i
        if newL > L: L = newL # increase by at most 1.
    k = M[L]
    S = [None] * L
    # construct the series.
    for i in range(L):
        S[L - i  - 1] = (seq[k])
        k = P[k]
    return S
        
print(lis([1]))
print(lis([1,2]))
print(lis([3,1,2]))
print(lis([1,2,3]))
print(lis([20,21,22,10,11,12]))
print(lis([10, 20, 30, 40, 41, 42, 43]))
print(lis([10, 20, 30, 40, 41, 42, 43, 30, 31, 32, 33, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29]))
print(len(lis(range(100))) == 100)
print(len(lis(range(1000))) == 1000)
print(len(lis(range(10000))) == 10000)
