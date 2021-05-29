package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 緩存重用.
 *
 * @author ray
 */
public class CacheReusePool<T> {
  private List<T> objects;
  private int index;

  /**
   * 初始化.
   *
   * @param size 個數
   * @param generatorObj 物件
   * @throws Exception 例外
   */
  public CacheReusePool(int size, Callable<T> generatorObj) throws Exception {
    objects = new ArrayList<T>(size);

    for (int i = 0; i < size; i++) {
      objects.add(generatorObj.call());
    }
  }

  /** 取得下一個元素. */
  public T next() {
    int index = 0;
    synchronized (objects) {
      index = this.index;
      this.index++;
      if (this.index >= objects.size()) {
        this.index = 0;
      }
    }
    return objects.get(index);
  }

  public int size() {
    return objects.size();
  }
}
