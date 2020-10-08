package util;

import java.util.Timer;
import java.util.TimerTask;

public class FpsCounter extends TimerTask {
  private final Timer resetTimer;
  private int current, last;

  public FpsCounter() {
    resetTimer = new Timer();
  }

  public synchronized void start() {
    resetTimer.schedule(this, 0, 1000);
    current = 0;
    last = -1;
  }

  public synchronized void stop() {
    resetTimer.cancel();
    current = -1;
  }

  public synchronized void frame() {
    ++current;
  }

  public synchronized int get() {
    return last;
  }

  @Override
  public void run() {
    last = current;
    current = 0;
  }
}
