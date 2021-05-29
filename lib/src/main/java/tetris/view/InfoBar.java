package tetris.view;

/**
 * 分數、消除行數、遊戲等級等等資訊.
 *
 * @author Ray Lee Created on 2017/10/18
 */
public class InfoBar {
  private long scoreMax = 999999999;
  private long score; // 遊戲分數
  private int levelMax = 99;
  private int level; // 遊戲等級
  private int cleanedCountMax = 999;
  private int cleanedCount; // 已消除的行數
  private int waitNextRoundSecond; // 遊戲結束後倒數秒數

  public InfoBar() {
    reset();
  }

  /** 重置. */
  public void reset() {
    score = 0;
    level = 0;
    cleanedCount = 0;
    waitNextRoundSecond = 0;
  }

  public long getScore() {
    return score;
  }

  /**
   * 設置分數.
   *
   * @param score 分數
   */
  public void setScore(int score) {
    if (score > scoreMax) {
      this.score = scoreMax;
    } else {
      this.score = score;
    }
  }

  /**
   * 加分數.
   *
   * @param score 分數
   */
  public void addScore(int score) {
    if ((score + score) > scoreMax) {
      this.score = scoreMax;
    } else {
      this.score += score;
    }
  }

  public int getLevel() {
    return level;
  }

  /**
   * 設置等級.
   *
   * @param level 等級
   */
  public void setLevel(int level) {
    if (level > levelMax) {
      this.level = levelMax;
    } else {
      this.level = level;
    }
  }

  /**
   * 加等級.
   *
   * @param level 等級
   */
  public void addLevel(int level) {
    if ((level + level) > levelMax) {
      level = levelMax;
    } else {
      level += level;
    }
  }

  public int getCleanedCount() {
    return cleanedCount;
  }

  /**
   * 設置已消除方塊行數.
   *
   * @param cleanedCount 消除行數
   */
  public void setCleanedCount(int cleanedCount) {
    if (cleanedCount > cleanedCountMax) {
      this.cleanedCount = cleanedCountMax;
    } else {
      this.cleanedCount = cleanedCount;
    }
  }

  /**
   * 新增已消除方塊行數.
   *
   * @param cleanedCount 消除行數
   */
  public void addCleanedCount(int cleanedCount) {
    if ((this.cleanedCount + cleanedCount) > cleanedCountMax) {
      this.cleanedCount = cleanedCountMax;
    } else {
      this.cleanedCount += cleanedCount;
    }
  }

  public void setWaitNextRoundSecond(int second) {
    waitNextRoundSecond = second;
  }

  public int getWaitNextRoundSecond() {
    return waitNextRoundSecond;
  }

  public void addWaitNextRoundSecond(int second) {
    waitNextRoundSecond += second;
  }
}
