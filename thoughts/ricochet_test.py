import unittest

import ricochet
from ricochet import Board, Direction, DIMENSION

# sample input
board = Board()
for i in range(DIMENSION):
    board.add_wall(0, i, Direction.LEFT)
    board.add_wall(i, 0, Direction.DOWN)
    board.add_wall(DIMENSION, i, Direction.LEFT)
    board.add_wall(i, DIMENSION, Direction.DOWN)
walls = [
  (0, 5, Direction.DOWN), (0, 9, Direction.DOWN),
  (1, 12, Direction.RIGHT), (1, 12, Direction.UP), (1, 2, Direction.DOWN), (1, 2, Direction.LEFT),
  (2, 10, Direction.DOWN), (2, 10, Direction.RIGHT),
  (3, 6, Direction.DOWN), (3, 6, Direction.RIGHT),
  (4, 15, Direction.LEFT),
  (5, 0, Direction.LEFT), (5, 11, Direction.LEFT), (5, 11, Direction.UP), (5, 4, Direction.LEFT), (5, 4, Direction.UP),
  (6, 1, Direction.RIGHT), (6, 1, Direction.UP), (6, 14, Direction.DOWN), (6, 14, Direction.LEFT),
  (7, 10, Direction.DOWN), (7, 10, Direction.RIGHT), (7, 7, Direction.DOWN), (7, 7, Direction.LEFT), (7, 8, Direction.LEFT), (7, 8, Direction.UP),
  (8, 7, Direction.DOWN), (8, 7, Direction.RIGHT), (8, 8, Direction.RIGHT), (8, 8, Direction.UP),
  (9, 3, Direction.LEFT), (9, 3, Direction.UP),
  (10, 15, Direction.LEFT), (10, 8, Direction.RIGHT), (10, 8, Direction.UP),
  (11, 1, Direction.DOWN), (11, 1, Direction.RIGHT), (11, 13, Direction.DOWN), (11, 13, Direction.LEFT),
  (12, 6, Direction.RIGHT), (12, 6, Direction.UP),
  (13, 9, Direction.DOWN), (13, 9, Direction.RIGHT),
  (14, 0, Direction.LEFT), (14, 14, Direction.LEFT), (14, 14, Direction.UP), (14, 2, Direction.DOWN), (14, 2, Direction.LEFT),
  (15, 12, Direction.DOWN), (15, 6, Direction.DOWN),
]

for wall in walls:
    board.add_wall(*wall)

robots = ((0, 1), (4, 1), (7, 14), (12, 4))

class RicochetTest(unittest.TestCase):
    def test_prev_position(self):
        self.assertEqual(
                set(ricochet.prev_position(board, set(), (1, 1))),
                {(1, 0)},
        )
        self.assertEqual(
                set(ricochet.prev_position(board, set(), (0, 0))),
                {(0,1),(0,2),(0,3),(0,4),(1,0),(2,0),(3,0),(4,0)},
        )
        self.assertEqual(
                set(ricochet.prev_position(board, {(1, 0)}, (0, 0))),
                {(0,1),(0,2),(0,3),(0,4)},
        )
        self.assertEqual(
                set(ricochet.prev_position(board, {(2, 0)}, (0, 0))),
                {(0,1),(0,2),(0,3),(0,4),(1,0)},
        )
    def test_compute_all(self):
        def neighbour(coord):
            return ricochet.prev_position(board, set(), coord)
        history = ricochet.compute_all((0, 0), neighbour)
        self.assertEqual(len(history), 252)

if __name__ == '__main__':
    unittest.main()
