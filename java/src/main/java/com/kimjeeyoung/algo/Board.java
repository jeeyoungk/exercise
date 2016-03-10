package com.kimjeeyoung.algo;

public final class Board {
  public final int width;
  public final int height;
  public final boolean[][] board;

  public Board(int width, int height) {
    this.width = width;
    this.height = height;
    this.board = new boolean[height][width];
  }

  public boolean blocked(int row, int col) {
    return row >= this.height || col >= this.width || row < 0 || col < 0 || board[row][col];
  }
}
