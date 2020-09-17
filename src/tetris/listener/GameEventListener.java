package tetris.listener;

import tetris.game.GameEvent;

public interface GameEventListener {
  public abstract void onEvent(GameEvent code, String data);
}
