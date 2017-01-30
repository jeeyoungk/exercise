import json
import random

# random.seed(1) # deterministic behavior

def extract_coord(row):
    coordinates = row['coordinates']
    return (coordinates['lat'], coordinates['lng'])

def obtain_data():
    with open('transactions.json', 'rb') as f:
        parsed = json.load(f)
    coords = map(extract_coord, parsed)
    return coords


def dist(coord_a, coord_b):
    a_x, a_y = coord_a
    b_x, b_y = coord_b
    return ((a_x - b_x) ** 2 + (a_y - b_y) ** 2) ** 0.5

def assign_cluster(coord, clusters):
    shortest_dist = None
    assigned_cluster = None
    for cluster_id, cluster in enumerate(clusters):
        cur_dist = dist(coord, cluster)
        if shortest_dist is None or shortest_dist > cur_dist:
            shortest_dist = cur_dist
            assigned_cluster = cluster_id
    return assigned_cluster

def re_calculate(coords, assignments, cluster_id):
    count = 0
    sum_x = 0.0
    sum_y = 0.0
    for coord, assignment in zip(coords, assignments):
        # coords * K
        if assignment != cluster_id:
            # throw away a lot of coords here.
            continue
        x, y = coord
        count += 1
        sum_x += x
        sum_y += y
    return (sum_x / count, sum_y / count)

def re_calculate_2(coords, assignments, K):
    counts = [0 for i in xrange(K)]
    sum_x = [0.0 for i in xrange(K)]
    sum_y = [0.0 for i in xrange(K)]
    for coord, assignment in zip(coords, assignments):
        x, y = coord
        counts[assignment] += 1
        sum_x[assignment] += x
        sum_y[assignment] += y
    output = []
    for i in xrange(K):
        count = counts[i]
        output.append((sum_x[i] / count, sum_y[i] / count))
    return output

        

def kmeans(coords, K):
    # TODO - termination condition
    # TODO - cluster initialization
    cluster_centers = random.sample(coords, K)
    assignments = [assign_cluster(coord, cluster_centers) for coord in coords]
    debug = False
    for i in xrange(10):
        if debug:
            print "Iteration: %d" % i
            print "Centers  : %s" % cluster_centers
        # new_cluster_centers = [re_calculate(coords, assignments, cluster_id) for cluster_id in xrange(K)]
        new_cluster_centers = re_calculate_2(coords, assignments, K)
        new_assignments = [assign_cluster(coord, new_cluster_centers) for coord in coords]
        if new_assignments == assignments:
            print "Breaking @ %d" % (i + 1)
            return new_cluster_centers
        assignments = new_assignments
        cluster_centers = new_cluster_centers
        # TODO - heuristics around how fast the centers are moving?
    return cluster_centers

def main():
    coords = obtain_data()
    print kmeans(coords, 2)
    # print kmeans(coords, 3)

def test():
    assert abs(dist((0, 0), (1, 0)) - 1.0) < 0.01
    assert abs(dist((0, 0), (1, 1)) - 1.414213) < 0.01
    assert abs(dist((2, 2), (1, 1)) - 1.414213) < 0.01
    print assign_cluster((0, 0), [(1, 1), (2, 2)])
    print assign_cluster((3, 3), [(1, 1), (2, 2)])
    kmeans([
        (0, 0),
        (0, 1),
        (1, 0),
        (1, 1),
        (2, 2),
        (3, 3),
        (4, 4),
        (5, 5),
        (5, 6),
        (6, 5),
        (6, 6),
    ], 2)

if __name__ == '__main__':
    # test()
    main()
