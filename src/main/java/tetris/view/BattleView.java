package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import org.json.JSONObject;
import tetris.Config;
import tetris.game.Cube;
import tetris.game.GameEvent;
import tetris.game.GameFlow;
import tetris.game.battle.Client;
import tetris.view.component.Label;
import tetris.view.component.RepaintView;
import tetris.view.listener.GameEventListener;
import util.AudioManager;
import util.AudioPlayer;
import util.Debug;

/**
 * 此類別只做畫面處理，不做方塊移動運算，所有GameLoop類別所觸發的事件會通知此類別的tetrisEvent() method
 *
 * @author Ray
 */
public class BattleView extends RepaintView implements GameEventListener {
  private static final long serialVersionUID = 1L;
  private static String SCORE = "SCORE : ";
  private static String LEVEL = "LEVEL : ";
  private static String LINES = "LINES : ";

  private int nextCubeSize = Config.get().getNextCubeSize(); // 下次要出現的方塊可顯示個數
  private int[][][] cubeBuffer; // 下次要出現的方塊style
  private int boxStartX; // 掉落方塊的初始位置x
  private int boxStartY; // 掉落方塊的初始位置y
  private int singleCubeWidth; // 每個方塊格寬
  private int singleCubeHeight; // 每個方塊格高
  private int rightNextCubeX; // 右側方塊的位置x
  private int rightNextCubeHeightSpacing; // 右側方塊的位置y間距
  private Label scoreLabel; // 分數顯示
  private Label levelLabel; // 等級顯示
  private Label linesLabel; // 方塊消除累計行數顯示
  private int gameOverLocationX; // 遊戲結束顯示位置x
  private int gameOverLocationY; // 遊戲結束顯示位置y
  private int nextRoundCountdownSecondLocationX; // 下局倒數秒數顯示位置x
  private int nextRoundCountdownSecondLocationY; // 下局倒數秒數顯示位置y
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

  private GameFlow gameFlow; // 遊戲邏輯(無畫面)
  private AudioPlayer backgroundMusic; // 播放背景音樂
  private InfoBar infoBar;
  private OpponentTetris opponentTetris;
  private JSONObject roomData;

  public BattleView(JSONObject params) {
    super(params);
    roomData = params;
    backgroundMusic = AudioManager.get().playMusic("sound/music.wav");
  }

  @Override
  public void init() {
    Config config = Config.get();

    boxStartX = config.zoom(62);
    boxStartY = config.zoom(79);
    singleCubeWidth = config.zoom(19);
    singleCubeHeight = config.zoom(19);
    rightNextCubeX = config.zoom(160);
    rightNextCubeHeightSpacing = config.zoom(50);

    scoreLabel = new Label();
    scoreLabel.setLocation(config.zoom(5), config.zoom(20));
    scoreLabel.setFont(Font.BOLD, config.zoom(20));
    scoreLabel.setColor(Color.RED);
    scoreLabel.setText(SCORE + 0);
    add(scoreLabel);

    levelLabel = new Label();
    levelLabel.setLocation(config.zoom(5), config.zoom(45));
    levelLabel.setFont(Font.BOLD, config.zoom(20));
    levelLabel.setColor(Color.BLACK);
    levelLabel.setText(LEVEL + 0);
    add(levelLabel);

    linesLabel = new Label();
    linesLabel.setLocation(config.zoom(5), config.zoom(70));
    linesLabel.setFont(Font.BOLD, config.zoom(20));
    linesLabel.setColor(Color.BLUE);
    linesLabel.setText(LINES + 0);
    add(linesLabel);

    // 遊戲結束
    gameOverLocationX = Config.get().zoom(100);
    gameOverLocationY = Config.get().zoom(250);
    nextRoundCountdownSecondLocationX = Config.get().zoom(155);
    nextRoundCountdownSecondLocationY = Config.get().zoom(270);

    opponentTetris = new OpponentTetris(value -> (int) (value * 1.3));
    opponentTetris.setWidth(Config.get().zoom(15));
    opponentTetris.setHeight(Config.get().zoom(15));
    opponentTetris.setLocation(Config.get().zoom((int) (getWidth() * 0.45)), Config.get().zoom(0));
    System.out.println("roomData=>" + roomData);
    int position = roomData.getInt("position");
    String userName =
        StreamSupport.stream(roomData.getJSONArray("users").spliterator(), false)
            .map(JSONObject.class::cast)
            .filter(o -> (o.getInt("position") != position))
            .findFirst()
            .get()
            .getString("name");
    opponentTetris.setUserName(userName);
    add(opponentTetris);

    // 分數、消除行數、等級
    infoBar = new InfoBar();
    // 建立遊戲邏輯
    gameFlow = new GameFlow();

    // 設定使用GameView代理遊戲邏輯進行畫面的繪圖
    gameFlow.setEventListener(this);

    // 設定方塊掉落秒數為
    gameFlow.setSecond(Config.get().getBoxFallSpeed(infoBar.getLevel()));

    // 設定下次要出現的方塊style個數為顯示3個
    cubeBuffer = getBufBox(gameFlow, nextCubeSize);

    Client.get()
        .getKcp()
        .ifPresent(
            k -> {
              k.setOnReadedListener(this::processServerMessage);
            });

    // 啟動遊戲邏輯執行緒
    gameFlow.start();
  }

