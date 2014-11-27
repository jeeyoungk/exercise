# Cycle detection algorithm, via
# DFS (depth first search) and back-edge
# detection.


# Implementation
# ==============

# detect a cycle.
# graph - {vertex:[vertices...]}
has_cycle = (graph) ->
  # set of root vertices.
  roots = find_root(graph)
  # If the root set is empty, then there MUST be a cycle.
  return false if roots.length is 0
  # All the visited node.
  visited = {}
  # All the visited node in current path.
  inpath = {}

  found = false
  traverse = (node) ->
    if inpath[node]? then found = true
    return if found # short circuit.
    return if visited[node]
    visited[node] = true
    inpath[node] = true
    traverse(neigh) for neigh in graph[node]
    delete inpath[node]
  for root in roots
    traverse(root)
  return found

find_root = (graph) ->
  # 1. reverse the graph.
  has_reverse = {}
  for parent, neigh of graph
    has_reverse[parent] ||= false
    for child in neigh
      has_reverse[child] ||= true
  result = []
  for vertex, is_root of has_reverse
    unless is_root then result.push vertex
  return result

# Test code
# =========

test = ->
  test_find_root()
  test_1()

assert = (cond) -> unless cond then console.log 'error'

test_find_root = ->
  graph = { 'a' : ['b'], 'b' : ['c'] }
  assert 1 is find_root(graph).length

test_1 = ->
  graph = { 'a' : ['b'], 'b' : ['c'], 'c' : ['d'], 'd' : ['b'] }
  assert has_cycle(graph)

test()
