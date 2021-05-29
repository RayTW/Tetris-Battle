package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONObject;
import tetris.Config;
import tetris.game.CubeMatrix;
import tetris.game.GameFlow;
import tetris.view.component.Role;

/**
 * 映射對戰對手的畫面.
 *
 * @author Ray
 */
public class OpponentTetris extends Role {
  private CubeMatrix cubeMatrix;
  private int boxStartX; // 掉落方塊的初始位置x
  private int boxStartY; // 掉落方塊的初始位置y
  private int singleCubeWidth; // 每個方塊格寬
  private int singleCubeHeight; // 每個方塊格高
  private int scoreLocationX;
  private int scoreLocationY; // 分數顯示位置
  private int gameOverLocationX; // 遊戲結束顯示位置x
  private int gameOverLocationY; // 遊戲結束顯示位置y
  private int nameLocationX;
  private int nameLocationY; // 方塊消除累計行數顯示位置
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

  private int score;
  private String userName = "";
  private Zoomable zoomable;
  private Status status = Status.INIT;
  private List<JSONObject> operationQueue = Collections.synchronizedList(new LinkedList<>());
  private Thread queueComsumerThread;
  private boolean queueThreadRunning;

  /**
   * 建構.
   *
   * @param zoomable 縮放比
   */
  public OpponentTetris(Zoomable zoomable) {
    scoreFont = null;
    this.zoomable = zoomable;
    cubeMatrix = new CubeMatrix();
    resetLocation(0, 0);

    queueThreadRunning = true;
    queueComsumerThread = new Thread(this::processOperation);
    queueComsumerThread.start();
  }

  public void setUserName(String name) {
    userName = name;
  }

  public void addOperation(JSONObject operation) {
    operationQueue.add(operation);
  }

  /**
   * 處理其他玩家遊戲操作同步.
   *
   * <pre>
   *   event :
   *     0:reset
   *     1:game start
   *     2:sync score
   *     10 : 建方塊
   *     20 : keycode
   *     30:game over
   *     40:cleanLine
   * </pre>
   */
  private void processOperation() {
    JSONObject operation = null;
    int event = 0;

    while (queueThreadRunning) {

      while (operationQueue.size() > 0) {
        operation = operationQueue.remove(0);
        event = operation.getInt("event");

        if (event == 10) {
          int code = operation.getInt("keyCode");
          boolean simulation = operation.getBoolean("simulation");

          onKeyCode(code, simulation);
        } else if (event == 2) {
          score = operation.getInt("score");
        } else if (event == 20) {
          createCube(operation.getInt("style"));
        } else if (event == 40) {
          String cube = operation.getString("cube");
          int x = operation.getInt("x");
          int y = operation.getInt("y");
          int style = operation.getInt("style");

          cleanLine(toArray(cube), x, y, style);
        } else if (event == 0) {
          reset();
        } else if (event == 30) {
          status = Status.GAME_OVER;
        } else if (event == 1) {
          status = Status.PLAYING;
        }
        Thread.yield();
      }
    }
  }

  private int[][] toArray(String temp) {
    String[] ary = temp.split("],");
    String[] inner = null;
    int[][] result = new int[ary.length][];

    for (int i = 0; i < ary.length; i++) {
      ary[i] = ary[i].replace("[", "").replace("]", "");
      inner = ary[i].split("[,]");
      result[i] = new int[inner.length];
      for (int j = 0; j < inner.length; j++) {
        result[i][j] = Integer.parseInt(inner[j].trim());
      }
    }
    return result;
  }

  @Override
  public void setLocation(int x, int y) {
    super.setX(x);
    super.setY(y);
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
    boxStartX = x;
    boxStartY = Config.get().zoom(zoomable.zoom(60)) + y;

    singleCubeWidth = zoomable.zoom(getWidth());
    singleCubeHeight = zoomable.zoom(getHeight());

    // 分數位置
    nameLocationX = x;
    nameLocationY = Config.get().zoom(zoomable.zoom(25)) + y;
    scoreLocationX = x;
    scoreLocationY = Config.get().zoom(zoomable.zoom(50)) + y;

    // 遊戲結束
    gameOverLocationX = Config.get().zoom(zoomable.zoom(30)) + x;
    gameOverLocationY = Config.get().zoom(zoomable.zoom(250)) + y;
  }

  private void cleanLine(int[][] b, int x, int y, int style) {
    cubeMatrix.addBox(b, x, y, style);
    // 取得可消除的行數
    String lineData = cubeMatrix.getClearLine();

    if (!lineData.isEmpty()) {
      cubeMatrix.clearLine(lineData); // 實際將可消除的方塊行數移除
    }
  }

