package tetris.game;

/**
 * 各種方塊，目前設定有7種形狀可設定.
 *
 * @author Ray
 */
public class Cube {
  /** 1.長條 */
  public static final String STYLE1 = "0,0|0,1|0,2|0,3@0,0|1,0|2,0|3,0";
  /** 2.反L */
  public static final String STYLE2 =
      "0,0|1,0|1,1|1,2@0,0|0,1|1,0|2,0@0,0|0,1|0,2|1,2@0,1|1,1|2,0|2,1";
  /** 3.L */
  public static final String STYLE3 =
      "0,2|1,0|1,1|1,2@0,0|1,0|2,0|2,1@0,0|0,1|0,2|1,0@0,0|0,1|1,1|2,1";
  /** 4.正方 */
  public static final String STYLE4 = "0,0|0,1|1,0|1,1";
  /** 5.正閃電 */
  public static final String STYLE5 = "0,1|0,2|1,0|1,1@0,0|1,0|1,1|2,1";
  /** 6.T型 */
  public static final String STYLE6 =
      "0,1|1,0|1,1|1,2@0,1|1,1|1,2|2,1@1,0|1,1|1,2|2,1@0,1|1,0|1,1|2,1";
  /** 7.反閃電 */
  public static final String STYLE7 = "0,0|0,1|1,1|1,2@0,1|1,0|1,1|2,0";

  private static final String[] STYLE_LIST = {
    STYLE1, STYLE2, STYLE3, STYLE4, STYLE5, STYLE6, STYLE7
  };

  private int nowX; // 目前的x位置
  private int nowY; // 目前的y位置

  private int nowturn; // 目前的轉向
  private int style; // 此方塊種類代碼
  private int[][][] cubeMatrix; // 此方塊轉向後的值
  private int[][] sizeMatrix; // 此方塊轉向後的高、寬

  /** 初始化方塊. */
  public Cube() {
    nowturn = 0;
    cubeMatrix = new int[0][0][0];
    style = 1;
    sizeMatrix = new int[0][0];
  }

  public void resetXY() {
    nowX = 0;
    nowY = 0;
  }

  /**
   * 指定style建立方塊型狀 style: 1.長條<br>
   * 2.反L<br>
   * 3.L <br>
   * 4.正方<br>
   * 5.正閃電 <br>
   * 6.T型<br>
   * 7.反閃電 <br>
   *
   * @param s 方塊種類
   */
  public void setStyle(int s) {
    if (s < 0 || s > STYLE_LIST.length) {
      System.out.println("設定的方塊型別不存在");
      return;
    }
    String data = STYLE_LIST[s - 1];
    style = s;
    createMatrix(data);
  }

  /**
   * 設定方塊類型 "[形狀1]_高,寬@[形狀2]_高,寬..."<br>
   * 形狀:二維陣列位置1|二維陣列位置1|...<br>
   * 接受格式為"0,0|0,1|0,2|0,3@0,0|1,0|2,0|3,0"<br>
   *
   * @param data STYLE1 or STYLE2, STYLE3...
   */
  private void createMatrix(String data) {
    String[] ary = data.split("[@]");
    cubeMatrix = new int[ary.length][][];
    sizeMatrix = new int[ary.length][2];

    for (int i = 0; i < ary.length; i++) {
      String[] box = ary[i].split("[|]");
      int h = 0;
      int w = 0;

      // 找尋方塊各方裡x最大格數與y的最大格數來當寬與高,找完之後因設定值為從0開始，需要再將高、寬各+1
      for (int j = 0; j < box.length; j++) {
        String[] bary = box[j].split("[,]");
        int x = bary[0].charAt(0) - '0';
        int y = bary[1].charAt(0) - '0';

        if (x > h) {
          h = x;
        }
        if (y > w) {
          w = y;
        }
      }
      h++;
      w++;
      sizeMatrix[i][0] = h;
      sizeMatrix[i][1] = w;

      cubeMatrix[i] = new int[h][w];

      for (int j = 0; j < box.length; j++) {
        String[] bary = box[j].split("[,]");
        int x = bary[0].charAt(0) - '0';
        int y = bary[1].charAt(0) - '0';
        cubeMatrix[i][x][y] = style;
      }
    }
  }

  public int getStyle() {
    return style;
  }

  /**
   * 取得目前有幾種方塊種類.
   *
   * @return
   */
  public static int getStyleCount() {
    return STYLE_LIST.length;
  }

  /**
   * 設定目前轉向.
   *
   * @param n 方向
   */
  public void setTurn(int n) {
    nowturn = n;
  }

  /**
   * 取得目前轉向.
   *
   * @return
   */
  public int getTurn() {
    return nowturn;
  }

  /**
   * 方塊下個轉向.
   *
   * @param n 方向
   * @return
   */
  public int nextTurn(int n) {
    int tmpTurn = nowturn;

    tmpTurn += n;
    if (tmpTurn < 0) {
      tmpTurn = getTrunKind() - 1;
    }

    tmpTurn %= getTrunKind();

    return tmpTurn;
  }

  /** 逆時針轉向. */
  public void turnLeft() {
    nowturn--;
    if (nowturn < 0) {
      nowturn = getTrunKind() - 1;
    }
  }

  /** 順時針轉向. */
  public void turnRight() {
    nowturn++;
    nowturn %= getTrunKind();
  }

  /**
   * 取得方塊有幾種轉向.
   *
   * @return
   */
  public int getTrunKind() {
    return cubeMatrix.length;
  }

  /**
   * 取得指定轉向的寬.
   *
   * @param n 方向
   * @return
   */
  public int getWight(int n) {
    if (n >= 0 && n < sizeMatrix.length) {
      return sizeMatrix[n][1];
    }
    return 0;
  }

  /**
   * 取得目前轉向的寬.
   *
   * @return
   */
  public int getTurnWight() {
    return getWight(nowturn);
  }

  /**
   * 取得指定轉向的高.
   *
   * @param n 方向
   * @return
   */
  public int getHeight(int n) {
    if (n >= 0 && n < sizeMatrix.length) {
      return sizeMatrix[n][0];
    }
    return 0;
  }

  /**
   * 取得目前轉向的高.
   *
   * @return
   */
  public int getTurnHeight() {
    return getHeight(nowturn);
  }

  /**
   * 取得目前方塊形狀.
   *
   * @return
   */
  public int[][] toArray() {
    return toArray(nowturn);
  }

  /**
   * 取得指定轉向的方塊形狀.
   *
   * @param index 指定轉向
   * @return
   */
  public int[][] toArray(int index) {
    if (index >= 0 && index < cubeMatrix.length) {
      return cubeMatrix[index];
    }
    return null;
  }

  /**
   * 取得目前方塊形狀.
   *
   * @return
   */
  public String toTurnCubeString() {
    return toCubeString(nowturn);
  }

  /**
   * 取得指定轉向的方塊形狀(以字串格式)，例如:"[形狀1]_高,寬@[形狀2]_高,寬.
   *
   * @param index 指定轉向
   * @return
   */
  public String toCubeString(int index) {
    if (index >= 0 && index < cubeMatrix.length) {
      String data = STYLE_LIST[style - 1];
      String[] ary = data.split("[@]");
      return ary[index];
    }
    return "";
  }

  /**
   * 移動x幾格，y幾格,並將舊的位置記下.
   *
   * @param x 座標x
   * @param y 座標y
   */
  public void move(int x, int y) {
    nowY += y;
    nowX += x;
  }

  public void setNowX(int x) {
    nowX = x;
  }

  public int getNowX() {
    return nowX;
  }

  public int getNowY() {
    return nowY;
  }
}
