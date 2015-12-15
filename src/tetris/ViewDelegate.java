package tetris;

public interface ViewDelegate {
	public abstract void tetrisEvent(GameEvent code, String data);
}
