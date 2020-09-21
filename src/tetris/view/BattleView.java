package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import tetris.Config;
import tetris.game.GameEvent;
import tetris.game.GameLoop;
import tetris.view.component.RepaintView;
import tetris.view.listener.GameEventListener;
import util.AudioPlayer;
import util.Debug;

/**
 * 此類別只做畫面處理，不做方塊移動運算，所有GameLoop類別所觸發的事件會通知此類別的tetrisEvent() method
 *
 * @author Ray
 */
public class BattleView extends RepaintView implements GameEventListener {
  private static final long serialVersionUID = 1L;
  private int nextBoxCount = Config.get().getNextBoxs(); // 下次要出現的方塊可顯示個數
  private int[][][] boxBuffer; // 下次要出現的方塊style
  private int boxStartX; // 掉落方塊的初始位置x
  private int boxStartY; // 掉落方塊的初始位置y
  private int singleBoxWidth; // 每個方塊格寬
  private int singleBoxHeight; // 每個方塊格高
  private int rightNextBoxesX; // 右側方塊的位置x
  private int rightNextBoxesHeightSpacing; // 右側方塊的位置y間距
  private int scoreLocationY; // 分數顯示位置
  private int levelLocationY; // 等級顯示位置
  private int gameOverLocationX; // 遊戲結束顯示位置x
  private int gameOverLocationY; // 遊戲結束顯示位置y
  private int nextRoundCountdownSecondLocationX; // 下局倒數秒數顯示位置x
  private int nextRoundCountdownSecondLocationY; // 下局倒數秒數顯示位置y
  private int linesLocationY; // 方塊消除累計行數顯示位置
  private Font scoreFont;
  private Color[] color = {
    null,
    new Color(0, 255, 255, 250),
    new Color(0, 0, 255, 250),
    new Color(0, 255, 0, 250),
    new Color(255, 0, 0, 250),
    new Color(255, 255, 0, 250),
    new Color(255, 0, 255, 250),
    new Color(50, 100, 150, 250)
  };
  private Color shadowColor = new Color(0, 0, 0, 128);

  private GameLoop gameLoop; // 遊戲邏輯(無畫面)
  private AudioPlayer backgroundMusic; // 播放背景音樂
  private InfoBar infoBar;
  private AdversaryTetris adversaryTetris;

  public BattleView(int width, int height) {
    super(width, height);
    backgroundMusic = playMusic("sound/music.wav");
  }

  @Override
  public void initialize() {
    scoreFont = null;
    Config config = Config.get();

    boxStartX = config.zoom(62);
    boxStartY = config.zoom(79);
    singleBoxWidth = config.zoom(19);
    singleBoxHeight = config.zoom(19);
    rightNextBoxesX = config.zoom(160);
    rightNextBoxesHeightSpacing = config.zoom(50);

    // 分數位置
    levelLocationY = Config.get().zoom(20);
    linesLocationY = Config.get().zoom(45);
    scoreLocationY = Config.get().zoom(70);

    // 遊戲結束
    gameOverLocationX = Config.get().zoom(100);
    gameOverLocationY = Config.get().zoom(250);
    nextRoundCountdownSecondLocationX = Config.get().zoom(155);
    nextRoundCountdownSecondLocationY = Config.get().zoom(270);

    adversaryTetris = new AdversaryTetris(value -> (int) (value * 0.5));
    adversaryTetris.setWidth(Config.get().zoom(15));
    adversaryTetris.setHeight(Config.get().zoom(15));
    adversaryTetris.setLocation(Config.get().zoom(260), Config.get().zoom(280));
    add(adversaryTetris);

    // 分數、消除行數、等級
    infoBar = new InfoBar();
    // 建立遊戲邏輯
    gameLoop = new GameLoop();

    // 設定使用GameView代理遊戲邏輯進行畫面的繪圖
    gameLoop.setEventListener(this);

    // 設定方塊掉落秒數為
    gameLoop.setSecond(Config.get().getBoxFallSpeed(infoBar.getLevel()));

    // 設定下次要出現的方塊style個數為顯示3個
    boxBuffer = getBufBox(gameLoop, nextBoxCount);

    // 啟動遊戲邏輯執行緒
    gameLoop.startGame();
  }

