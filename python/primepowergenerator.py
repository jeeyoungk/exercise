"""
Given a list of primes (p1, p2, p3 ... pn)
generate composite numbers of their multiples
in increasing order.
"""
import heapq

def generate(primes):
    # assume primes is sorted 
    h = [] # min-heap.
    # logic: seed the heap with the primes, pop and generate from the heap.
    # for every popped element v, add (v * p) for all p in primes.
    # we are guaranteed to generate all composites in order.
    for p in primes: heapq.heappush(h, p)


    while True:
        value = heapq.heappop(h)
        yield value
        for p in primes:
            # attempt to insert all (value * p)
            # Optimization:
            # however, we can optimize inserting value * p if 
            # value * p | q = 0, q < p since
            # (value * p / q) is generated beforehand (since p / q < 1)
            # and (value * p / q) * q = (value * p)
            # would've been inserted when (value * p / q) is generated.
            should_insert = True
            new_insert_value = value * p
            for q in primes:
                if q >= p: break
                if new_insert_value % q == 0:
                    should_insert = False
                    break
            if should_insert: heapq.heappush(h, new_insert_value)

for i, v in enumerate(generate([2, 3, 5])):
    if i == 100: break
    print v

for i, v in enumerate(generate([3, 11])):
    if i == 100: break
    print v
