# Determine best time to buy and sell a given item.
# input - time series [v_0, v_1 ... v_n]
# input - k >= 1 - # of times you can buy / sell. you cannot buy again if you've already bought one.
# output - total profit with k trades.

# The solution is O(n) given quickselect.
import pprint

DEBUG = False
class Node(object):
    def __init__(self, low, high):
        """
        tree of stock prices, constructed from the historic price.

        invariants:
        * low < high
        * low < child.low for all children

        tree is constructed greedily from the input. thus, recursively
        traversing the tree from left to right should yield the original input order.
        """
        assert low < high
        self.low = low
        self.high = high     # original high
        self.max_high = high # transitive high of node + all children.
        self.children = []
        self.profits = []    # list of possible profits for doing a single trade.

    def finish(self):
        """
        merges the stock price tree and generates list of profits.
        should be called after all children are appended.
        """
        for child in self.children:
            if child.max_high > self.max_high:
                # Price transitions A_low ~ A_high ~ B_low ~ B_high can be merged:
                # the optimal trade is A_low ~ B_high.
                # if an additional trade is permitted, then do split to two trades: A_low ~ A_high and B_low ~ B_high
                # For that case, additional profit is
                #   B_low - A_high
                # ex: reduce (5~8) ~ (7~10) to (5~10) and profit of 1
                self.profits.append(self.max_high - child.low)
                self.max_high = child.max_high
            else:
                # Price transitions are nested - i.e. A_low < B_low < B_high < A_high
                # the optimal trade is (A_low ~ A_high).
                # if an additional trade is permitted, then perform (B_low ~ B_high)
                # ex: reduce (0~10) ~ (1~9) to (0~10) and profit of 8
                self.profits.append(child.max_high - child.low)

    def to_profits(self):
        output = [self.max_high - self.low]
        self._append_profits(output)
        return output

    def _append_profits(self, output):
        output += self.profits
        for child in self.children:
            child._append_profits(output)

    def to_dict(self):
        "useful for debugging. prefixed to hack python dictionary ordering."
        return {
            '1-range': (self.low, self.high),
            '2-profits': self.profits,
            '3-children': tuple(child.to_dict() for child in self.children)
        }

def buy_sell(raw_prices, k):
    prices = normalize(raw_prices)
    if len(prices) <= 1: return 0
    stack = []; roots = []
    for idx in xrange(0, len(prices), 2):
        low, high = prices[idx], prices[idx + 1]
        node = Node(low, high)
        while stack and stack[-1].low > node.low:
            popped = stack.pop()
            popped.finish()
        # append to the latest node as a child, or append as a root.
        if stack: stack[-1].children.append(node)
        else: roots.append(node)
        stack.append(node)
    while stack:
        popped = stack.pop()
        popped.finish()
    possibilities = []
    for root in roots:
        possibilities += root.to_profits()
    if DEBUG:
        print "Prices: %s" % prices
        pprint.pprint([root.to_dict() for root in roots])
    possibilities.sort(reverse = True)
    result = 0
    for i, p in enumerate(possibilities):
        if i < k: result += p
    return result

def normalize(prices):
    """
    1. reduce consecutive increase / decrease in prices to a single increase / decrease.
    ex: [1,2,3] -> [1,3], [1,2,3,4,3,2] -> [1,4,2]
    2. ensure that prices[0] < prices[1] - otherwise prices[0] will never belong in an optimal answer.
    3. ensure that prices[-2] < prices[-1] - otherwise prices[-1] will never belong in an optimal answer.

    output is:
    * even number of elements.
    * alternates between decreasing and increasing.
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
    assert buy_sell([], 1) == 0    # simple case 1
    assert buy_sell([1], 1) == 0   # simple case 2
    assert buy_sell([1,2], 1) == 1 # simple case 3
    assert buy_sell([1,2,3,4,5], 1) == 4   # reduces to a single range.
    assert buy_sell([1,2,1,2,1,2], 3) == 3 # repeating the simple case
    assert buy_sell([1,2,1,2,1,2], 4) == 3 # more buy-sell pairs then we need.
    assert buy_sell([1,5,2,3], 2) == 5     # double range 1~5 + 2~3 = 5
    assert buy_sell([1,3,2,4], 2) == 4     # range splitting 1~3 + 2~4 = 4
    ary = [100,140,50,71,60,75,70,80]
    assert buy_sell(ary, 1) == 40 # 100~140
    assert buy_sell(ary, 2) == 70 # 100~140 + 50~80
    assert buy_sell(ary, 3) == 81 # 100~140 + 50~71 + 60~80
    assert buy_sell([0,60,40,90,15,85,10,60,50,100], 1) == 100 # 0~100
    assert buy_sell([0,60,40,90,15,85,10,60,50,100], 2) == 180 # 0~90 10~100
    assert buy_sell([0,60,40,90,15,85,10,60,50,100], 3) == 250 # 0~90 15~85 10~100

main()
