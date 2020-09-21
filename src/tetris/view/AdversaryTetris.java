package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import tetris.Config;
import tetris.game.Cube;
import tetris.game.CubeMatrix;
import tetris.game.GameLoop;
import tetris.view.component.Role;
import util.CountDownConsumer;

/**
 * 映射對戰對手的畫面
 *
 * @author Ray
 */
public class AdversaryTetris extends Role {
  private CubeMatrix gameBox;
  private int boxStartX; // 掉落方塊的初始位置x
  private int boxStartY; // 掉落方塊的初始位置y
  private int singleBoxWidth; // 每個方塊格寬
  private int singleBoxHeight; // 每個方塊格高
  private int scoreLocationX;
  private int scoreLocationY; // 分數顯示位置
  private int levelLocationX;
  private int levelLocationY; // 等級顯示位置
  private int gameOverLocationX; // 遊戲結束顯示位置x
  private int gameOverLocationY; // 遊戲結束顯示位置y
  private int linesLocationX;
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
  private Color mShadowColor = new Color(0, 0, 0, 128);

  private InfoBar mInfoBar;
  private Zoomable zoomable;
  private CountDownConsumer<Cube> checkClean;

  public AdversaryTetris(Zoomable zoomable) {
    scoreFont = null;
    this.zoomable = zoomable;
    gameBox = new CubeMatrix();
    resetLocation(0, 0);

    // 分數、消除行數、等級
    mInfoBar = new InfoBar();
    checkClean = new CountDownConsumer<>();
    checkClean.setConsumer(this::cleanLine);
    checkClean.start();
  }

  @Override
  public void setLocation(int x, int y) {
    setX(x);
    setY(y);
    resetLocation(x, y);
  }

  @Override
  public void setX(int x) {
    super.setX(x);
    resetLocation(x, getY());
  }

  @Override
  public void setY(int y) {
    super.setY(y);
    resetLocation(getX(), y);
  }

  private void resetLocation(int x, int y) {
    boxStartX = zoomable.zoom(62) + x;
    boxStartY = zoomable.zoom(79) + y;

    singleBoxWidth = zoomable.zoom(getWidth());
    singleBoxHeight = zoomable.zoom(getHeight());

    // 分數位置
    levelLocationX = zoomable.zoom(50) + x;
    levelLocationY = zoomable.zoom(20) + y;
    linesLocationX = zoomable.zoom(50) + x;
    linesLocationY = zoomable.zoom(45) + y;
    scoreLocationX = zoomable.zoom(50) + x;
    scoreLocationY = zoomable.zoom(70) + y;

    // 遊戲結束
    gameOverLocationX = zoomable.zoom(100) + x;
    gameOverLocationY = zoomable.zoom(250) + y;
  }

  private void cleanLine(Cube c) {
    gameBox.addBox(c);
    // 取得可消除的行數
    String lineData = gameBox.getClearLine();

    if (!lineData.isEmpty()) {
      gameBox.clearLine(lineData); // 實際將可消除的方塊行數移除
    }
  }

  // 接收鍵盤事件
  public void onKeyCode(int code, boolean simulation) {
    if (!simulation) {
      switch (code) {
        case KeyEvent.VK_UP: // 上,順轉方塊
          gameBox.turnRight();
          break;
        case KeyEvent.VK_DOWN: // 下,下移方塊
          moveDown();
          break;
        case KeyEvent.VK_LEFT: // 左,左移方塊
          gameBox.moveLeft();
          break;
        case KeyEvent.VK_RIGHT: // 右,右移方塊
          gameBox.moveRight();
          break;
        case KeyEvent.VK_SPACE: // 空白鍵,快速掉落方塊
          quickDown();
          break;
        default:
      }
    } else {
      if (code == KeyEvent.VK_DOWN) {
        if (!gameBox.moveDown()) {
          tryCleanLine();
        }
      }
    }
  }

  private void moveDown() {
    if (gameBox.moveDown()) {
      mInfoBar.addScore(Config.get().getMoveDownScore());
    } else {
      tryCleanLine();
    }
  }

  private void quickDown() {
    int befor = gameBox.getNowBoxXY()[1];
    gameBox.quickDown();
    int after = gameBox.getNowBoxXY()[1];
    // 若方塊快速落到底，再另外加分數
    int quickDownScore = after - befor;

    if (quickDownScore > 0) {
      mInfoBar.addScore(quickDownScore * Config.get().getQuickDownScore());
    }
    tryCleanLine();
  }

  private void tryCleanLine() {
    checkClean.set(gameBox.getCube());
    checkClean.countDown();
  }

  // 雙緩衝區繪圖
  @Override
  public void onDraw(Graphics canvas) {
    // 把整個陣列要畫的圖，畫到暫存的畫布上去(即後景)
    int[][] boxAry = gameBox.getMatrix();
    showBacegroundBox(boxAry, canvas);

    // 畫掉落中的方塊
    int[] xy = gameBox.getNowBoxXY();
    int[][] box = gameBox.getCurrentCube();

    // 畫陰影
    shadow(xy, box, canvas, gameBox.getDownY());

    showDownBox(xy, box, canvas);

    // 顯示分數
    showInfoBar(mInfoBar, canvas);

    //    // 顯示遊戲結束，並倒數秒數
    //    showGameOver(mInfoBar, canvas);
  }

  // 畫定住的方塊與其他背景格子
  private void showBacegroundBox(int[][] boxAry, Graphics buffImg) {
    buffImg.setColor(Color.BLACK);

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

  // 畫陰影
  private void shadow(int[] xy, int[][] box, Graphics buffImg, int index) {
    int boxX = xy[0];
    buffImg.setColor(mShadowColor);
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
      Font newFont = currentFont.deriveFont(Font.BOLD, zoomable.zoom(20));
      scoreFont = newFont;
    }
    // 調整分數字型
    buffImg.setFont(scoreFont);

    buffImg.setColor(Color.RED);
    buffImg.drawString("LEVEL : " + info.getLevel(), levelLocationX, levelLocationY);
    buffImg.setColor(Color.BLACK);
    buffImg.drawString("SCORE : " + info.getScore(), linesLocationX, linesLocationY);
    buffImg.setColor(Color.BLUE);
    buffImg.drawString("LINES : " + info.getCleanedCount(), scoreLocationX, scoreLocationY);
  }

  private void showGameOver(InfoBar info, Graphics buffImg) {
    buffImg.setColor(Color.DARK_GRAY);
    buffImg.drawString("GAME OVER", gameOverLocationX, gameOverLocationY);
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

  /**
   * 創建新的掉落方塊
   *
   * @param style
   */
  public void createCube(int style) {
    gameBox.createNewCube(style);
  }

  /** 重置遊戲頁面 */
  public void reset() {
    // 重置分數
    mInfoBar.initialize();
    // 清除全畫面方塊
    gameBox.clearAllCube();
  }

  public void close() {
    checkClean.stop();
  }

  public static interface Zoomable {
    int zoom(int n);
  }
}
