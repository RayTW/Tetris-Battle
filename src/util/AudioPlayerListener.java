package util;

public interface AudioPlayerListener {
  /**
   * 音樂播放完畢時會執行audioPlayEnd
   *
   * @param object
   */
  public abstract void onAudioPlayEnd(Object object);
}
