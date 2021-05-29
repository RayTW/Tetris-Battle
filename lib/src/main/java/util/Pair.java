package util;

/**
 * @author Ray Lee Created on 2017/10/18
 * @param <F>
 * @param <S>
 */
public class Pair<F, S> {
  private F first;
  private S second;

  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public void setFirst(F first) {
    this.first = first;
  }

  public void setSecond(S second) {
    this.second = second;
  }

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }
}
