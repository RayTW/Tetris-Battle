package tetris.view;

import tetris.GameEvent;

public interface EventListener {
  public abstract void onEvent(GameEvent code, String data);
}