  private AudioPlayer playMusic(String path) {
    return playAudio(path, 0, 1);
  }

  private AudioPlayer playSound(String path) {
    return playAudio(path, 1, Config.get().getSoundCacheCount());
  }

  private AudioPlayer playAudio(String path, int playCount, int cacheCount) {
    AudioPlayer audio = new AudioPlayer();
    path = "/" + path;
    audio.loadAudio(path, this);
    audio.setCacheCount(cacheCount);

    audio.setPlayCount(playCount); // 播放次數
    audio.play();
    return audio;
  }

  @Override
  public void onMouseClicked(MouseEvent e) {}

  // 接收鍵盤事件
  @Override
  public void onKeyCode(int code) {
    if (gameLoop.isGameOver()) {
      return;
    }
    if (code == KeyEvent.VK_ESCAPE) {
      changeView(ViewName.MENU);
      return;
    }
    if (!gameLoop.isPause()) {
      switch (code) {
        case KeyEvent.VK_UP: // 上,順轉方塊
          gameLoop.turnRight();
          onEvent(GameEvent.BOX_TURN, null);
          break;
        case KeyEvent.VK_DOWN: // 下,下移方塊
          moveDown();
          break;
        case KeyEvent.VK_LEFT: // 左,左移方塊
          gameLoop.moveLeft();
          break;
        case KeyEvent.VK_RIGHT: // 右,右移方塊
          gameLoop.moveRight();
          break;
        case KeyEvent.VK_SPACE: // 空白鍵,快速掉落方塊
          quickDown();
          break;
        default:
      }
      // TODO ---test---begin
      adversaryTetris.onKeyCode(code, false);
      // TODO ---test---end
    }

    // 每次按了鍵盤就將畫面重繪
    repaint();
  }

  private void moveDown() {
    if (gameLoop.moveDown()) {
      infoBar.addScore(Config.get().getMoveDownScore());
    }
  }

  private void quickDown() {
    int befor = gameLoop.getNowBoxXY()[1];
    gameLoop.quickDown();
    int after = gameLoop.getNowBoxXY()[1];
    // 若方塊快速落到底，再另外加分數
    int quickDownScore = after - befor;

    if (quickDownScore > 0) {
      infoBar.addScore(quickDownScore * Config.get().getQuickDownScore());
    }
  }

  // 雙緩衝區繪圖
  @Override
  public void onPaintComponent(Graphics canvas) {
    super.onPaintComponent(canvas);

    canvas.setColor(Color.BLACK);

    // 把整個陣列要畫的圖，畫到暫存的畫布上去(即後景)
    int[][] boxAry = gameLoop.getBoxAry();
    showBacegroundBox(boxAry, canvas);

    // 畫掉落中的方塊
    int[] xy = gameLoop.getNowBoxXY();
    int[][] box = gameLoop.getNowBoxAry();

    // 畫陰影
    shadow(xy, box, canvas, gameLoop.getDownY());

    showDownBox(xy, box, canvas);

    // 畫右邊下次要出現的方塊
    showBufferBox(boxBuffer, canvas);

    // 顯示分數
    showInfoBar(infoBar, canvas);

    // 顯示遊戲結束，並倒數秒數
    showGameOver(infoBar, canvas);
  }

  // 畫定住的方塊與其他背景格子
  private void showBacegroundBox(int[][] boxAry, Graphics buffImg) {
    for (int i = 0; i < boxAry.length; i++) {
      for (int j = 0; j < boxAry[i].length; j++) {
        int style = boxAry[i][j];
        if (style > 0) { // 畫定住的方塊
          drawBox(style, j, i, buffImg);
        } else { // 畫其他背景格子
          buffImg.drawRect(
              boxStartX + (singleBoxWidth * j),
              boxStartY + (singleBoxHeight * i),
              singleBoxWidth,
              singleBoxHeight);
        }
      }
    }
  }

