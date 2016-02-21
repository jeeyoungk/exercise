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
    # length = (dx ** 2 + dy ** 2) ** 0.5
    angle = math.atan2(dy, dx)
    return (angle, point)

def calculate_angles(source, points):
    return [calculate_angle(source, point) for point in points]

def convexhull(points):
    """using the gift wrapping method"""
    start = max(points, key = lambda t : (t[1], t[0]))
    hull = [start] # start with the leftmost point
    while True:
        last = hull[-1]
        angles = calculate_angles(last, points)
        output = [angle for angle in angles if angle[1] not in hull]
        if len(hull) > 1:
            output += [calculate_angle(last, hull[0])]
        output = sorted(output)
        next_point = min(output)
        hull.append(next_point[1])
        if hull[-1] == hull[0]:
            break
    return hull

def generate_code(n):
    result = []
    for i in xrange(n):
        if i == 0:
            result.append(Path.MOVETO)
        elif i == n - 1:
            result.append(Path.CLOSEPOLY)
        else:
            result.append(Path.LINETO)
    return result

def execute(points):
    hull = convexhull(points)
    codes = generate_code(len(hull))
    path = Path(hull, codes)
    fig = matplotlib.pyplot.figure()
    ax = fig.add_subplot(111)
    patch = matplotlib.patches.PathPatch(path, facecolor='orange', alpha = 0.1, lw=2)
    x = [p[0] for p in points]
    y = [p[1] for p in points]
    print x
    ax.scatter(x, y, s = 10)
    ax.add_patch(patch)
    limit = 10
    ax.set_xlim(-limit, limit)
    ax.set_ylim(-limit, limit)
    matplotlib.pyplot.show()

def test_1():
    execute([(1, 0), (-1, 0), (0, 1), (0, -1), (0, 0)])

def test_2():
    execute([(1, 0), (-1, 0), (0, 1), (0, -1), (0, 0), (0, -0.25)])

def test_random():
    result = []
    for i in range(10):
        result.append((numpy.random.normal(), numpy.random.normal()))
    execute(result)

test_2()
