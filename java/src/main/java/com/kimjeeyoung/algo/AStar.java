package com.kimjeeyoung.algo;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Implementation of A* Algorithm on 2-d grid.
 */
public class AStar {
  public final static class Coordinate {
    final int row;
    final int col;

    public Coordinate(int row, int col) {
      this.row = row;
      this.col = col;
    }

    public Coordinate add(int row, int col) {
      return new Coordinate(this.row + row, this.col + col);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Coordinate that = (Coordinate) o;
      return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
      return Objects.hash(row, col);
    }

    @Override
    public String toString() {
      return String.format("(%d,%d)", row, col);
    }
  }

  public static class CoordValue implements Comparable<CoordValue> {
    Coordinate coord;
    /**
     * heuristic estimate to the goal.
     */
    final int heuristic;

    /**
     * real value from "start" to this coordinate.
     */
    final int value;

    public CoordValue(Coordinate coord, int heuristic, int value) {
      this.coord = coord;
      this.value = value;
      this.heuristic = heuristic;
    }

    @Override
    public int compareTo(CoordValue o) {
      // A* picks the next point by finding the next smallest
      // (real value + heuristic value)
      return Integer.compare(heuristic + value, o.heuristic + o.value);
    }
  }

  public final static class ShortestHistory {
    private int distance;
    private Coordinate previous;

    public ShortestHistory() {
    }

    public ShortestHistory(int distance) {
      this.distance = distance;
    }

    @Override
    public String toString() {
      return "ShortestHistory{" +
        "distance=" + distance +
        ", previous=" + previous +
        '}';
    }
  }

  private final Board board;
  private final Coordinate start;
  private final Coordinate end;
  private boolean executed = false;
  private int result = 0;
  @VisibleForTesting
  final Map<Coordinate, ShortestHistory> shortestDistance;

  public AStar(Coordinate start, Coordinate end, Board board) {
    this.board = board;
    this.start = start;
    this.end = end;
    shortestDistance = new HashMap<>();
  }

  /**
   * Execute the A* algorithm on top of data.
   */
  public int execute() {
    if (!executed) {
      result = executeImpl();
      executed = true;
    }
    return result;
  }

  public List<Coordinate> getPath() {
    if (!executed) {
      execute();
    }
    if (result != -1) {
      List<Coordinate> result = new ArrayList<>();
      Coordinate coord = end;
      result.add(coord);
      do {
        ShortestHistory prev = shortestDistance.get(coord);
        coord = prev.previous;
        result.add(coord);
      } while (!coord.equals(start));
      Collections.reverse(result);
      return result;
    } else {
      return Collections.emptyList();
    }
  }

  private int executeImpl() {
    PriorityQueue<CoordValue> queue = new PriorityQueue<>();
    queue.add(new CoordValue(start, heuristic(start), 0));
    shortestDistance.put(start, new ShortestHistory(0));
    int rows[] = {0, 1, 0, -1};
    int cols[] = {1, 0, -1, 0};
    while (!queue.isEmpty()) {
      CoordValue elem = queue.poll();
      int newValue = elem.value + 1;
      for (int i = 0; i < 4; i++) {
        Coordinate newCoord = elem.coord.add(rows[i], cols[i]);
        ShortestHistory shortestHistory = shortestDistance.get(newCoord);
        if (shortestHistory == null || shortestHistory.distance > newValue) {
          if (!board.blocked(newCoord.row, newCoord.col)) {
            if (shortestHistory == null) {
              shortestHistory = new ShortestHistory();
              shortestDistance.put(newCoord, shortestHistory);
            }
            shortestHistory.distance = newValue;
            shortestHistory.previous = elem.coord;
            if (newCoord.equals(end)) {
              return newValue;
            }
            queue.add(new CoordValue(newCoord, heuristic(newCoord), newValue));
          }
        }
      }
    }
    return -1; // no result.
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    Set<Coordinate> path = new HashSet<>(getPath());
    for (int col = 0; col < board.width; col++) {
      for (int row = 0; row < board.height; row++) {
        Coordinate coord = new Coordinate(row, col);
        char toAppend;
        if (coord.equals(start)) {
          toAppend = 'X';
        } else if (coord.equals(end)) {
          toAppend = 'X';
        } else if (board.blocked(row, col)) {
          toAppend = 'O';
        } else if (path.contains(coord)) {
          toAppend = '*';
        } else {
          toAppend = '.';
        }
        sb.append(toAppend);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * A* heuristic function.
   */
  public int heuristic(Coordinate coord) {
    return Math.abs(coord.row - end.row) + Math.abs(coord.col - end.col);
  }
}
