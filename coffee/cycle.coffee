# Cycle detection algorithm, via
# DFS (depth first search) and back-edge
# detection.


# Implementation
# ==============

# `has_cycle` - determines whether a given graph contains a cycle.
#
# `graph` is an object of the form `{vertex:[outgoing vertices...]}`
has_cycle = (graph) ->
  # set of root vertices. DFS are started from these.
  roots = find_root graph
  # If the root set is empty, then it is cyclic graph.
  return false if roots.length is 0
  # All the visited node. Set is additive during the execution
  # of algorithm.
  visited = {}
  # All the visited node in current path. Added & removed during recursion.
  inpath = {}
  # true if a cycle is found.
  found = false
  # Performs a depth first search traversal, and sees if it finds a
  # back edge to the traversal path.
  # Visisted vertices are tracked for efficiency.
  dfs = (node) ->
    if inpath[node]? then found = true
   # Short circuit if a cycle is found. Unwind the stack.
    return if found
    return if visited[node] # No need to traverse the same node again.
    visited[node] = true; inpath[node] = true
    dfs neighbour for neighbour in (graph[node] or [])
    delete inpath[node]
  dfs root for root in roots
  return found

# Find all the vertices with no inbound edge.
find_root = (graph) ->
  has_reverse = {}
  for parent, neigh of graph
    has_reverse[parent] ||= false
    for child in neigh
      has_reverse[child] ||= true
  return (node for node, is_root of has_reverse when not is_root)

# Test code
# =========
test = ->
  test_find_root()

  test_cycle()
  test_line()
  test_tree()

assert = (cond) -> unless cond then console.log 'error'

test_find_root = ->
  assert 1 is find_root({ 'a' : ['b'], 'b' : ['c'] }).length

test_cycle = ->
  assert has_cycle { 'a' : ['b'], 'b' : ['c'], 'c' : ['d'], 'd' : ['b'] }

test_line = ->
  assert not has_cycle { 'a' : ['b'], 'b' : ['c'] , 'c': ['d'], 'd' : ['e'] }

test_tree = ->
  assert not has_cycle {
    root : ['a','b','c']
    a : ['aa', 'ab', 'ac', 'ad']
    b : ['ba']
    aa : ['aaa', 'aab']
    ab : ['aba']
    ba : ['baa', 'bab', 'bac', 'bad']
  }
test()
