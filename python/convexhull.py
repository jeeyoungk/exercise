import math
import matplotlib.patches
import matplotlib.pyplot
import numpy.random

from matplotlib.path import Path

def calculate_angle(source, point):
    x0, y0 = source
    x1, y1 = point
    dx = x1 - x0
    dy = y1 - y0
    angle = math.atan2(dy, dx)
    return (angle, point)

def calculate_angles(source, points):
    return [calculate_angle(source, point) for point in points]

def normalize_angle(theta, delta):
    if theta > delta:
        return theta - delta
    else:
        return theta - delta + math.pi * 2

def convexhull(points):
    """using the gift wrapping method"""
    start = max(points, key = lambda t : (t[1], t[0]))
    hull = [start] # start with the leftmost point
    while True:
        last = hull[-1]
        angles = calculate_angles(last, points)
        output = [angle for angle in angles if angle[1] not in hull] # list of (angle, points)
        if len(hull) == 1:
            next_point = min(output, key=lambda x: x[0])[1]
        else:
            second_last = hull[-2]
            output += [calculate_angle(last, hull[0])] # first hull point is always a candidate.
            delta = calculate_angle(second_last, last)[0]
            next_point = min(output, key=lambda x: normalize_angle(x[0], delta))[1]
        hull.append(next_point)
        if hull[-1] == hull[0]: break # terminal condition
    return hull

def generate_code(n):
    """generate path command codes for matplotlib for a convex hull"""
    result = []
    def index_to_code(i):
        if i == 0: return Path.MOVETO
        elif i == n - 1: return Path.CLOSEPOLY
        else: return Path.LINETO
    return map(index_to_code, xrange(n))

count = 0
fig = matplotlib.pyplot.figure()

def execute(points):
    global count
    global fig
    count += 1
    hull = convexhull(points)
    codes = generate_code(len(hull))
    path = Path(hull, codes)
    ax = fig.add_subplot(3, 2, count)
    limit = 10
    patch = matplotlib.patches.PathPatch(path, facecolor='orange', alpha = 0.1, lw=2)
    # plot the points
    x = [p[0] for p in points]
    y = [p[1] for p in points]
    ax.scatter(x, y, s = 10)
    # plot the convex hull
    ax.add_patch(patch)
    ax.set_xlim(-limit, limit)
    ax.set_ylim(-limit, limit)

def test_1():
    execute([(1, 0), (-1, 0), (0, 1), (0, -1), (0, 0)])

def test_2():
    execute([(1, 0), (-1, 0), (0, 1), (0, -1), (0, 0), (0, -0.25)])

def test_3():
    result = []
    for i in range(-2, 3):
        for j in range(-2, 3):
            # add a bit of variation
            result.append((i + numpy.random.normal(scale=0.01), j + numpy.random.normal(scale=0.01)))
    execute(result)

def test_random_1():
    result = []
    for i in range(100):
        result.append((numpy.random.normal(), numpy.random.normal()))
    execute(result)

def test_random_2():
    """two gaussian distributions"""
    result = []
    for i in range(1000):
        result.append((numpy.random.normal(2), numpy.random.normal(2)))
    for i in range(1000):
        result.append((numpy.random.normal(-2), numpy.random.normal(-2)))
    execute(result)

def test_random_3():
    """three gaussian distributions"""
    result = []
    for i in range(100):
        result.append((numpy.random.normal(3), numpy.random.normal()))
    for i in range(100):
        result.append((numpy.random.normal(-3), numpy.random.normal()))
    for i in range(100):
        result.append((numpy.random.normal(), numpy.random.normal(-3)))
    execute(result)

test_random_1()
test_random_2()
test_random_3()
test_1()
test_2()
test_3()
matplotlib.pyplot.show()
