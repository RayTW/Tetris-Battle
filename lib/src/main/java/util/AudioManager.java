package util;

import java.util.List;
import tetris.Config;

public class AudioManager {
  private static AudioManager instance = new AudioManager();

  private AudioManager() {}

  public static AudioManager get() {
    return instance;
  }

  /**
   * 預戴音樂
   *
   * @param listener
   * @param path
   */
  public void preload(List<String> path, OnPreloadListener listener) {
    path.forEach(
        p -> {
          if (listener != null) {
            listener.onLoaded(p);
          }
          AudioPlayer audio = new AudioPlayer();
          audio.loadAudio(p, AudioManager.this);
          audio.stop();
        });

    if (listener != null) {
      listener.onCompleted();
    }
  }

  public AudioPlayer playMusic(String path) {
    return playAudio(path, 0, 1);
  }

  public AudioPlayer playSound(String path) {
    return playAudio(path, 1, Config.get().getSoundCacheCount());
  }

  public AudioPlayer playAudio(String path, int playCount, int cacheCount) {
    AudioPlayer audio = new AudioPlayer();
    path = "/" + path;
    audio.loadAudio(path, this);
    audio.setCacheCount(cacheCount);

    audio.setPlayCount(playCount); // 播放次數
    audio.play();
    return audio;
  }

  public static interface OnPreloadListener {
    public void onLoaded(String path);

    public void onCompleted();
  }
}
