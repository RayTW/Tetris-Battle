package util;

public interface AudioPlayerListener {
  /**
   * 音樂播放完畢時會執行
   *
   * @param object
   */
  public abstract void onAudioPlayed(Object object);
}
