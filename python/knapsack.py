# integer Knapsack problem implementation

def knapsack(size, inputs):
    inputs = sorted(inputs)
    history = {0: ()}
    for cur_input in inputs:
        for prev_value, prev_history in history.items(): # items instead of iteritems, to take a deep copy
            new_value = prev_value + cur_input
            new_history = prev_history + (cur_input,)
            if new_value == size: return new_history
            history[new_value] = new_history
    return None # failed to  find a sum.

def knapsack_wrapper(size, inputs):
    result = knapsack(size, inputs)
    if result is None: print "%d is not possible from combining %s" % (size, ", ".join(map(str, inputs)))
    else: print "%d = %s" % (size, " + ".join(map(str, result)))

knapsack_wrapper(10, [10])
knapsack_wrapper(10, [3, 5, 2])
knapsack_wrapper(2536, [132,524,241,523,251,231,634])
knapsack_wrapper(10, [1,2,3,4,5])
knapsack_wrapper(63, [10, 20, 30, 32, 21])
knapsack_wrapper(10, [3, 8])
knapsack_wrapper(10, [1])
knapsack_wrapper(2535, [132,524,241,523,251,231,634])
