package util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 多緒條件達成後會回傳事件.
 *
 * @author ray
 */
public class CountDownConsumer<T> {
  private CountDownLatch latch;
  private T object;
  private Consumer<T> consumer;
  private AtomicBoolean isStop;

  public CountDownConsumer() {
    isStop = new AtomicBoolean(false);
  }

  public void setConsumer(Consumer<T> consumer) {
    this.consumer = consumer;
  }

  public void set(T obj) {
    object = obj;
  }

  /** 啟動. */
  public void start() {
    latch = new CountDownLatch(1);
    new Thread() {
      @Override
      public void run() {
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (!isStop.get()) {
          consumer.accept(object);
          CountDownConsumer.this.start();
        }
      }
    }.start();
  }

  public void countDown() {
    latch.countDown();
  }

  /** 停止. */
  public void stop() {
    isStop.set(true);
    while (latch.getCount() > 0) {
      latch.countDown();
    }
  }
}
