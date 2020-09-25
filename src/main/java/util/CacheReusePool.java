package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Ray Lee Created on 2017/10/18
 * @param <T>
 */
public class CacheReusePool<T> {
  private List<T> objects;
  private int index;

  public CacheReusePool(int size, Callable<T> generatorObj) throws Exception {
    objects = new ArrayList<T>(size);

    for (int i = 0; i < size; i++) {
      objects.add(generatorObj.call());
    }
  }

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
