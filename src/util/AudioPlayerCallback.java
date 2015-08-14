package util;

public interface AudioPlayerCallback {
	/**
	 * 音樂播放完畢時會執行audioPlayEnd
	 * 
	 * @param callbackObj
	 */
	public abstract void audioPlayEnd(Object callbackObj);
}
