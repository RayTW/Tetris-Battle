package tetris.game;

import tetris.Config;
import util.AudioPlayer;

public class AudioManager {
  private static AudioManager instance = new AudioManager();

  private AudioManager() {}

  public static AudioManager get() {
    return instance;
  }

  /**
   * 預戴音樂
   *
   * @param completed
   * @param path
   */
  public void preloadMusic(Runnable completed, String... path) {
    new Thread(
            () -> {
              for (String p : path) {
                AudioPlayer audio = new AudioPlayer();
                audio.loadAudio("/" + p, this);
                audio.stop();
              }
              if (completed != null) {
                completed.run();
              }
            })
        .start();
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
}
