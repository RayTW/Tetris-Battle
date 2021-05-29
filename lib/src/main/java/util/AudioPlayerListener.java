package util;

/**
 * 播放事件.
 *
 * @author ray
 */
public interface AudioPlayerListener {
  /**
   * 音樂播放完畢時會執行.
   *
   * @param object 物件
   */
  public abstract void onAudioPlayed(Object object);
}
