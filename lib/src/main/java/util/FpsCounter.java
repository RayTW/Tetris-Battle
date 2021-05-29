package util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * fps計數器.
 *
 * @author ray
 */
public class FpsCounter extends TimerTask {
  private final Timer resetTimer;
  private int current;
  private int last;

  public FpsCounter() {
    resetTimer = new Timer();
  }

  /** 啟動. */
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
