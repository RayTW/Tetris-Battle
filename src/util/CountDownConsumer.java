package util;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class CountDownConsumer<T> {
  private CountDownLatch latch;
  private T object;
  private Consumer<T> consumer;
  private boolean isStop;

  public CountDownConsumer() {
    isStop = false;
  }

  public void setConsumer(Consumer<T> consumer) {
    this.consumer = consumer;
  }

  public void set(T obj) {
    object = obj;
  }

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
        consumer.accept(object);
        if (!isStop) {
          CountDownConsumer.this.start();
        }
      }
    }.start();
  }

  public void countDown() {
    latch.countDown();
  }

  public void stop() {
    isStop = true;
    while (latch.getCount() > 0) {
      latch.countDown();
    }
  }
}
