# Algorithm to compute pi via subdividing unit square recursively
# and seeing it is part of the circle or not.
from __future__ import division

def iterate(x, y, iteration, N):
    '''
    Determines whether a square
    size = (1 / 2 ^ iteration)
    at coord (x, y)

    is inside the unit circle or not.

    returns 2-tuple of
    (volume_inside, volume_outside)
    where volume_insdie + volume_outside <= 1 / 4 ^ iteration
    '''
    length = 2 ** iteration
    low_x = x / length
    low_y = y / length
    high_x = (x + 1) / length
    high_y = (y + 1) / length
    low_length = low_x ** 2 + low_y ** 2
    high_length = high_x ** 2 + high_y ** 2
    if low_length > 1: return (0, 1) # NOT part of the circle
    elif high_length < 1: return (1, 0) # part of the circle
    elif iteration == N: return (0, 0) # uncertain

    # recursive call - subdivide the square to 4 chunks
    # and collect their results.
    ld1, ud1 = iterate(x * 2, y * 2, iteration + 1, N)
    ld2, ud2 = iterate(x * 2, y * 2 + 1, iteration + 1, N)
    ld3, ud3 = iterate(x * 2 + 1, y * 2, iteration + 1, N)
    ld4, ud4 = iterate(x * 2 + 1, y * 2 + 1, iteration + 1, N)
    return ((ld1 + ld2 + ld3 + ld4) / 4, (ud1 + ud2 + ud3 + ud4) / 4)

def around_border(x, y, N):
    length = 2 ** N
    low_x = x / length
    low_y = y / length
    high_x = (x + 1) / length
    high_y = (y + 1) / length
    low_length = low_x ** 2 + low_y ** 2
    high_length = high_x ** 2 + high_y ** 2
    if low_length > 1: return False
    elif high_length < 1: return False
    return True

def navigating(N):
    length = 2 ** N
    x = 0
    y = length - 1
    inside = length - 1
    outside = 0
    border = 1

    while not (x == 2 ** N - 1 and y == 0):
        # print((x, y), inside, outside)
        right = around_border(x + 1, y, N)
        bottom = around_border(x, y - 1, N)
        assert not (right and bottom), "(%d, %d) Cannot navigate" % (x, y)
        assert right or bottom, "(%d, %d) Navigating both" % (x, y)
        if right:
            inside += y
            outside += (length - y - 1)
            x += 1
        elif bottom:
            inside -= 1
            y -= 1
    # print((x, y), inside, outside)
    return (
        (inside  / length / length),
        (outside / length / length)
    )


def calculate(N, algorithm):
    lower, upper = algorithm(N)
    pi_lower = lower * 4
    pi_upper = (1 - upper) * 4
    delta = ((1 - upper) - lower) * 4
    print("%2d: %f < pi < %f (delta = %f)" % (N, pi_lower, pi_upper, delta))


for i in range(20):
    print("")
    calculate(i, lambda N: iterate(0, 0, 0, N))
    calculate(i, navigating)

# sample out:
'''
 0: 0.000000 < pi < 4.000000 (delta = 4.000000)
 1: 1.000000 < pi < 4.000000 (delta = 3.000000)
 2: 2.000000 < pi < 3.750000 (delta = 1.750000)
 3: 2.562500 < pi < 3.500000 (delta = 0.937500)
 4: 2.859375 < pi < 3.343750 (delta = 0.484375)
 5: 3.007812 < pi < 3.253906 (delta = 0.246094)
 6: 3.075195 < pi < 3.199219 (delta = 0.124023)
 7: 3.107910 < pi < 3.170166 (delta = 0.062256)
 8: 3.125549 < pi < 3.156738 (delta = 0.031189)
 9: 3.133484 < pi < 3.149094 (delta = 0.015610)
10: 3.137589 < pi < 3.145397 (delta = 0.007809)
11: 3.139624 < pi < 3.143529 (delta = 0.003905)
12: 3.140601 < pi < 3.142554 (delta = 0.001953)
13: 3.141100 < pi < 3.142076 (delta = 0.000977)
14: 3.141347 < pi < 3.141835 (delta = 0.000488)
15: 3.141470 < pi < 3.141714 (delta = 0.000244)
16: 3.141531 < pi < 3.141653 (delta = 0.000122)
17: 3.141562 < pi < 3.141623 (delta = 0.000061)
18: 3.141577 < pi < 3.141608 (delta = 0.000031)
19: 3.141585 < pi < 3.141600 (delta = 0.000015)
'''
