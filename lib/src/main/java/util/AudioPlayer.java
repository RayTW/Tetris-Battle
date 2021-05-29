package util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * 播放音樂(支援WAV, AIFF, AU) 2011/10/09.
 *
 * <p>2012-12-08 <br>
 * 1.增加播放結束時callback <br>
 * 2.修正bug: 無限次播放時，無法stop()<br>
 * 2017-10-18<br>
 * 1.[bug]修正不斷播重建物件進行播放會造成卡頓<br>
 * 2.移除左、右聲道切換功能<br>
 * 3.移除close()關閉音檔功能<br>
 *
 * @author Ray
 */
public class AudioPlayer {
  private static ConcurrentHashMap<String, CacheReusePool<Pair<AudioInputStream, Clip>>>
      cacheAudioClip =
          new ConcurrentHashMap<String, CacheReusePool<Pair<AudioInputStream, Clip>>>();

  private Clip clip;

  private float gain;
  private FloatControl gainControl;

  // 控制靜音 開/關
  private boolean mute;
  private BooleanControl muteControl;

  // 播放次數,小於等於0:無限次播放,大於0:播放次數
  private int playCount;
  private int cacheCount;

  // 音樂播放完畢時，若有設定回call的對象，則會通知此對象
  private AudioPlayerListener callbackTartet;
  private Object callbackObj;
  private boolean isPause;

  /** 初始化. */
  public AudioPlayer() {
    clip = null;
    gain = 0.5f;
    gainControl = null;
    mute = false;
    muteControl = null;
    playCount = 0;
    cacheCount = 20;
    isPause = false;
  }

  /**
   * 設定每個音檔cache的數量，若過少又要reuse聲音的話會發生播音馬上被停止.
   *
   * @param count 次數
   */
  public void setCacheCount(int count) {
    cacheCount = count;
  }

  /**
   * 設定要接收音樂播放完時事件的對象.
   *
   * @param cb 接收callback的對象
   * @param obj callback回來的物件
   */
  public void setCallbackTartet(AudioPlayerListener cb, Object obj) {
    callbackTartet = cb;
    callbackObj = obj;
  }

  /**
   * 設定播放次數,播放次數,小於等於0:無限次播放,大於0:播放次數.
   *
   * @param c 次數
   */
  public void setPlayCount(int c) {
    if (c < -1) {
      c = -1;
    }
    playCount = c - 1;
  }

