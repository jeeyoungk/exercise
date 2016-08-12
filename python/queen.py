# 8 queens puzzle.
import itertools
import random

class Board(object):
    def __init__(self, rows = 8, cols = 8):
        self.placed = set()
        self.rows = rows
        self.cols = cols

    def place(self, row, col):
        "place a chess piece."
        if not self.can_place(row, col): raise RuntimeError("Cannot place")
        self.placed.add((row, col))

    def unplace(self, row, col):
        "un-place a chess piece."
        if (row, col) not in self.placed: raise RuntimeError("Piece does not exist")
        self.placed.remove((row, col))

    def can_place(self, row, col):
        if not (0 <= row < self.rows and 0 <= col < self.cols):
            return False # outside the range
        for placed_row, placed_col in self.placed:
            if placed_col == col: return False
            if placed_row == row: return False
            if abs(placed_row - row) == abs(placed_col - col): return False
        return True

    def __str__(self):
        output = ""
        for row in xrange(self.rows):
            for col in xrange(self.cols):
                if (row, col) in self.placed:
                    output += "X"
                else:
                    output += "."
            output += "\n"
        return output

def seq(board):
    # sequential navigation strategy
    return itertools.product(xrange(board.rows), xrange(board.cols))

def rand(board):
    # random navigation strategy
    x = list(itertools.product(range(board.rows), range(board.cols)))
    random.shuffle(x)
    return x

def rand_partial(num):
    def generator(board):
        # random navigation strategy
        x = []
        for row, col in itertools.product(range(board.rows), range(board.cols)):
            if board.can_place(row, col):
                x.append((row, col))
        random.shuffle(x)
        return x[:num]
    return generator

def attempt(board, goal, strategy = seq, stat = None):
    """
    attempt placing a queen to the board.
    board - a chess board being worked on.
    goal - # of queens we want to place on this board.
    strategy - queen placing strategy.
    stat - statistics related to the current run.
    """
    if stat is not None:
        stat['count'] += 1
    for row, col in strategy(board):
        if board.can_place(row, col):
            board.place(row, col)
            if len(board.placed) >= goal:
                return board
            found = attempt(board, goal, strategy, stat)
            if found: return found
            board.unplace(row, col)
    return None

def main():
    def test_run(board, goal, strategy):
        stat = {'count': 0}
        found = attempt(board, goal, strategy, stat)
        print stat['count']
        print board
    def load_test(title, size, strategy, runs):
        print '> %s' % title
        count = 0
        found = 0
        for run in xrange(runs):
            stat = {'count': 0}
            success = attempt(Board(size, size), size, strategy, stat)
            count += stat['count']
            if success:
                found += 1
        print '  count: %d' % (count / runs)
        print '  failures: %d' % (runs - found)

    # test_run(Board(8, 8), 8, seq)
    # test_run(Board(8, 8), 8, rand)
    # test_run(Board(10, 10), 10, rand)
    # print 'rand'
    # load_test(8, rand, 10)
    print ">> Board size 8"
    # load_test('rand', 8, rand, 10)
    load_test('rand_partial(1)', 8, rand_partial(5), 10)
    load_test('rand_partial(5)', 8, rand_partial(5), 10)
    load_test('rand_partial(20)', 8, rand_partial(20), 10)
    print ">> Board size 10"
    load_test('rand_partial(3)', 10, rand_partial(20), 10)

main()