  private void processServerMessage(String msg) {
    JSONObject json = new JSONObject(msg);
    int code = json.getInt("code");

    /*
     * {
     *   "code": 412,
     *   "roomId": "3b1848b0-fad0-4967-824a-ac9540f49be7",
     *   "operation": {
     *      "event": 2,
     *      "style":false
     *  }
     * }
     */
    if (code == 412) {
      JSONObject operation = json.getJSONObject("operation");

      opponentTetris.addOperation(operation);
      return;
    }
  }

  @Override
  public void onMouseClicked(MouseEvent e) {}

  // 接收鍵盤事件
  @Override
  public void onKeyCode(int code) {
    if (gameFlow.isGameOver()) {
      return;
    }
    if (code == KeyEvent.VK_ESCAPE) {
      Client.get().close();
      changeView(ViewName.MENU);
      return;
    }
    if (!gameFlow.isPause()) {
      switch (code) {
        case KeyEvent.VK_UP: // 上,順轉方塊
          if (gameFlow.turnRight()) {
            AudioManager.get().playSound("sound/turn.wav");
          }
          break;
        case KeyEvent.VK_DOWN: // 下,下移方塊
          moveDown();
          break;
        case KeyEvent.VK_LEFT: // 左,左移方塊
          gameFlow.moveLeft();
          break;
        case KeyEvent.VK_RIGHT: // 右,右移方塊
          gameFlow.moveRight();
          break;
        case KeyEvent.VK_SPACE: // 空白鍵,快速掉落方塊
          quickDown();
          break;
        default:
      }
      sendOperation(
          10,
          json -> {
            json.put("keyCode", code);
            json.put("simulation", false);
          });
    }

    // 每次按了鍵盤就將畫面重繪
    repaint();
  }

  private void moveDown() {
    if (gameFlow.moveDown()) {
      addScore(Config.get().getMoveDownScore());
    }
  }

  private void quickDown() {
    int befor = gameFlow.getNowBoxXY()[1];
    gameFlow.quickDown();
    int after = gameFlow.getNowBoxXY()[1];
    // 若方塊快速落到底，再另外加分數
    int quickDownScore = after - befor;

    if (quickDownScore > 0) {
      addScore(quickDownScore * Config.get().getQuickDownScore());
    }
  }

  private void addScore(int score) {
    infoBar.addScore(score);
    scoreLabel.setText(SCORE + infoBar.getScore());
    sendOperation(2, json -> json.put("score", infoBar.getScore()));
  }

  private void setLevel(int level) {
    infoBar.setLevel(level);
    levelLabel.setText(LEVEL + infoBar.getLevel());
  }

  private void addCleanedCount(int lines) {
    infoBar.addCleanedCount(lines);
    linesLabel.setText(LINES + infoBar.getCleanedCount());
  }

