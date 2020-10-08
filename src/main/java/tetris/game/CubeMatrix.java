package tetris.game;

public class CubeMatrix {
  public int cubeRowCount = 20; // 直的20個方塊
  public int cubeColumnCount = 10; // 橫的10個方塊
  private int[][] cube; // 整個可以放置方塊、移動方塊的大陣列

  private Cube currentCube; // 目前移動中的方塊

  public CubeMatrix() {
    initialize();
  }

  public void initialize() {
    cube = new int[cubeRowCount][cubeColumnCount];
  }

  // ----------------------------public
  // method------------begin--------------------
  /**
   * 建立新的方塊,回傳:true:建立成功，方塊未撞到其他方塊,false:建立失敗，方塊撞到其他方塊.
   *
   * @param style 方塊種類
   * @return
   */
  public boolean createNewCube(int style) {
    boolean b = true;
    Cube nb = new Cube();
    nb.setStyle(style);
    nb.setNowX((cubeColumnCount / 2) - 1);
    currentCube = nb;

    if (hitTest(cube, currentCube)) { // 方塊一建立就撞到
      b = false;
    }
    return b;
  }

  public int[][] createCube(int style) {
    Cube b = new Cube();
    b.setStyle(style);
    return b.toArray(0);
  }

  public boolean tryMoveDown() {
    Cube tempNowBox = currentCube;

    // 判斷方塊撞到底部
    int boxY = tempNowBox.getNowY() + tempNowBox.getTurnHeight();
    if (boxY >= cubeRowCount) {
      return false;
    }

    int[][] nbAry = tempNowBox.toArray();
    int nx = tempNowBox.getNowX();
    int ny = tempNowBox.getNowY();

    return !hitTest(cube, nbAry, nx, ny + 1);
  }

