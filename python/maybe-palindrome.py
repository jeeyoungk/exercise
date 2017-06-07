import sys

def maybe_palindrome(string, start, end, removed):
    if removed >= 2:
        return None # cannot find one.
    if start >= end:
        return -1 # is a palindrome.
    if string[start] == string[end]:
        return maybe_palindrome(string, start + 1, end - 1, removed)
    # try removing one.
    result = maybe_palindrome(string, start + 1, end, removed + 1)
    if result == -1:
        return start
    result = maybe_palindrome(string, start, end - 1, removed + 1)
    if result == -1:
        return end
    return None

def find_index(string):
    stack = []
    stack.append((0, len(string) - 1, None))
    while len(stack) > 0:
        start, end, removed = stack.pop()
        if start >= end:
            if removed is None:
                return -1
            else:
                return removed
        elif string[start] == string[end]:
            stack.append((start+1, end-1, removed))
        elif removed is None:
            stack.append((start, end-1, end))
            stack.append((start+1, end, start))

    return -1

if __name__ == '__main__':
    q = int(raw_input().strip())
    for a0 in xrange(q):
        s = raw_input().strip()
        result = find_index(s)
        print(result)
