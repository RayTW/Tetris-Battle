package tetris.view;

import tetris.GameEvent;

public interface ViewDelegate {
	public abstract void tetrisEvent(GameEvent code, String data);
}
