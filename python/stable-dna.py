import sys
import random
random.seed(0)

SHUFFLE=True

def run(string):
    expected = len(string) / 4
    shortest_from_start = [len(string) for i in xrange(len(string))]
    shortest_to_end = [0 for i in xrange(len(string))]

    def attempt_consume(start, end, char, a, t, g, c):
        if char == 'A': a += 1
        elif char == 'T': t += 1
        elif char == 'G': g += 1
        elif char == 'C': c += 1

        if a <= expected and t <= expected and g <= expected and c <= expected:
            return (start, end, a, t, g, c)
        else:
            return None

    def stable_dna(start, end, a, t, g, c):
        start_char = string[start]
        end_char = string[end]

        if shortest_from_start[start] < end:
            print 'skip R', shortest_from_start[start], end
            right_consume = None
        else:
            right_consume = attempt_consume(start, end-1, end_char, a, t, g, c)

        if shortest_to_end[end] > start:
            print 'skip L', start, shortest_to_end[end]
            left_consume = None
        else:
            left_consume = attempt_consume(start+1, end, start_char, a, t, g, c)
        results = []
        if right_consume is not None:
            results.append(right_consume)
        if left_consume is not None:
            results.append(left_consume)
        if len(results) > 1 and SHUFFLE:
            random.shuffle(results)
            
        return results

    # print string
    stack = []
    visited = set()
    stack.append((0, len(string) - 1, 0, 0, 0, 0))
    min_dist = len(string)
    while len(stack) > 0:
        start, end, a, t, g, c = stack.pop()
        min_dist = min(min_dist, end - start + 1)
        if start > end: continue
        elif (start, end) in visited: continue

        # print start, end, end - start + 1, (a, t, g, c)
        print (start, end)
        shortest_from_start[start] = min(end, shortest_from_start[start])
        shortest_to_end[end] = max(start, shortest_to_end[end])
        visited.add((start, end))
        results = stable_dna(start, end, a, t, g, c)
        for result in results:
            stack.append(result)
    # print sorted(visited)
    # print min_dist
    print len(visited)
    # print shortest_from_start
    # print shortest_to_end
    return min_dist

def main():
    q = int(raw_input().strip())
    s = raw_input().strip()
    result = run(s)
    print(result)

# print run('GG' + 'A'*20 + 'TT')
# print run('AAAATTTTGGGG')
print run('AAAA' * 9)
# print run('AAAA' * 100)
# run('GAAATAAA')
# print run('TGATGCCGTCCCCTCAACTTGAGTGCTCCTAATGCGTTGC')

# main()

