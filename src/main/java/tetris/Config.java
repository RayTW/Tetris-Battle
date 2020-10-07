package tetris;

/**
 * 遊戲設定
 *
 * @author Ray Lee Created on 2017/10/18
 */
public class Config {
  private static Config instance = new Config();

  // 遊戲版本
  private String version = "1.4.4";

  // 消除方塊行數可獲得的分數
  private int[] cleanLinesScore = {0, 40, 100, 300, 1200};

  // 方塊掉落速度(fps/秒)
  private float[] boxFallSpeed = {48, 43, 38, 33, 28, 23, 18, 8, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 2};

  // 每個等級升下一級所需的行數
  private int[] levelUpLines = {
    10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 100, 100, 100, 100, 100, 100, 110, 120, 130
  };

  // 畫面比例
  private double screenScale = 1.3;

  // 重新開始等待秒數
  private int nextRoundDelaySecond = 5;

  // 重用音效緩存的筆數
  private int soundCacheCount = 20;

  // 下移1格時可獲得的分數
  private int moveDownScore = 5;

  // 快速下移時可獲得的分數
  private int quickDownScore = 10;

  // 可顯示將要的掉落方塊個數(畫面右側)
  private int nextCubeSize = 3;

  // 是否顯示目前畫面的fps
  private boolean displayFps;
  // 設定fps上限(預設30)
  private int maxFps;
  // 畫面刷新間隔ms
  private int repainMills;

  private String host = "127.0.0.1";
  private int port = 8009;
  private String userName = "user" + Math.random() * Integer.MAX_VALUE;

  private Config() {
    setMaxFps(30);
  }

  public static Config get() {
    return instance;
  }

  /**
   * 取得遊戲版本
   *
   * @return
   */
  public String getVersion() {
    return version;
  }

  /**
   * 設定遊戲畫面縮放比
   *
   * @param scale 縮放比 0.0 ~ 2.0，預設1.3
   */
  public void setScreenScale(double scale) {
    screenScale = scale;
  }

  public void setMaxFps(int fps) {
    if (fps > 60) {
      fps = 60;
    } else if (fps < 1) {
      fps = 1;
    }
    this.maxFps = fps;
    repainMills = 1000 / fps;
  }

  public int getMaxFps() {
    return maxFps;
  }

  public int getRepainMills() {
    return repainMills;
  }

  public void setDisplayFps(boolean enable) {
    displayFps = enable;
  }

  public boolean isDisplayFps() {
    return displayFps;
  }

  /**
   * 單列 100 雙列 300 三列 500 四列 800
   *
   * @param cleanLines 消除行數
   * @return
   */
  public int getCleanLinesScore(int cleanLines) {
    if (cleanLines > 0 && cleanLines < cleanLinesScore.length) {
      return cleanLinesScore[cleanLines];
    }
    return 0;
  }

  /**
   * 遊戲畫面縮放，將傳入的數值加乘畫面縮放比率後回傳
   *
   * @param value
   * @return
   */
  public int zoom(int value) {
    return (int) (value * screenScale);
  }

  /**
   * 重新開始等待秒數
   *
   * @return
   */
  public int getNextRoundDelaySecond() {
    return nextRoundDelaySecond;
  }

  /**
   * 重用音效緩存的筆數
   *
   * @return
   */
  public int getSoundCacheCount() {
    return soundCacheCount;
  }

  /**
   * 用等級取得方塊掉落速度
   *
   * @param level
   * @return
   */
  public float getBoxFallSpeed(int level) {
    if (level >= 0 && level < boxFallSpeed.length) {
      return boxFallSpeed[level] / 60.0f;
    }
    return boxFallSpeed[boxFallSpeed.length - 1] / 60.0f;
  }

  /**
   * 已消除的方塊行數轉換為對應的等級
   *
   * @param lines
   * @return
   */
  public int linesConvertLevel(int lines) {
    int level = 0;

    for (int n : levelUpLines) {
      lines -= n;

      if (lines < 0) {
        break;
      }
      level++;
    }

    int maxLines = levelUpLines[levelUpLines.length - 1];

    if (lines > maxLines) {
      level += (lines / maxLines);
    }
    return level;
  }

  /**
   * 取得下移1格時可獲得的分數
   *
   * @return
   */
  public int getMoveDownScore() {
    return moveDownScore;
  }

  /**
   * 取得快速下速方塊可獲得的分數
   *
   * @return
   */
  public int getQuickDownScore() {
    return quickDownScore;
  }

  /**
   * 可顯示將要的掉落方塊個數(畫面右側)
   *
   * @return
   */
  public int getNextCubeSize() {
    return nextCubeSize;
  }

  public void setHostPort(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public void setUserName(String name) {
    userName = name;
  }

  public String getUserName() {
    return userName;
  }
}