  // 接收鍵盤事件
  private void onKeyCode(int code, boolean simulation) {
    if (!simulation) {
      switch (code) {
        case KeyEvent.VK_UP: // 上,順轉方塊
          cubeMatrix.turnRight();
          break;
        case KeyEvent.VK_DOWN: // 下,下移方塊
          cubeMatrix.moveDown();
          break;
        case KeyEvent.VK_LEFT: // 左,左移方塊
          cubeMatrix.moveLeft();
          break;
        case KeyEvent.VK_RIGHT: // 右,右移方塊
          cubeMatrix.moveRight();
          break;
        case KeyEvent.VK_SPACE: // 空白鍵,快速掉落方塊
          cubeMatrix.quickDown();
          break;
        default:
      }
    } else {
      if (code == KeyEvent.VK_DOWN) {
        cubeMatrix.moveDown();
      }
    }
  }

  // 雙緩衝區繪圖
  @Override
  public void onDraw(Graphics canvas) {
    // 把整個陣列要畫的圖，畫到暫存的畫布上去(即後景)
    int[][] boxAry = cubeMatrix.getMatrix();
    drawBacegroundBox(boxAry, canvas);

    if (cubeMatrix.getCube() != null) {
      // 畫掉落中的方塊
      int[] xy = cubeMatrix.getNowBoxXy();
      int[][] box = cubeMatrix.getCurrentCube();

      // 畫陰影
      //      shadow(xy, box, canvas, gameBox.getDownY());

      drawDownBox(xy, box, canvas);
    }

    // 顯示分數
    drawInfoBar(canvas);

    // 顯示遊戲結束
    drawGameOver(canvas);
  }

  // 畫定住的方塊與其他背景格子
  private void drawBacegroundBox(int[][] boxAry, Graphics buffImg) {
    buffImg.setColor(Color.BLACK);

    for (int i = 0; i < boxAry.length; i++) {
      for (int j = 0; j < boxAry[i].length; j++) {
        int style = boxAry[i][j];
        if (style > 0) { // 畫定住的方塊
          drawBox(style, j, i, buffImg);
        } else { // 畫其他背景格子
          buffImg.drawRect(
              boxStartX + (singleCubeWidth * j),
              boxStartY + (singleCubeHeight * i),
              singleCubeWidth,
              singleCubeHeight);
        }
      }
    }
  }

  // 畫掉落中的方塊
  private void drawDownBox(int[] xy, int[][] box, Graphics buffImg) {
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

  private void drawInfoBar(Graphics buffImg) {
    if (scoreFont == null) {
      Font currentFont = buffImg.getFont();
      Font newFont = currentFont.deriveFont(Font.BOLD, Config.get().zoom(zoomable.zoom(16)));
      scoreFont = newFont;
    }
    // 調整分數字型
    buffImg.setFont(scoreFont);

    buffImg.setColor(Color.BLACK);
    buffImg.drawString(userName, nameLocationX, nameLocationY);
    buffImg.setColor(Color.RED);
    buffImg.drawString("SCORE : " + score, scoreLocationX, scoreLocationY);
  }

  private void drawGameOver(Graphics buffImg) {
    if (status != Status.GAME_OVER) {
      return;
    }
    buffImg.setColor(Color.DARK_GRAY);
    buffImg.drawString("GAME OVER", gameOverLocationX, gameOverLocationY);
  }

  /**
   * 畫每個小格子.
   *
   * @param style 類型
   * @param x x位置
   * @param y y位置
   * @param buffImg 圖
   */
  public void drawBox(int style, int x, int y, Graphics buffImg) {
    buffImg.setColor(color[style]);
    buffImg.fill3DRect(
        boxStartX + (singleCubeWidth * x),
        boxStartY + (singleCubeHeight * y),
        singleCubeWidth,
        singleCubeHeight,
        true);
    buffImg.setColor(Color.BLACK);
  }

  /**
   * 將下個方塊字串轉成2維方塊陣列，以便繪圖.
   *
   * @param tetris 流程
   * @param cnt 類數
   */
  public int[][][] getBufBox(GameFlow tetris, int cnt) {
    String[] bufbox = tetris.getAnyCountBox(cnt);
    int[][][] ary = new int[bufbox.length][][];
    for (int i = 0; i < bufbox.length; i++) {
      ary[i] = tetris.createBox(Integer.parseInt(bufbox[i]));
    }
    return ary;
  }

  /**
   * 創建新的掉落方塊.
   *
   * @param style 方塊類型
   */
  public void createCube(int style) {
    cubeMatrix.createNewCube(style);
  }

  /** 重置遊戲頁面. */
  private void reset() {
    status = Status.INIT;
    score = 0;
    // 清除全畫面方塊
    cubeMatrix.clearAllCube();
  }

  public void close() {
    queueThreadRunning = false;
    queueComsumerThread.interrupt();
  }

  /**
   * 縮放.
   *
   * @author ray
   */
  public static interface Zoomable {
    int zoom(int n);
  }

  enum Status {
    INIT,
    PLAYING,
    GAME_OVER
  }
}
