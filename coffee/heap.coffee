# Binary max-heap implementation.
#         0
#    1          2
#  3   4     5     6
# 7 8 9 10 11 12 13 14
# ....
default_comparator = (x, y) ->
  if x > y then return 1
  else if x < y then return -1
  else return 0

make_heap = (comparator = default_comparator) ->
  data = []
  size = 0

  parent = (index) -> Math.floor((index - 1) / 2)
  left   = (index) -> index * 2 + 1
  right  = (index) -> index * 2 + 2
  swap   = (i, j)  -> tmp = data[i]; data[i] = data[j]; data[j] = tmp
  index_comparator = (i, j) -> comparator data[i], data[j]
  return {
    push: (val) ->
      index = size
      data[size] = val
      size++
      # reshuffle.
      while index isnt 0
        parent_index = parent(index)
        if -1 is index_comparator index, parent_index
          swap parent_index, index
          index = parent_index
        else break
    pop:  () ->
      if size is 0 then return null
      result = data[0]
      data[0] = data[size - 1]
      data[size] = null
      size--
      # re-shuffle.
      parent_index = 0
      while true
        # We need to maintain the heap invariance.
        # ```
        #   parent
        # A        B
        # ```
        left_index = left(parent_index); right_index = right(parent_index)
        if left_index > size and right_index > size
          break
        else if right_index > size
          if index_comparator(left_index, parent_index) is -1
            swap left_index, parent_index
            parent_index = left_index
          else
            break
        else
          if index_comparator(left_index, parent_index) isnt -1 and index_comparator(right_index, parent_index) isnt -1
            break
          # TODO - why is this part so hard. OTL
        result
      return result

    peek: () -> if size is 0 then return null else data[0]
    size: () -> size
    data: data
  }

test = ->
  h = make_heap()
  h.push 8
  h.push 1
  h.push 2
  h.push 3
  h.push 5
  h.push 4
  h.push 6
  h.push 7
  console.log h.data
test()
