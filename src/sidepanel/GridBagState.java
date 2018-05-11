package sidepanel;

class GridBagState {
  private int x;
  private int y;

  GridBagState() {
    this.x = 2;
    this.y = 0;
  }

  public int getX() {
    return x;
  }
  public int getY() {
    return y;
  }

  public int increaseY() {
    return ++y;
  }
}
