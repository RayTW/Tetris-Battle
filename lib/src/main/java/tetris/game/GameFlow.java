package tetris.game;

import java.util.Random;
import tetris.view.listener.GameEventListener;
import util.CountDownConsumer;

/**
 * 控制遊戲流程.
 *
 * @author Ray
 */
public class GameFlow implements Runnable {
  private float sec;
  private Random rand;
  private CubeMatrix gameBox;
  private boolean isRun;
  private boolean isPause;
  private boolean isGameOver; // 是否遊戲結束
  private boolean isClean; // 目前是否有方塊到底
  private GameEventListener eventListener;
  private CountDownConsumer<Cube> checkClean;
  private Thread thread;

  private int flag; // 目前使用的方塊位置
  private String[] styleBuffer; // 預戴方塊buffer區

  /** 建構. */
  public GameFlow() {
    isRun = true;
    rand = new Random();
    checkClean = new CountDownConsumer<>();
    checkClean.setConsumer(this::cleanLine);
    initialize();
  }

  /** 初始化. */
  public void initialize() {
    flag = 0;
    styleBuffer = new String[0];
    sec = 0.2f;
    gameBox = new CubeMatrix();
    isPause = false;
    isGameOver = false;
    checkClean.start();
    setBoxList(getRandBox(5)); // 設定使用5組亂數排列方塊進行遊戲
  }

  public void start() {
    thread = new Thread(this);
    thread.start();
  }

  /** 停止遊戲. */
  public void stop() {
    isRun = false;
    isGameOver = true;
    thread.interrupt();
    checkClean.stop();
  }