  // 雙緩衝區繪圖
  @Override
  public void onPaintComponent(Graphics canvas) {
    super.onPaintComponent(canvas);

    canvas.setColor(Color.BLACK);

    // 把整個陣列要畫的圖，畫到暫存的畫布上去(即後景)
    int[][] boxAry = gameFlow.getBoxAry();
    showBacegroundBox(boxAry, canvas);

    // 畫掉落中的方塊
    int[] xy = gameFlow.getNowBoxXY();
    int[][] box = gameFlow.getNowBoxAry();

    // 畫陰影
    shadow(xy, box, canvas, gameFlow.getDownY());

    showDownBox(xy, box, canvas);

    // 畫右邊下次要出現的方塊
    showBufferBox(cubeBuffer, canvas);

    // 顯示遊戲結束，並倒數秒數
    showGameOver(infoBar, canvas);

    super.onPaintComponent(canvas);
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
              boxStartX + (singleCubeWidth * j),
              boxStartY + (singleCubeHeight * i),
              singleCubeWidth,
              singleCubeHeight);
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
                rightNextCubeX + (singleCubeWidth * (j + 5)),
                (n * rightNextCubeHeightSpacing) + (singleCubeHeight * (i + 5)),
                singleCubeWidth,
                singleCubeHeight,
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
              boxStartX + (singleCubeWidth * (j + boxX)),
              boxStartY + (singleCubeHeight * (i + index)),
              singleCubeWidth,
              singleCubeHeight,
              true);
        }
      }
    }
  }

  private void showGameOver(InfoBar info, Graphics buffImg) {
    if (gameFlow.isGameOver()) {
      buffImg.setColor(Color.RED);
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
        boxStartX + (singleCubeWidth * x),
        boxStartY + (singleCubeHeight * y),
        singleCubeWidth,
        singleCubeHeight,
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
  public int[][][] getBufBox(GameFlow tetris, int cnt) {
    String[] bufbox = tetris.getAnyCountBox(cnt);
    int[][][] ary = new int[bufbox.length][][];
    for (int i = 0; i < bufbox.length; i++) {
      ary[i] = tetris.createBox(Integer.parseInt(bufbox[i]));
    }
    return ary;
  }

  /** 所有 */
  @Override
  public void onEvent(GameEvent code, Object data) {
    // 收到重畫自己畫面的陣列
    if (GameEvent.REPAINT == code) {
      repaint();
      return;
    }
    // 方塊下移
    if (GameEvent.BOX_MOVE_DOWN == code) {
      sendOperation(
          10,
          json -> {
            json.put("keyCode", KeyEvent.VK_DOWN);
            json.put("simulation", true);
          });
      return;
    }
    // 方塊落到底
    if (GameEvent.BOX_DOWN == code) {
      // 播放方塊掉落音效
      AudioManager.get().playSound("sound/down.wav");
      return;
    }
    // 建立完下一個方塊
    if (GameEvent.BOX_NEW == code) {
      sendOperation(
          20,
          json -> {
            json.put("style", Integer.parseInt((String) data));
          });
      return;
    }
    // 建立完下一個方塊
    if (GameEvent.BOX_NEXT == code) {
      cubeBuffer = getBufBox(gameFlow, nextCubeSize);
      return;
    }
    if (GameEvent.CLEAN_LINE_BEFORE == code) {
      Cube cube = (Cube) data;
      sendOperation(
          40,
          json -> {
            json.put("x", cube.getNowX());
            json.put("y", cube.getNowY());
            json.put("style", cube.getStyle());
            json.put("cube", Arrays.deepToString(cube.toArray()));
          });
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
      String[] lines = ((String) data).split("[,]", -1);
      addCleanedCount(lines.length);
      addScore(Config.get().getCleanLinesScore(lines.length));

      if (tryLevelUp()) {
        Debug.get().println("提升等級!!");
      }
      return;
    }
    // 計算自己垃圾方塊數
    if (GameEvent.BOX_GARBAGE == code) {

      return;
    }
    if (GameEvent.GAME_START == code) {
      sendOperation(1);
      return;
    }
    // 方塊頂到最高處，遊戲結束
    if (GameEvent.GAME_OVER == code) {
      sendOperation(30);
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
      resetScore();

      // 清除全畫面方塊
      gameFlow.clearBox();

      sendOperation(0);

      // 設定方塊掉落秒數
      gameFlow.setSecond(Config.get().getBoxFallSpeed(infoBar.getLevel()));

      // 當方塊到頂時，會自動將GameOver設為true,因此下次要開始時需設定遊戲為false表示可進行遊戲
      gameFlow.setGameOver(false);
    }
    return;
  }

  /** 試著計算是否提升等級1級，並重設方塊掉落速度 */
  private boolean tryLevelUp() {
    int currentLevel = infoBar.getLevel();
    int newLevel = Config.get().linesConvertLevel(infoBar.getCleanedCount());

    if (currentLevel != newLevel) {
      setLevel(newLevel);
      gameFlow.setSecond(Config.get().getBoxFallSpeed(infoBar.getLevel()));
      return true;
    }
    return false;
  }

  public void sendOperation(int event) {
    sendOperation(event, o -> {});
  }

  public void sendOperation(int event, Consumer<JSONObject> consumer) {
    JSONObject operation = new JSONObject();

    operation.put("event", event);
    consumer.accept(operation);

    JSONObject json = new JSONObject();
    json.put("code", 411);
    json.put("operation", operation);

    /*
     * {
     *   "code": 411,
     *   "roomId": "3b1848b0-fad0-4967-824a-ac9540f49be7",
     *   "operation": {
     *      "event": 2,
     *      "style":false
     *  }
     * }
     */
    Client.get().write(json);
  }

  private void resetScore() {
    infoBar.reset();
    scoreLabel.setText(SCORE + infoBar.getScore());
    levelLabel.setText(LEVEL + infoBar.getLevel());
    linesLabel.setText(LINES + infoBar.getCleanedCount());
  }

  @Override
  public void release() {
    super.release();
    backgroundMusic.stop();
    opponentTetris.close();
    gameFlow.stop();
  }
}
