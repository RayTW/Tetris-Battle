package tetris;

import org.junit.Test;

/**
 * 測試.
 *
 * @author ray
 */
public class LibraryTest {
  @Test
  public void testSomeLibraryMethod() {
    toArray("[[2, 0, 0], [2, 2, 2]]");
  }

  private int[][] toArray(String temp) {
    String[] ary = temp.split("],");
    String[] inner = null;
    int[][] result = new int[ary.length][];

    for (int i = 0; i < ary.length; i++) {
      ary[i] = ary[i].replace("[", "").replace("]", "");
      inner = ary[i].split("[,]");
      result[i] = new int[inner.length];
      for (int j = 0; j < inner.length; j++) {
        result[i][j] = Integer.parseInt(inner[j].trim());
      }
    }
    return result;
  }
}
