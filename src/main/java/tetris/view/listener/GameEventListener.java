package tetris.view.listener;

import tetris.game.GameEvent;

public interface GameEventListener {
  public abstract void onEvent(GameEvent code, Object data);
}
