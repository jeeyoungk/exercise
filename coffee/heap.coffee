# Binary min-heap implementation.
# Binary min-heap is a binary tree
# where the parent node <= child nodes. This is referred as the **min-heap invariant**.
#
# An exmaple of a min-heap would be:
# ```
#          0
#    1            2
#  3   4       5     6
# 7 8 9 10   11 12 13 14
# ```
#
# Internally, binary heap is stored in an array `(level, left-to-right)`
# traversal order. We maintain the following invariants.
#
# * Min-heap invariant.
# * Underlying binary tree is almost complete. If the last level
# of the tree isn't complete, the nodes of that level are filled from left
# to right.

# Default comparator function.
default_comparator = (x, y) ->
  if x > y then return 1
  else if x < y then return -1
  else return 0

# **Public**: Make a new instance of heap
make_heap = (comparator = default_comparator) ->
  # private variables. `data` is the underlying data store. `size` stores the
  # size of the heapj
  data = []; size = 0

  # `parent`, `left`, and `right` are functions to navigate the binary heap.
  parent = (index) -> Math.floor((index - 1) / 2)
  left   = (index) -> index * 2 + 1
  right  = (index) -> index * 2 + 2
  # `swap` swaps two indices of `data`.
  swap   = (i, j)  -> tmp = data[i]; data[i] = data[j]; data[j] = tmp
  # `index_comparator` compares data pointed by the indices.
  index_comparator = (i, j) -> comparator data[i], data[j]
  # Given a subsection of the form:
  # ```
  #   cur
  # L     R
  # ```
  # ensure that `cur < L and cur < R` holds.
  # Otherwise, swap `cur` with either `L` or `R`.
  # If a swap occurs, `maintain_invariant` is recursively invoked
  # on the subsection rooted by the swapped element.
  maintain_invariant = (cur_index) ->
    left_index = left(cur_index); right_index = right(cur_index)
    swap_index = null
    # Exit condition: Current node is a leaf node.
    if left_index >= size and right_index >= size
    # Current node only has a right child. See if a swap is needed.
    else if right_index >= size
      if index_comparator(cur_index, left_index) is 1
        swap_index = left_index
    # Has both left and right child. Note that a node cannot have only
    # the a left child since heap is almost fully balanced with bias
    # towards left subtrees.
    else
      cur_l = index_comparator cur_index, left_index
      cur_r = index_comparator cur_index, right_index
      l_r =   index_comparator left_index, right_index
      swap_index = (
        if cur_l isnt 1 and cur_r isnt 1 then null
        else if cur_l isnt 1 then right_index
        else if cur_r isnt 1 then left_index
        else if l_r is -1 then left_index
        else right_index
      )
    # Swap and recurse if needed. Otherwise exit.
    return if swap_index is null
    swap swap_index, cur_index
    maintain_invariant swap_index

  {
    # **Public**: Add a new element to the heap, while
    # maintaining the heap invariant.
    push: (val) ->
      index = size; data[size] = val; size++
      while index isnt 0
        parent_index = parent(index)
        if -1 is index_comparator index, parent_index
          swap parent_index, index
          index = parent_index
        else return
    # **Public**: Removes & returns the minimal element, while
    # maintaining the heap invariant.
    pop:  () ->
      if size is 0 then return null
      result = data[0]
      size--; data[0] = data[size]; data[size] = null
      maintain_invariant(0)
      return result
    # **Public**: Returns the minimal element, without removing it.
    peek: () -> if size is 0 then return null else data[0]
    # **Public**: The size of the heap.
    size: () -> size
  }

# Test Code
# =========
assert = (cond) -> unless cond then console.log 'error'

test_3 = ->
  tests = [
    [1, 2, 3]
    [1, 3, 2]
    [2, 1, 3]
    [2, 3, 1]
    [3, 1, 2]
    [3, 2, 1]
  ]
  for test in tests
    h = make_heap()
    h.push(val) for val in test
    assert h.pop() is 1
    assert h.pop() is 2
    assert h.pop() is 3
    assert h.size() is 0

test_10 = ->
  h = make_heap()
  h.push(val) for val in [0, 5, 1, 6, 2, 7, 3, 8, 4, 9]
  for val in [0..9]
    assert h.size() is 10 - val
    assert val is h.pop()
  assert h.size() is 0

test_3()
test_10()
