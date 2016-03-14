# Data Structures & common logic
from enum import Enum
import heapq
import matplotlib.pyplot as plt

DIMENSION = 16 # size of the board
DIRX = [0, 0, -1, 1] # directional vectors
DIRY = [1, -1, 0, 0] # color vectors
COLORS = ['red','blue','green','purple']
MAX_DEPTH = 30

class Direction(Enum):
    UP, DOWN, LEFT, RIGHT = range(4)
    def reverse(self):
        if self == Direction.UP:
            return Direction.DOWN
        elif self == Direction.DOWN:
            return Direction.UP
        elif self == Direction.LEFT:
            return Direction.RIGHT
        elif self == Direction.RIGHT:
            return Direction.LEFT
        return None

class Color(Enum):
    RED, BLUE, GREEN, PURPLE = range(4)

class Board(object):
    def __init__(self):
        # note: bottom left of grid is 0, 0
        self.walls = set() # list of walls - normalized to (x, y, (DOWN|LEFT))
        
    def add_wall(self, x, y, direction):
        """Add a wall to the current position"""
        self.walls.add(normalize_wall(x, y, direction))
        
    def has_wall(self, x, y, direction):
        """Determine whether there's a wall in the given position."""
        return normalize_wall(x, y, direction) in self.walls

class Stat(object):
    def __init__(self):
        self.iteration = 0
        self.distance = -1
        
    def __repr__(self):
        return repr(self.__dict__)
        
def normalize_wall(x, y, direction):
    '''walls are normalized to "down" or "left".'''
    if direction == Direction.UP:
        direction = Direction.DOWN
        y += 1
    elif direction == Direction.RIGHT:
        direction = Direction.LEFT
        x += 1
    return (x, y, direction)

def compute_delta(robots1, robots2):
    '''
    computes delta between two positioning of robots. Assume that exactly one robot is moved.
    return (color, (x, y), (x, y))
    note: this logic is used to construct robot paths.
    '''
    for idx in range(len(COLORS)):
        if robots1[idx] != robots2[idx]:
            return (idx, robots1[idx], robots2[idx])
    assert False, "same positions given"

def next_moves_single(board, robot_index, robots):
    """Generate list of next moves by moving a single robot given by the index."""
    def generate(index, replaced_robot):
        return tuple((replaced_robot if i == index else r) for (i, r) in enumerate(robots))
    robot = robots[robot_index]
    for direction in Direction:
        moved = False
        (x, y) = robot
        while True:
            newx = x + DIRX[direction.value]
            newy = y + DIRY[direction.value]
            # stops when a wall or another robot is encountered.
            if board.has_wall(x, y, direction) or (newx, newy) in robots:
                if moved: yield generate(robot_index, (x, y))
                break
            moved = True
            x = newx
            y = newy
                
def next_moves_all(board, robots):
    """Generate list of next moves by moving a single robot."""
    for index in range(len(robots)):
        for move in next_moves_single(board, index, robots):
            assert move is not None
            yield move

def prev_position(board, obstacles, start, magic_stop=False):
    for direction in Direction:
        (x, y) = start
        reverse = direction.reverse()
        prevx = x + DIRX[reverse.value]
        prevy = y + DIRY[reverse.value]
        if not magic_stop and not (board.has_wall(x, y, reverse) or (prevx, prevy) in obstacles):
            continue # Cannot reach here.
        moved = False
        while True:
            newx = x + DIRX[direction.value]
            newy = y + DIRY[direction.value]
            if board.has_wall(x, y, direction) or (newx, newy) in obstacles:
                break
            yield (newx, newy)
            x = newx
            y = newy
                
