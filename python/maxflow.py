# algorithm to implement max-flow min-cut

def bfs(N, start, end, dist):
    'find an augmenting path from start to end via dijkstra on a graph.'
    visited = [False] * N
    distance = [None] * N
    pointer = [None] * N
    distance[start] = 0
    while not visited[end]:
        latest_frontier = None # next vertex to visit.
        for i in xrange(N):
            if distance[i] is None or visited[i]: continue
            if latest_frontier is None or distance[latest_frontier] > distance[i]:
                latest_frontier = i
        if latest_frontier is None: return None # no path.
        visited[latest_frontier] = True
        for i in xrange(N):
            # update the known minimum distance.
            if dist(latest_frontier, i) is None: continue
            candidate_distance = distance[latest_frontier] + dist(latest_frontier, i)
            if distance[i] is None or distance[i] > candidate_distance:
                distance[i] = candidate_distance
                pointer[i] = latest_frontier
    # reconstruct the path (start, 1, 2, 3, ... end)
    path = []
    cur_index = end
    path.append(end)
    while pointer[cur_index] is not None:
        path.append(pointer[cur_index])
        cur_index = pointer[cur_index]
    path.reverse()
    return path

def find_max_capacity(path, dist):
    max_capacity = None
    for i in xrange(len(path) - 1):
        if max_capacity is None: max_capacity = dist(path[i], path[i+1])
        else: max_capacity = min(max_capacity, dist(path[i], path[i+1]))
    return max_capacity

def max_flow_min_cut(graph, start = 0, end = None):
    'graph is an adjancecy graph of flow capacity'
    N = len(graph)
    if end is None: end = N - 1
    f = new_graph(N, 0) # flow graph - initialize to 0.

    def residual_distance(i, j):
        'utility function for the residual graph. should be postiive or None (has no edge or no residual capacity).'
        if graph[i][j] is not None: v = graph[i][j] - f[i][j]
        elif graph[j][i] is not None: v = f[j][i]
        else: v = 0
        return v if v > 0 else None

    while True:
        augpath = bfs(N, start, end, residual_distance)
        if augpath is None: # terminal condition - no more augmenting path. return the flow value.
            return sum(f[start])
        else: # otherwise adjust the flow along the augpath.
            max_capacity = find_max_capacity(augpath, residual_distance)
            for i in xrange(len(augpath) - 1):
                f[augpath[i]][augpath[i+1]] += max_capacity
                f[augpath[i+1]][augpath[i]] -= max_capacity

def new_graph(N, value=None):
    'new adjancy graph of size N.'
    return [[value] * N for i in xrange(N)]

def test_1():
    # using the example in p. 710 in CLRS, 3rd edition.
    g = new_graph(6)
    # name to id mapping
    n = {
        'vancouver': 0, 'edmonton': 1,
        'calgary': 2,   'saskatoon': 3,
        'regina': 4,    'winnipeg': 5,
    }
    g[n['vancouver']][n['edmonton']] = 16
    g[n['vancouver']][n['calgary']] = 13
    g[n['edmonton']][n['saskatoon']] = 12
    g[n['calgary']][n['edmonton']] = 4
    g[n['calgary']][n['regina']] = 14
    g[n['saskatoon']][n['winnipeg']] = 20
    g[n['saskatoon']][n['calgary']] = 9
    g[n['regina']][n['saskatoon']] = 7
    g[n['regina']][n['winnipeg']] = 4
    assert max_flow_min_cut(g) == 23

def test_2():
    g = new_graph(2)
    assert max_flow_min_cut(g) == 0

def test_3():
    g = new_graph(4)
    g[0][1] = 1
    g[1][2] = 2
    g[2][3] = 3
    assert max_flow_min_cut(g) == 1
    
def test_4():
    g = new_graph(4)
    g[0][1] = 1000000
    g[0][2] = 1000000
    g[1][3] = 1000000
    g[1][2] = 1
    g[2][3] = 1000000
    assert max_flow_min_cut(g) == 2000000

test_1()
test_2()
test_3()
test_4()
