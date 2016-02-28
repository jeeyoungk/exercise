
def palindrome(a):
    """
    Given a string a, turn it into a palindrome with minimum # of additions.
    
    returns triple (original input, palindrome, # of additions)
    """
    if len(a) == 0:
        return ("", "", 0)
    cache = {}
    def inner(i, j):
        # returns a palindrome generated from the substring (a[i], ... , a[j])

        # i - left pointer, j - outer pointer
        # i starts at 0, j starts at len(a) - 1
        # i --> middle <-- j

        # memoization
        if (i, j) in cache: return cache[(i, j)]

        if i == j:
            # base case - string is length of 1. nothing to do.
            value = a[i]
        elif a[i] == a[j]:
            if i + 1 == j:
                # base case - string is length of 2, in the form of "xx". nothing to do.
                value = (a[i] + a[j])
            else:
                # recursing case - string is in the form of "xAx". recurse.
                value = a[i] + inner(i + 1, j - 1) + a[j]

        else:
            # recursing case - string is in the form of "xAy"
            # we explore two cases:
            # x inner(Ay) x
            # y inner(xA) y
            # which are both palindrome, as guaranteed by inner()
            left = inner(i + 1, j)
            right = inner(i, j - 1)
            if len(left) < len(right):
                value = a[i] + left + a[i]
            else:
                value = a[j] + right + a[j]
        cache[(i, j)] = value # memoize the value.
        return value
    result = inner(0, len(a) - 1)
    return (a, result, len(result) - len(a))

def main():
    for a in ["", "a", "aa", "aaa", "aaab", "baab",
            "aaabc", "aaabcaaa", "abcxyz",
            "0010 1010 1101 1101 1101",
            "aaaaXaaaXaaaaaXaYaaaXaaaaXaaaaa",
            "my name is jeeyoung kim and i like pineapples",
            "google is google"
            ]:
        print "%s -> %s (+%d)" % palindrome(a)

if __name__ == '__main__':
    main()