  // 畫掉落中的方塊
  private void showDownBox(int[] xy, int[][] box, Graphics buffImg) {
    int boxX = xy[0];
    int boxY = xy[1];
    for (int i = 0; i < box.length; i++) {
      for (int j = 0; j < box[i].length; j++) {
        int style = box[i][j];
        if (style > 0) {
          drawBox(style, (j + boxX), (i + boxY), buffImg);
        }
      }
    }
  }

  // 畫右邊下次要出現的方塊
  private void showBufferBox(int[][][] boxBuf, Graphics buffImg) {
    for (int n = 0; n < boxBuf.length; n++) {
      int[][] ary = boxBuf[n];

      for (int i = 0; i < ary.length; i++) {
        for (int j = 0; j < ary[i].length; j++) {
          int style = ary[i][j];
          if (style > 0) {
            buffImg.setColor(color[style]);
            buffImg.fill3DRect(
                rightNextBoxesX + (singleBoxWidth * (j + 5)),
                (n * rightNextBoxesHeightSpacing) + (singleBoxHeight * (i + 5)),
                singleBoxWidth,
                singleBoxHeight,
                true);
          }
        }
      }
    }
  }

  // 畫陰影
  private void shadow(int[] xy, int[][] box, Graphics buffImg, int index) {
    int boxX = xy[0];
    buffImg.setColor(shadowColor);

    for (int i = 0; i < box.length; i++) {
      for (int j = 0; j < box[i].length; j++) {
        int style = box[i][j];
        if (style > 0) {
          buffImg.fill3DRect(
              boxStartX + (singleBoxWidth * (j + boxX)),
              boxStartY + (singleBoxHeight * (i + index)),
              singleBoxWidth,
              singleBoxHeight,
              true);
        }
      }
    }
  }

  private void showInfoBar(InfoBar info, Graphics buffImg) {
    if (scoreFont == null) {
      Font currentFont = buffImg.getFont();
      Font newFont = currentFont.deriveFont(Font.BOLD, Config.get().zoom(20));
      scoreFont = newFont;
    }
    // 調整分數字型
    buffImg.setFont(scoreFont);

    buffImg.setColor(Color.RED);
    buffImg.drawString("LEVEL : " + info.getLevel(), 2, levelLocationY);
    buffImg.setColor(Color.BLACK);
    buffImg.drawString("SCORE : " + info.getScore(), 2, linesLocationY);
    buffImg.setColor(Color.BLUE);
    buffImg.drawString("LINES : " + info.getCleanedCount(), 2, scoreLocationY);
  }

  private void showGameOver(InfoBar info, Graphics buffImg) {
    if (gameLoop.isGameOver()) {
      buffImg.setColor(Color.DARK_GRAY);
      buffImg.drawString("GAME OVER", gameOverLocationX, gameOverLocationY);

      buffImg.drawString(
          String.valueOf(info.getWaitNextRoundSecond() + 1),
          nextRoundCountdownSecondLocationX,
          nextRoundCountdownSecondLocationY);
    }
  }

  /**
   * 畫每個小格子
   *
   * @param style
   * @param x
   * @param y
   * @param buffImg
   */
  public void drawBox(int style, int x, int y, Graphics buffImg) {
    buffImg.setColor(color[style]);
    buffImg.fill3DRect(
        boxStartX + (singleBoxWidth * x),
        boxStartY + (singleBoxHeight * y),
        singleBoxWidth,
        singleBoxHeight,
        true);
    buffImg.setColor(Color.BLACK);
  }

