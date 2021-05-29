package tetris.view.listener;

import tetris.game.GameEvent;

/**
 * 遊戲事件.
 *
 * @author ray
 */
public interface GameEventListener {
  public abstract void onEvent(GameEvent code, Object data);
}