  /**
   * 指定路徑讀取音檔,回傳true:播放成功,false:播放失敗.
   *
   * @param filePath 目前物件放置的package路徑
   */
  public boolean loadAudio(String filePath) {
    try {
      loadAudio(new File(filePath));
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 指定路徑讀取音檔,使用目前物件放置的package當相對路徑root,null時不使用物件路徑為root.
   *
   * @param filePath 目前物件放置的package路徑
   * @param obj 資料
   * @return 回傳true:播放成功,false:播放失敗
   */
  public boolean loadAudio(final String filePath, final Object obj) {
    try {
      if (obj != null) {
        doLoadingAudio(
            filePath,
            new Callable<URL>() {

              @Override
              public URL call() throws Exception {
                return obj.getClass().getResource(filePath);
              }
            });
      } else {
        loadAudio(new File(filePath));
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /** 從遠端讀取音檔./ */
  public void loadAudio(final URL url) throws Exception {
    doLoadingAudio(
        url.toString(),
        new Callable<URL>() {

          @Override
          public URL call() throws Exception {
            return url;
          }
        });
  }

  /**
   * 讀取本地端音檔.
   *
   * @param file 檔案
   * @throws Exception 例外
   */
  public void loadAudio(final File file) throws Exception {
    doLoadingAudio(
        file.getPath(),
        new Callable<File>() {

          @Override
          public File call() throws Exception {
            return file;
          }
        });
  }

  /** load完音檔後，進行播放設定. */
  private <T> void doLoadingAudio(String key, final Callable<T> callable) throws Exception {
    CacheReusePool<Pair<AudioInputStream, Clip>> audioClipList = cacheAudioClip.get(key);

    if (audioClipList == null) {
      audioClipList =
          new CacheReusePool<Pair<AudioInputStream, Clip>>(
              cacheCount,
              new Callable<Pair<AudioInputStream, Clip>>() {
                @Override
                public Pair<AudioInputStream, Clip> call() throws Exception {
                  // 載入音檔來源轉為串流
                  AudioInputStream audio = getAudioInputStream(callable.call());
                  AudioFormat audioFormat = audio.getFormat();
                  DataLine.Info dlInfo =
                      new DataLine.Info(
                          Clip.class,
                          audioFormat,
                          ((int) audio.getFrameLength() * audioFormat.getFrameSize()));

                  // 開啟串流轉換為Clip
                  Clip clip = (Clip) AudioSystem.getLine(dlInfo);
                  clip.open(audio);
                  clip.addLineListener(
                      new LineListener() {
                        @Override
                        public void update(LineEvent event) {
                          if (event.getType().equals(LineEvent.Type.STOP)) {
                            if (!isPause) {
                              if (callbackTartet != null) {
                                callbackTartet.onAudioPlayed(callbackObj);
                              }
                            }
                          }
                        }
                      });
                  return new Pair<AudioInputStream, Clip>(audio, clip);
                }
              });
      cacheAudioClip.put(key, audioClipList);

      clip = audioClipList.next().getSecond();
    } else {
      clip = audioClipList.next().getSecond();
    }
  }

  /** 播放音檔. */
  public void play() {
    if (clip != null) {
      clip.setFramePosition(0);
      clip.loop(playCount);
    }
  }

  /** 恢復播放音檔. */
  public void resume() {
    isPause = false;

    if (clip != null) {
      clip.setFramePosition(clip.getFramePosition());
      clip.loop(playCount);
    }
  }

  /** 暫停播放音檔. */
  public void pause() {
    isPause = true;
    if (clip != null) {
      clip.stop();
    }
  }

  /** 停止播放音檔,且將音檔播放位置移回開始處. */
  public void stop() {
    if (clip != null) {
      clip.stop();
    }
  }

  /**
   * 設定音量.
   *
   * @param db 0~1,預設為0.5
   */
  public void setVolume(float db) {
    float tempB = floorPow(db, 1);
    gain = tempB;
    resetVolume();
  }

  /**
   * 次方運算無條件捨去.
   *
   * @param min 要無條件捨去的數字
   * @param Num 要捨去的位數
   */
  private float floorPow(float min, int num) {
    float n = (float) Math.pow(10, num);
    return ((int) (min * n)) / n;
  }

  /** 重設音量. */
  protected void resetVolume() {
    gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    // double gain = .5D; // number between 0 and 1 (loudest)
    float db = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
    gainControl.setValue(db);
  }

  /**
   * 設定靜音狀態,true:靜音,false:不靜音.
   *
   * @param enable 開關
   */
  public void setMute(boolean enable) {
    mute = enable;
    resetMute();
  }

  /** 重設靜音狀態. */
  protected void resetMute() {
    muteControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
    muteControl.setValue(mute);
  }

  /** 取得播放位置. */
  public int getFramePosition() {
    try {
      return clip.getFramePosition();
    } catch (Exception e) {
      return -1;
    }
  }

  /** 取得音檔的串流. */
  public AudioInputStream getAudioInputStream(Object loadReference) {
    try {
      AudioInputStream aiStream;
      if (loadReference == null) {
        return null;
      } else if (loadReference instanceof URL) {
        URL url = (URL) loadReference;
        aiStream = AudioSystem.getAudioInputStream(url);
      } else if (loadReference instanceof File) {
        File file = (File) loadReference;
        aiStream = AudioSystem.getAudioInputStream(file);
      } else if (loadReference instanceof AudioInputStream) {
        AudioInputStream stream = (AudioInputStream) loadReference;
        aiStream = AudioSystem.getAudioInputStream(stream.getFormat(), stream);
        stream.reset();
      } else {

        InputStream inputStream = (InputStream) loadReference;
        aiStream = AudioSystem.getAudioInputStream(inputStream);
      }

      return aiStream;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /** 取得剪輯音檔. */
  public Clip getClip() {
    return clip;
  }
}