  /**
   * 將下個方塊字串轉成2維方塊陣列，以便繪圖
   *
   * @param bufbox
   * @param tetris
   * @return
   */
  public int[][][] getBufBox(GameLoop tetris, int cnt) {
    String[] bufbox = tetris.getAnyCountBox(cnt);
    int[][][] ary = new int[bufbox.length][][];
    for (int i = 0; i < bufbox.length; i++) {
      ary[i] = tetris.createBox(Integer.parseInt(bufbox[i]));
    }
    return ary;
  }

  /** 所有 */
  @Override
  public void onEvent(GameEvent code, String data) {
    // 收到重畫自己畫面的陣列
    if (GameEvent.REPAINT == code) {
      repaint();
      return;
    }
    if (GameEvent.BOX_TURN == code) {
      playSound("sound/turn.wav");
      return;
    }
    // 方塊下移
    if (GameEvent.BOX_MOVE_DOWN == code) {
      // TODO ---test---begin
      adversaryTetris.onKeyCode(KeyEvent.VK_DOWN, true);
      // TODO ---test---end
      return;
    }
    // 方塊落到底
    if (GameEvent.BOX_DOWN == code) {
      // 播放方塊掉落音效
      playSound("sound/down.wav");
      return;
    }
    // 建立完下一個方塊
    if (GameEvent.BOX_NEW == code) {
      // TODO ---test---begin
      adversaryTetris.createCube(Integer.parseInt(data));
      // TODO ---test---end
      return;
    }
    // 建立完下一個方塊
    if (GameEvent.BOX_NEXT == code) {
      boxBuffer = getBufBox(gameLoop, nextBoxCount);
      return;
    }
    // 有方塊可清除,將要清除方塊,可取得要消去的方塊資料
    if (GameEvent.CLEANING_LINE == code) {
      Debug.get().println("有方塊可清除,將要清除方塊,可取得要消去的方塊資料");
      return;
    }
    // 方塊清除完成
    if (GameEvent.CLEANED_LINE == code) {
      Debug.get().println("方塊清除完成" + data);
      String[] lines = data.split("[,]", -1);
      infoBar.addCleanedCount(lines.length);
      infoBar.addScore(Config.get().getCleanLinesScore(lines.length));

      if (tryLevelUp()) {
        Debug.get().println("提升等級!!");
      }
      return;
    }
    // 計算自己垃圾方塊數
    if (GameEvent.BOX_GARBAGE == code) {

      return;
    }
    // 方塊頂到最高處，遊戲結束
    if (GameEvent.GAME_OVER == code) {
      infoBar.setWaitNextRoundSecond(Config.get().getNextRoundDelaySecond());

      while (infoBar.getWaitNextRoundSecond() > 0) {
        repaint();
        Debug.get().println(infoBar.getWaitNextRoundSecond() + "秒後開始新局...");
        infoBar.addWaitNextRoundSecond(-1);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {

          e.printStackTrace();
        }
      }
      // 重置分數
      infoBar.initialize();
      // 清除全畫面方塊
      gameLoop.clearBox();

      // TODO ---test---begin
      adversaryTetris.reset();
      // TODO ---test---end

      // 設定方塊掉落秒數
      gameLoop.setSecond(Config.get().getBoxFallSpeed(infoBar.getLevel()));

      // 當方塊到頂時，會自動將GameOver設為true,因此下次要開始時需設定遊戲為false表示可進行遊戲
      gameLoop.setGameOver(false);
    }
    return;
  }

  /** 試著計算是否提升等級1級，並重設方塊掉落速度 */
  private boolean tryLevelUp() {
    int currentLevel = infoBar.getLevel();
    int newLevel = Config.get().linesConvertLevel(infoBar.getCleanedCount());

    if (currentLevel != newLevel) {
      infoBar.setLevel(newLevel);
      gameLoop.setSecond(Config.get().getBoxFallSpeed(infoBar.getLevel()));
      return true;
    }
    return false;
  }

  @Override
  public void release() {
    backgroundMusic.stop();
    adversaryTetris.close();
  }
}
