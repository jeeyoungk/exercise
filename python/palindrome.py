
def palindrome(a):
    """Given a string a, turn it into a palindrome with minimum # of additions"""
    if len(a) == 0:
        return ("", "", 0)
    cache = {}
    def inner(i, j):
        # i - left pointer, j - outer pointer
        if (i, j) in cache: return cache[(i, j)]

        if i == j:
            value = a[i]
        elif a[i] == a[j]:
            if i + 1 == j:
                value = (a[i] + a[j])
            else:
                value = a[i] + inner(i + 1, j - 1) + a[j]

        else:
            # need to add a single character
            left = inner(i + 1, j)
            right = inner(i, j - 1)
            if len(left) < len(right):
                value = a[i] + left + a[i]
            else:
                value = a[j] + right + a[j]
        cache[(i, j)] = value
        return value
    result = inner(0, len(a) - 1)
    return (a, result, len(result) - len(a))

def main():
    for a in ["", "a", "aa", "aaa", "aaab", "baab", "aaabc", "aaabcaaa", "abcxyz"]:
        print "%s -> %s (+%d)" % palindrome(a)

if __name__ == '__main__':
    main()