  /**
   * 向下移1格,回傳是否可再往下移 true:可繼續下移,false:不可往下移.
   *
   * @return
   */
  public boolean moveDown() {
    Cube tempNowBox = currentCube;

    if (tryMoveDown()) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
      tempNowBox.move(0, 1);
      return true;
    }
    return false;
  }

  /**
   * 方塊右移,回傳是否可再往右移 true:可繼續右移,false:不可往右移
   *
   * @return
   */
  public boolean moveRight() {
    boolean b = true;
    Cube tempNowBox = currentCube;
    // 取得目前方塊右移後的x位置+上方塊的寬度
    int x = tempNowBox.getNowX() + tempNowBox.getTurnWight() + 1; // 假設右移1格

    if (x > cubeColumnCount) { // 判斷方塊位置是否會超過boxAry的寬度
      b = false;
    } else { // 沒超過牆，判斷是否撞到其他方塊
      if (hitTest(
          cube,
          tempNowBox.toArray(),
          tempNowBox.getNowX() + 1,
          tempNowBox.getNowY())) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
        b = false;
      } else {
        tempNowBox.move(1, 0);
      }
    }

    return b;
  }

  /**
   * 方塊左移1格,回傳是否可再往左移 true:可繼續左移,false:不可往左移
   *
   * @return
   */
  public boolean moveLeft() {
    boolean b = true;
    Cube tempNowBox = currentCube;
    int[][] nowturnBoxAry = tempNowBox.toArray();
    int x = tempNowBox.getNowX() - 1;

    // 判斷當方塊移到最左邊格子時，方塊本身是否可再左移
    if (x == -1) {
      for (int i = 0; i < nowturnBoxAry.length; i++) {
        if (nowturnBoxAry[i][0] > 0) {
          return false;
        }
      }
    }

    if (x < -1) {
      b = false;
    } else {
      if (hitTest(
          cube,
          nowturnBoxAry,
          tempNowBox.getNowX() - 1,
          tempNowBox.getNowY())) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
        b = false;
      } else {
        tempNowBox.move(-1, 0);
      }
    }

    return b;
  }

  /** 方塊直接掉落到定位 */
  public void quickDown() {
    while (true) {
      if (!moveDown()) {
        break;
      }
    }
  }

  /**
   * 順時針轉,true:可繼續順轉,false:不能順轉
   *
   * @return
   */
  public boolean turnRight() {
    Cube tempNowBox = currentCube;
    int rightTurn = tempNowBox.nextTurn(1); // 取出順轉1次後的方塊位置索引值
    int turnW = tempNowBox.getWight(rightTurn);
    int turnH = tempNowBox.getHeight(rightTurn);
    int h = tempNowBox.getNowY() + turnH;
    int ny = tempNowBox.getNowY();
    int nx = tempNowBox.getNowX();
    int w = tempNowBox.getNowX() + turnW;
    int[][] turnAry = tempNowBox.toArray(rightTurn);

    if (h > cubeRowCount) { // 判斷順轉後的方塊位置是否會超過boxAry的寬度或底部
      return false;
    }
    if (nx == -1) {
      // if(w > BOX_W){//判斷是否轉向超出右牆,再判斷超出後，方塊左移之後是否會撞到其他方塊
      int tmpX = 1;

      if (hitTest(cube, turnAry, nx + tmpX, ny)) {
        return false;
      } else {
        tempNowBox.move(tmpX, 0);
        nx = tempNowBox.getNowX();
      }
      // }

      if (hitTest(cube, turnAry, nx, ny)) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
        return false;
      } else {
        tempNowBox.turnRight();
        return true;
      }
    } else {
      if (w > cubeColumnCount) { // 判斷是否轉向超出右牆,再判斷超出後，方塊左移之後是否會撞到其他方塊
        int tmpX = w - cubeColumnCount;

        if (hitTest(cube, turnAry, nx + (-tmpX), ny)) {
          return false;
        } else {
          tempNowBox.move(-tmpX, 0);
          nx = tempNowBox.getNowX();
        }
      }

      if (hitTest(cube, turnAry, nx, ny)) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
        return false;
      } else {
        tempNowBox.turnRight();
        return true;
      }
    }
  }

  /**
   * 逆時針轉,true:可繼續逆轉,false:不能逆轉
   *
   * @return
   */
  public boolean turnLeft() {
    Cube tempNowBox = currentCube;
    int leftTurn = tempNowBox.nextTurn(-1); // 取出逆轉1次後的方塊位置索引值
    int turnH = tempNowBox.getHeight(leftTurn) + tempNowBox.getNowY();
    int turnW = tempNowBox.getWight(leftTurn);
    int w = tempNowBox.getNowX() + turnW;
    int nx = tempNowBox.getNowX();
    int ny = tempNowBox.getNowY();
    int[][] turnAry = tempNowBox.toArray(leftTurn);

    if (turnH > cubeRowCount) { // 判斷轉向是否超出高度
      return false;
    }
    if (nx == -1) {
      // if(w > BOX_W){//判斷是否轉向超出右牆,再判斷超出後，方塊左移之後是否會撞到其他方塊
      int tmpX = 1;

      if (hitTest(cube, turnAry, nx + tmpX, ny)) {
        return false;
      } else {
        tempNowBox.move(tmpX, 0);
        nx = tempNowBox.getNowX();
      }
      // }

      if (hitTest(cube, turnAry, nx, ny)) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
        return false;
      } else {
        tempNowBox.turnRight();
        return true;
      }
    } else { // 沒超過高度
      if (w > cubeColumnCount) { // 判斷是否超過寬度
        int tmpX = w - cubeColumnCount;

        if (hitTest(cube, turnAry, nx + (-tmpX), ny)) { // 判斷轉向後是否撞到其他方塊
          return false;
        } else { // 方塊轉向後超出寬度，因此將方塊左移tmpX格
          tempNowBox.move(-tmpX, 0);
          nx = tempNowBox.getNowX();
        }
      }

      if (hitTest(cube, turnAry, nx, ny)) { // 判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
        return false;
      } else { // 方塊可逆轉
        tempNowBox.turnLeft();
        return true;
      }
    }
  }

  /**
   * 真正清除整行滿的方塊
   *
   * @param tempBoxAry
   * @param startIndex
   * @param line
   * @return
   */
  public void clearLine(String lineData) {
    if (!lineData.isEmpty()) {
      String[] data = lineData.split("[,]");

      for (int i = 0; i < data.length; i++) {
        int line = Integer.parseInt(data[i]);
        moveLine(cube, 0, line);
      }
    }
  }

  /** 清空整個可移動方塊區域的二維陣列 */
  public void clearAllCube() {
    for (int i = 0; i < cube.length; i++) {
      for (int j = 0; j < cube[i].length; j++) {
        cube[i][j] = 0;
      }
    }
  }

  /** 取得目前的整個遊戲畫面可移動方塊區域的二維陣列 */
  public int[][] getMatrix() {
    return cube;
  }

  /**
   * 取得目前二維陣列裡，疊的方塊到第幾個位置，0~20個單位<br>
   * 必須在方塊落到底時呼叫(即GameLoop的cleanLine()被執行時)，才可得到正確的高度資料
   *
   * @return
   */
  public int getCurrentCubeIndex() {
    int index = cube.length + 1;
    for (int i = 0; i < cube.length; i++) {
      index--;
      for (int j = 0; j < cube[i].length; j++) {
        if (cube[i][j] > 0) {
          return index;
        }
      }
    }
    return index;
  }

  /**
   * 取得可清除的行數資料,例如: "17,19" 表示可消除第17、19行,style為9表示不可消除的垃圾方塊
   *
   * @return
   */
  public String getClearLine() {
    Cube nb = currentCube;
    StringBuffer lineList = new StringBuffer();
    int ny = nb.getNowY();

    for (int i = ny; i < cube.length; i++) {
      int BW = cube[i].length;
      int checkCnt = 0;
      for (int j = 0; j < BW; j++) {
        int boxStyle = cube[i][j];
        if (boxStyle > 0 && boxStyle != 9) {
          checkCnt++;
          if (checkCnt == BW) {
            if (lineList.length() > 0) {
              lineList.append(",");
            }
            lineList.append(i);
          }
        }
      }
    }
    return lineList.toString();
  }

  /**
   * 新增垃圾方塊,一次新增一排,同一種style
   *
   * @param style 新增的方塊style
   */
  public void addGarbageBoxOneLine(int style) {
    int[][] tmpAry = cube;
    int[] tmp = tmpAry[0];

    int i = 0;
    while (i < tmpAry.length - 1) {
      tmpAry[i] = tmpAry[i + 1];
      i++;
    }

    tmpAry[i] = tmp;
    for (int j = 0; j < tmpAry[i].length; j++) {
      tmpAry[i][j] = style;
    }
  }

  /**
   * 新增垃圾方塊,一次新增一排,同一種style
   *
   * @param styleList 新增的方塊style列表，格式為"1|2|3|4|5|6|0|0|2|3"
   */
  public void addGarbageBoxOneLine(String styleList) {
    String[] styleAry = styleList.split("[|]");
    int[][] tmpAry = cube;
    int[] tmp = tmpAry[0];

    int i = 0;
    while (i < tmpAry.length - 1) {
      tmpAry[i] = tmpAry[i + 1];
      i++;
    }

    tmpAry[i] = tmp;
    for (int j = 0; j < tmpAry[i].length && j < styleAry.length; j++) {
      int style = Integer.parseInt(styleAry[j]);
      if (style > 0) { // 判斷收到的方塊是其他顏色全轉成方塊8的顏色
        style = 8;
      }
      tmpAry[i][j] = style;
    }
  }

  /**
   * 拿自己這次清掉的方塊行數去移除垃圾方塊，回傳自己還剩幾行方塊可扣掉炸彈<br>
   * 此method僅能消掉style為9的垃圾方塊
   *
   * @param count
   * @return
   */
  public int removeGarbageBoxOneLine(int count) {
    if (count == 0) {
      return 0;
    }

    int lineCount = count; // 這次自己消掉幾行方塊
    int garbageCount = 0; // 自己目前的垃圾方塊數

    // 計算自己有幾行不能消掉的垃圾方塊
    for (int i = cube.length - 1; i >= 0; i--) {
      int style = cube[i][0];
      if (style == 9) {
        garbageCount++;
      }
    }

    // 以自己消除的行數去清除垃圾方塊行數
    for (int i = count - 1; i >= 0 && garbageCount > 0; i--) {
      // int line = Integer.parseInt(data[i]);
      moveLine(cube, 0, cube.length - 1);
      garbageCount--;
      lineCount--;
    }

    return lineCount;
  }

  /**
   * 取得掉落中方塊二維陣列
   *
   * @return
   */
  public int[][] getCurrentCube() {
    return currentCube.toArray();
  }

  /**
   * 取得掉落中方塊物件
   *
   * @return
   */
  public Cube getCube() {
    return currentCube;
  }

  /**
   * 取得掉落中方塊
   *
   * @return
   */
  public String getCurrentCubeStyleString() {
    return currentCube.toTurnCubeString();
  }

  public int getCurrentCubeStyle() {
    return currentCube.getStyle();
  }

  /**
   * 取得掉落中方塊目前的x、y位置,w,h寬高
   *
   * @return
   */
  public int[] getNowBoxXY() {
    Cube tempNowBox = currentCube;
    int[] xy = new int[4];
    xy[0] = tempNowBox.getNowX();
    xy[1] = tempNowBox.getNowY();
    xy[2] = tempNowBox.getTurnWight();
    xy[3] = tempNowBox.getTurnHeight();

    return xy;
  }

  /**
   * 取得到第Y個位置會撞到方塊,回傳目前掉落中的方塊會撞到的底或其他方塊之後定格位置
   *
   * @return
   */
  public int getDownY() {
    Cube tempNowBox = currentCube;
    int nx = tempNowBox.getNowX();
    int ny = tempNowBox.getNowY();
    int[][] nbAry = tempNowBox.toArray();

    while (true) {
      // 判斷方塊撞到底部
      int boxY = ny + tempNowBox.getTurnHeight();
      if (boxY >= cubeRowCount) {
        return ny;
      }

      if (hitTest(cube, nbAry, nx, ny + 1)) { // 方塊沒碰撞到其他方塊，可下移
        break;
      } else {
        ny++;
      }
    }
    return ny;
  }

  /**
   * 畫上新位置方塊
   *
   * @param c
   */
  public void addBox(Cube c) {
    int[][] tempBoxAry = cube;
    int x = c.getNowX();
    int y = c.getNowY();
    int[][] b = c.toArray();

    for (int i = 0; i < b.length; i++) {
      for (int j = 0; j < b[i].length; j++) {
        if (b[i][j] > 0) tempBoxAry[i + y][j + x] = c.getStyle();
      }
    }
  }

  public void addBox(int[][] b, int x, int y, int style) {
    int[][] tempBoxAry = cube;

    for (int i = 0; i < b.length; i++) {
      for (int j = 0; j < b[i].length; j++) {
        if (b[i][j] > 0) tempBoxAry[i + y][j + x] = style;
      }
    }
  }

  /**
   * @param lineData
   * @param isGap
   * @return
   */
  public String getLineList(String lineData, boolean isGap) {
    Cube nb = currentCube;
    int[][] tempBoxAry = cube;
    StringBuffer lineListStr = new StringBuffer();
    String[] lineAry = lineData.split("[,]");

    if (isGap) {
      int[][] downBox = nb.toArray();
      int nx = nb.getNowX();
      int ny = nb.getNowY();
      for (int i = 0; i < downBox.length; i++) {
        for (int j = 0; j < downBox[i].length; j++) {
          if (downBox[i][j] > 0) {
            for (int k = 0; k < lineAry.length; k++) {
              if (lineAry[k].equals(String.valueOf(i + ny))) {
                tempBoxAry[i + ny][j + nx] = 0;
                break;
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < lineAry.length; i++) {
      int line = Integer.parseInt(lineAry[i]);

      int[] box = cube[line];
      StringBuffer lineStr = new StringBuffer();
      for (int j = 0; j < box.length; j++) {
        if (lineStr.length() > 0) {
          lineStr.append("|");
        }
        lineStr.append(box[j]);
      }
      if (lineListStr.length() > 0) {
        lineListStr.append("@");
      }
      lineListStr.append(lineStr);
      lineStr.setLength(0);
    }
    return lineListStr.toString();
  }

  // ------------------------------public
  // method--------------end--------------------

  // ------------------------------private method----begin-------------------

  /**
   * 判斷目前的方塊，是否碰撞到boxAry裡其他的方塊
   *
   * @param tempBoxAry
   * @param nb
   * @return
   */
  protected boolean hitTest(int[][] tempBoxAry, Cube nb) {
    int[][] newBox = nb.toArray();
    int ny = nb.getNowY();
    int nx = nb.getNowX();

    return hitTest(tempBoxAry, newBox, nx, ny);
  }

  /**
   * 檢查方塊碰撞
   *
   * @param tempBoxAry 20 x 10的大陣列
   * @param boxBaseAry 要新增上去的方塊陣列
   * @param nx 方塊目前的x位置
   * @param ny 方塊目前的y位置
   * @return
   */
  protected boolean hitTest(int[][] tempBoxAry, int[][] boxBaseAry, int nx, int ny) {
    for (int i = 0; i < boxBaseAry.length; i++) {
      for (int j = 0; j < boxBaseAry[i].length; j++) {
        int nbNum = boxBaseAry[i][j];
        if (nbNum > 0) {
          int byNum = tempBoxAry[i + ny][j + nx];
          if (byNum > 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 清除整橫排滿的方塊
   *
   * @param tmpAry
   * @param startIndex 開始的位置
   * @param endIndex 結束的位置
   */
  protected void moveLine(int[][] tmpAry, int startIndex, int endIndex) {
    int[] tmp = tmpAry[endIndex];

    for (int i = endIndex - 1; i >= startIndex; i--) {
      tmpAry[i + 1] = tmpAry[i];
    }
    tmpAry[startIndex] = tmp;
    for (int j = 0; j < tmpAry[startIndex].length; j++) {
      tmpAry[startIndex][j] = 0;
    }
  }

  // ------------------------------private method----end-------------------

  // 測試用---------------------begin-----------------

  /** 一開始時的預設地圖 */
  public void defaultMap() {
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
    addGarbageBoxOneLine(9);
  }

  // 測試用---------------------end-----------------

}