def astar(
    start,
    neighbour,
    finish_condition,
    heuristic=None,
    stat=None):
    """
    Perform an A* search.
    finish_condition = (position) -> bool
    neighbour - neibhbourhood generation function
    heuristic = A* heuristic function. (new position, old position) -> distance
    """
    queue = [] # contains (distance+heuristic, distance, position)
    heapq.heappush(queue, (0, 0, start, None))
    history = {start: (0, None)} # position -> (distance, previous)
    visited = set()
    if not stat: stat = Stat()
    if not heuristic: heuristic = lambda new, old: 0
    while queue:
        stat.iteration += 1
        _, distance, position, prev_position = heapq.heappop(queue)
        if distance > MAX_DEPTH: return
        if finish_condition(position):
            # found a solution!
            positions = [position, prev_position]
            cur_position = prev_position
            while cur_position in history:
                cur_position = history[cur_position][1]
                if cur_position is not None:
                    positions.append(cur_position)
            stat.distance = distance
            return positions
        if position in visited: continue
        visited.add(position)
        new_distance = distance + 1
        for new_position in neighbour(position):
            if new_position in history and new_distance > history[new_position][0]: continue
            history[new_position] = (new_distance, position)
            heapq.heappush(queue, (new_distance + heuristic(position, new_position), new_distance, new_position, position))

def compute_all(start, neighbour):
    """
    Compute shortest distance from "start" to all reachable node.

    Note: This function should only be executed with relatively small graph.
    """
    queue = []
    # contains (distance, position, old_position)
    heapq.heappush(queue, (0, start))
    history = {start: (0, None)} # position -> (distance, previous)
    visited = set()
    while queue:
        distance, position = heapq.heappop(queue)
        if position in visited: continue
        visited.add(position)
        new_distance = distance + 1
        for new_position in neighbour(position):
            if new_position in history and new_distance > history[new_position][0]: continue
            history[new_position] = (new_distance, position)
            heapq.heappush(queue, (new_distance, new_position))
    return history
        
def print_board(board,
    robots=None,
    paths=None,
    additionals=None,
    labels=None,
    markers=None):
    '''
    Print the given board position.
    robots - 4-tuple of pair (x, y), representing red, blue, green, and yellow robots.
    paths - list of (color, (x, y), (x, y)) paths to draw.
    additionals - list of (color, (x, y)) points to draw.
    labels - list of labels to render.
    '''
    plt.figure(figsize=(5, 5))
    axis = plt.gca()
    MARGIN = 0.1
    PADDING = 0.5
    def plot_robot(index, coord, size):
        (x, y) = coord
        circle = plt.Circle((x + 0.5, y + 0.5), size, fc=COLORS[i])
        axis.add_patch(circle)
    def render_wall(wall):
        (x1, y1, direction) = wall
        if direction == Direction.DOWN:
            x2 = x1 + 1
            y2 = y1
        else:
            x2 = x1
            y2 = y1 + 1
        line = plt.Line2D((x1, x2), (y1, y2), lw=2.5, color='black')
        axis.add_line(line)
    def render_path(path):
        (i, pos1, pos2) = path
        line = plt.Line2D(
            (pos1[0] + 0.5, pos2[0] + 0.5),
            (pos1[1] + 0.5, pos2[1] + 0.5),
            color=COLORS[i],
            marker='x')
        axis.add_line(line)
    def render_marker(marker):
        (color, coord) = marker
        (x, y) = coord
        rectangle = plt.Rectangle((x + MARGIN, y + MARGIN),
                                  1 - MARGIN * 2,
                                  1 - MARGIN * 2,
                                  fc=COLORS[color])
        axis.add_patch(rectangle)

    for wall in board.walls: render_wall(wall)
    for path in (paths or []): render_path(path)
    for marker in (markers or []): render_marker(marker)
    for additional in (additionals or []):
        (i, robot) = additional
        plot_robot(i, robot, 0.1)
    if robots is not None:
        for i in range(len(COLORS)):
            plot_robot(i, robots[i], 0.4)
    if labels is not None:
        for row_idx, row in enumerate(labels):
            for col_idx, cell in enumerate(row):
                axis.text(col_idx + 0.5,
                        row_idx + 0.5,
                        cell,
                        verticalalignment='center',
                        horizontalalignment='center')
    plt.xlim(0 - PADDING, DIMENSION + PADDING)
    plt.ylim(0 - PADDING, DIMENSION + PADDING)
    plt.show()

