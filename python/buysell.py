# Determine best time to buy and sell a given item.
# input - time series [v_0, v_1 ... v_n]
# input - k >= 1 - # of times you can buy / sell. you cannot buy again if you've already bought one.
# output - total delta you are making.

# This implementation takes O(n log n),
# but it can be optimized to O(n) if
# quickselect is used instead of sorting.

def buy_sell(raw_prices, k):
    prices = normalize(raw_prices)
    maxes = max_from(prices)
    mins = partitioned_min_since(prices, maxes)
    # normalization + maxes + mins gives a nice property, where
    # max[idx] - min[idx] gives a single optimal solution
    # for a buy-sell range that contains idx.
    possibilities = []
    N = len(prices) / 2
    for idx in xrange(0, len(prices), 2):
        if idx < len(prices) - 2: # not last element
            if mins[idx] != mins[idx+2] or maxes[idx] != maxes[idx+2]:
                # end of a range - append delta for the entire range.
                possibilities.append(maxes[idx] - mins[idx])
            else:
                # not end yet - append delta from the drop only.
                # the reason why we calculate "drop" instead of gain is the following:
                # an optimal range hasn't ended yet - but any range (start, end) that contains
                # idx can be split to two ranges:
                # (start, end) -> (start, idx + 1), (idx + 2, end)
                # and the difference between LHS and RHS for total gain is precisely
                # (prices[idx + 1] - prices[idx + 2])
                possibilities.append(prices[idx + 1] - prices[idx + 2])
        else: # last element - append delta for the entire range.
            possibilities.append(maxes[idx] - mins[idx])
    # Sort all possibilities, and select top K.
    # This makes the runtime to O(n log n)
    # However, this can be done in O(n) if quickselect is used to find the kth largest element
    # and filter all items greater than k.
    possibilities.sort(reverse = True)
    result = 0
    for i, p in enumerate(possibilities):
        if i < k: result += p
    return result

def partitioned_min_since(array, maxes):
    """
    similar to max_from, except:
    - it calculates minimums.
    - it gets reset whenever the max value changes.
    """
    v = None
    output = []
    for i, cur in enumerate(array):
        if v is None: v = cur
        else:
            if maxes[i] != maxes[i - 1]: v = cur
            else: v = min(v, cur)
        output.append(v)
    return output

def max_from(array):
    "result[i] = max(array[0]..array[i])"
    return list(reversed(partial_reduce(reversed(array), max)))

def partial_reduce(array, reduce_fn):
    """
    reduce a given array, but produces partial output for each iteration.
    """
    v = None
    output = []
    for cur in array:
        if v is None: v = cur
        else: v = reduce_fn(v, cur)
        output.append(v)
    return output


def normalize(prices):
    """
    1. reduce consecutive increase / decrease in prices to a single increase / decrease.
    ex: [1,2,3] -> [1,3], [1,2,3,4,3,2] -> [1,4,2]
    2. ensure that prices[0] < prices[1] - otherwise prices[0] will never belong in an optimal answer.
    3. ensure that prices[-2] < prices[-1] - otherwise prices[-1] will never belong in an optimal answer.
    """
    output = []
    for i, p in enumerate(prices):
        if i == 0 or i == len(prices) -1:
            output.append(p) # always append first & last.
            continue
        before = prices[i-1]
        after = prices[i+1]
        if not (before <= p <= after or before >= p >= after):
            output.append(p)
    if len(output) > 1 and output[0] > output[1]: del output[0]
    if len(output) > 1 and output[-2] > output[-1]: del output[-1]
    return output

def main():
    print buy_sell([], 1) == 0    # simple case 1
    print buy_sell([1], 1) == 0   # simple case 2
    print buy_sell([1,2], 1) == 1 # simple case 3
    print buy_sell([1,2,3,4,5], 1) == 4   # reduces to a single range.
    print buy_sell([1,2,1,2,1,2], 3) == 3 # repeating the simple case
    print buy_sell([1,2,1,2,1,2], 4) == 3 # more buy-sell pairs then we need.
    print buy_sell([1,5,2,3], 2) == 5     # double range 1~5 + 2~3 = 5
    print buy_sell([1,3,2,4], 2) == 4     # range splitting 1~3 + 2~4 = 4
    ary = [100,140,50,71,60,75,70,80]
    print buy_sell(ary, 1) == 40 # 100~140
    print buy_sell(ary, 2) == 70 # 100~140 + 50~80
    print buy_sell(ary, 3) == 81 # 100~140 + 50~71 + 60~80

main()