  @Override
  public void run() {
    publishEvent(GameEvent.GAME_START, "");
    // 遊戲開始才建立第1個方塊
    nextCreateBox();
    while (isRun) {
      try {
        if (!isPause && !isGameOver) { // 沒有按暫停才可玩
          if (!gameBox.moveDown()) { // 方塊已到底停住,不能再往下移
            isClean = true;
            tryCheckClean();
          } else {
            publishEvent(GameEvent.BOX_MOVE_DOWN, "");
          }
          publishEvent(GameEvent.REPAINT, "");
        }
        Thread.sleep((int) (1000 * sec));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    eventListener = null;
    rand = null;
    gameBox = null;
    styleBuffer = null;
  }

  /**
   * 設定預載的方塊style.
   *
   * @param boxList 方壽容器
   */
  public void setBoxList(String boxList) {
    if (!boxList.isEmpty()) {
      styleBuffer = boxList.split("[|]");
    }
  }

  /**
   * 取得n組亂數排列方塊,例如取1組為:"1|5|4|3|2|6|7".
   *
   * @param n 方塊組數
   */
  public String getRandBox(int n) {
    int[] boxAry = new int[Cube.getStyleCount()];
    StringBuffer styleList = new StringBuffer();

    // 初始化可使用的方塊style,目前為1~7
    for (int i = 1; i <= boxAry.length; i++) {
      boxAry[i - 1] = i;
    }

    // 將1~7方塊亂數排列後，轉成字串回傳
    for (int i = 0; i < n; i++) {
      int[] ary = randBoxAry(boxAry);
      for (int j = 0; j < ary.length; j++) {
        if (styleList.length() > 0) {
          styleList.append("|");
        }
        styleList.append(ary[j]);
      }
    }

    return styleList.toString();
  }

  /**
   * 亂數打亂方塊排列 .
   *
   * @param ary 方塊
   */
  private int[] randBoxAry(int[] ary) {
    for (int i = 0; i < ary.length; i++) {
      int tmpIndex = rand.nextInt(ary.length);
      int style = ary[tmpIndex];
      ary[tmpIndex] = ary[i];
      ary[i] = style;
    }
    return ary;
  }

  /** 取出預載方塊buffer下一個方塊. */
  public int nextBox() {
    int style = 0;
    if (flag > styleBuffer.length - 1) {
      flag = 0;
    }
    style = Integer.parseInt(styleBuffer[flag]);
    flag++;
    return style;
  }

  /**
   * 取出預載方塊buffer n個方塊.
   *
   * @param n 方塊組數
   */
  public String[] getAnyCountBox(int n) {
    String[] box = new String[n];
    int tmpFlag = flag;

    for (int i = 0; i < n; i++) {
      if (tmpFlag > styleBuffer.length - 1) {
        tmpFlag = 0;
      }
      box[i] = styleBuffer[tmpFlag];
      tmpFlag++;
    }
    return box;
  }

  /** 控制方塊下移1格. */
  public boolean moveDown() {
    if (!gameBox.moveDown()) {
      tryCheckClean();
      return false;
    }
    return true;
  }

  /** 控制方塊左移1格. */
  public boolean moveLeft() {
    if (isClean) {
      return false;
    }

    return gameBox.moveLeft();
  }

  /** 控制方塊右移1格. */
  public boolean moveRight() {
    if (isClean) {
      return false;
    }

    return gameBox.moveRight();
  }

  /** 控制方塊順轉1次. */
  public boolean turnLeft() {
    if (isClean) {
      return false;
    }

    return gameBox.turnLeft();
  }

  /** 控制方塊逆轉1次. */
  public boolean turnRight() {
    if (isClean) {
      return false;
    }

    return gameBox.turnRight();
  }

  /** 方塊直接掉落到定位. */
  public void quickDown() {
    if (isClean) {
      return;
    }
    gameBox.quickDown();
    tryCheckClean();
    isClean = true;
  }

  /** 遊戲暫停. */
  public void pause() {
    isPause = true;
  }

  /** 目前是否暫停中. */
  public boolean isPause() {
    return isPause;
  }

  /** 繼續遊戲(有按暫停之後使用). */
  public void rusme() {
    isPause = false;
  }

  /**
   * 設定GameOver狀態,true:遊戲結束,false:遊戲未結束.
   *
   * @param b 遊戲是否結束標記
   */
  public void setGameOver(boolean b) {
    isGameOver = b;
  }

  /** 取得目前是否遊戲結束. */
  public boolean isGameOver() {
    return isGameOver;
  }

  /**
   * 設定遊戲事件傾聽者.
   *
   * @param listener 遊戲結束傾聽
   */
  public void setEventListener(GameEventListener listener) {
    eventListener = listener;
  }

  /**
   * 設定目前方塊掉落等待秒數.
   *
   * @param s 秒數
   */
  public void setSecond(float s) {
    sec = s;
  }

  /** 取得目前方塊掉落等待秒數. */
  public float getSec() {
    return sec;
  }

  /**
   * 發出事件.
   *
   * @param code 遊戲流程事件
   * @param data 資料
   */
  public void publishEvent(GameEvent code, Object data) {
    if (eventListener != null) {
      eventListener.onEvent(code, data);
    }
  }

  /**
   * 取得目前二維陣列裡，疊的方塊到第幾個位置，0~20個單位<br>
   * 必須在方塊落到底時呼叫(即GameLoop的cleanLine()被執行時)，才可得到正確的高度資料.
   */
  public int getNowBoxIndex() {
    return gameBox.getCurrentCubeIndex();
  }

  /** 取得目前的整個遊戲畫面可移動方塊區域的二維陣列. */
  public int[][] getBoxAry() {
    return gameBox.getMatrix();
  }

  /** 亂數產生方塊. */
  public boolean randCreatBox() {
    int style = rand.nextInt(Cube.getStyleCount()) + 1;
    return gameBox.createNewCube(style);
  }

  /** 從buffer取方塊建立. */
  public boolean nextCreateBox() {
    int style = nextBox();
    publishEvent(GameEvent.BOX_NEW, String.valueOf(style));
    return gameBox.createNewCube(style);
  }

  public int[][] createBox(int style) {
    return gameBox.createCube(style);
  }

  private void tryCheckClean() {
    checkClean.set(gameBox.getCube());
    checkClean.countDown();
  }

  private void cleanLine(Cube c) {
    if (isGameOver()) {
      return;
    }
    gameBox.addBox(c);
    publishEvent(GameEvent.CLEAN_LINE_BEFORE, c);
    publishEvent(GameEvent.REPAINT, "");
    publishEvent(GameEvent.BOX_DOWN, "");

    // 取得可消除的行數
    String lineData = gameBox.getClearLine();

    if (!lineData.isEmpty()) {
      publishEvent(GameEvent.CLEANING_LINE, lineData);
      gameBox.clearLine(lineData); // 實際將可消除的方塊行數移除
      publishEvent(GameEvent.CLEANED_LINE, lineData);
    }

    publishEvent(GameEvent.BOX_GARBAGE, lineData);

    boolean isOk = nextCreateBox(); // 建立方塊
    if (!isOk) { // 建立失敗
      isGameOver = true;
      publishEvent(GameEvent.REPAINT, "");
      publishEvent(GameEvent.GAME_OVER, "");
    }
    publishEvent(GameEvent.REPAINT, "");
    publishEvent(GameEvent.BOX_NEXT, "");
    isClean = false;
  }

  /** 目前掉落方塊已定格中，進行檢查可消方塊. */
  public boolean isClean() {
    return isClean;
  }

  /** 清空整個畫面所有方塊. */
  public void clearBox() {
    gameBox.clearAllCube();
  }

  /** 取得掉落中方塊. */
  public int[][] getNowBoxAry() {
    return gameBox.getCurrentCube();
  }

  /** 取得掉落中方塊目前的x、y位置. */
  public int[] getNowBoxXy() {
    return gameBox.getNowBoxXy();
  }

  public int getNowBoxStyle() {
    return gameBox.getCurrentCubeStyle();
  }

  /** 取得到第Y個位置會撞到方塊. */
  public int getDownY() {
    return gameBox.getDownY();
  }

  /**
   * isGap為false時 取得指定index行的方塊串，格式為"1|2|3|4|5||6|7|1|2|3@1|2|3|4|5||6|7|1|2|3@..."<br>
   * isGap為true時 取得指定index行的方塊串，格式為"1|2|3|4|5||6|7|1|0|0@1|2|3|4|5||6|7|1|2|0@..."
   *
   * @param lineData 接收格式為:17,19,5...
   * @param isGap true 取出的已被消除行數資料是未加上掉落方塊，false取出可被消行數是加上掉落方塊
   */
  public String getLineList(String lineData, boolean isGap) {
    return gameBox.getLineList(lineData, isGap);
  }
}
