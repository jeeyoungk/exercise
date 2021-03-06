package com.kimjeeyoung.algo;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AStarTest {
  private static final boolean PRINT_BOARD = false;
  private Board board;
  private AStar astar;

  @After
  public void printBoard() {
    if (PRINT_BOARD) {
      System.err.println(astar);
    }
  }

  @Test
  public void testExecuteLongBoard() throws Exception {
    board = new Board(10, 20);
    astar = new AStar(new AStar.Coordinate(0, 0), new AStar.Coordinate(19, 9), board);
    assertEquals(28, astar.execute());
    assertEquals(164, astar.shortestDistance.size());
  }

  @Test
  public void testExecuteSingleStep() throws Exception {
    board = new Board(10, 10);
    astar = new AStar(new AStar.Coordinate(0, 0), new AStar.Coordinate(1, 1), board);
    assertEquals(2, astar.execute());
    assertEquals(5, astar.shortestDistance.size());
  }

  @Test
  public void testExecuteObstacle() throws Exception {
    board = new Board(3, 3);
    board.board[0][1] = true;
    board.board[1][1] = true;
    astar = new AStar(new AStar.Coordinate(0, 0), new AStar.Coordinate(0, 2), board);
    assertEquals(6, astar.execute());
    assertEquals(7, astar.shortestDistance.size());
  }

  @Test
  public void testPath() throws Exception {
    board = new Board(21, 21);
    board.board[10][10] = true;
    astar = new AStar(new AStar.Coordinate(10, 2), new AStar.Coordinate(10, 18), board);
    assertEquals(18, astar.execute());
    assertEquals(86, astar.shortestDistance.size());
  }

  @Test
  public void testWall() throws Exception {
    board = new Board(21, 21);
    for (int i = 3; i < 21; i++) {
      board.board[i][10] = true;
    }
    for (int i = 0; i < 21 - 3; i++) {
      board.board[i][12] = true;
    }
    for (int i = 3; i < 21; i++) {
      board.board[i][14] = true;
    }
    astar = new AStar(new AStar.Coordinate(10, 2), new AStar.Coordinate(10, 18), board);
    assertEquals(64, astar.execute());
  }

  @Test
  public void testBlocked() throws Exception {
    board = new Board(21, 21);
    for (int i = 0; i < 21; i++) {
      board.board[i][10] = true;
    }
    astar = new AStar(new AStar.Coordinate(10, 2), new AStar.Coordinate(10, 18), board);
    assertEquals(-1, astar.execute());
  }
}
