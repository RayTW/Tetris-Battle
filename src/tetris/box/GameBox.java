package tetris.box;

public class GameBox{
    public int BOX_H = 20;
    public int BOX_W = 10;
    private int [][] boxAry;    //整個可以放置方塊、移動方塊的大陣列

    private Box nowBox;        //目前移動中的方塊    

    public GameBox() {
        GameBoxInit();
        //defaultMap();
    }

    public void GameBoxInit(){
        boxAry = new int[BOX_H][BOX_W];
    }

    //----------------------------public method------------begin--------------------
    /**
     * 建立新的方塊,回傳:true:建立成功，方塊未撞到其他方塊,false:建立失敗，方塊撞到其他方塊
     * @param style
     * @return
     */
    public boolean createBaseObj(int style){
        boolean b = true;
        Box nb = new Box();
        nb.setBoxData(style);
        nb.nowX = (BOX_W / 2) - 1;
        nowBox = nb;

        if(hitTest(boxAry, nowBox)){//方塊一建立就撞到
            b = false;
        }
        return b;
    }
    
    public int [][] createBox(int style){
    	Box b = new Box();
    	b.setBoxData(style);
    	return b.getBoxAry(0);
    }

    /**
     * 向下移1格,回傳是否可再往下移 true:可繼續下移,false:不可往下移
     * @return
     */
    public boolean moveDown(){
        Box tempNowBox = nowBox;

//        判斷方塊撞到底部
        int boxY = tempNowBox.nowY + tempNowBox.getNowTurnHeight();
        if(boxY >= BOX_H){
            return false;
        }

        boolean b = true;
        int [][] nbAry = tempNowBox.getNowturnBoxAry();
        int nx = tempNowBox.nowX;
        int ny = tempNowBox.nowY;

        if(hitTest(boxAry, nbAry, nx, ny + 1)){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
            b = false;
        }else{//方塊沒碰撞到其他方塊，可下移
            tempNowBox.move(0, 1);
        }
        return b;
    }

    /**
     * 方塊右移,回傳是否可再往右移 true:可繼續右移,false:不可往右移
     * @return
     */
    public boolean moveRight(){
        boolean b = true;
        Box tempNowBox = nowBox;
        //取得目前方塊右移後的x位置+上方塊的寬度
        int x = tempNowBox.nowX + tempNowBox.getNowTurnWight()+1;//假設右移1格

        if(x > BOX_W){//判斷方塊位置是否會超過boxAry的寬度
            b = false;
        }else{//沒超過牆，判斷是否撞到其他方塊
            if(hitTest(boxAry, tempNowBox.getNowturnBoxAry(), tempNowBox.nowX + 1, tempNowBox.nowY)){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
                b = false;
            }else{
                tempNowBox.move(1, 0);
            }
        }

        return b;
    }

    /**
     * 方塊左移1格,回傳是否可再往左移 true:可繼續左移,false:不可往左移
     * @return
     */
    public boolean moveLeft(){
        boolean b = true;
        Box tempNowBox = nowBox;
        int [][] nowturnBoxAry = tempNowBox.getNowturnBoxAry();
        int x = tempNowBox.nowX - 1;

        //判斷當方塊移到最左邊格子時，方塊本身是否可再左移
        if(x == -1){
            for(int i = 0; i < nowturnBoxAry.length; i++){
                if(nowturnBoxAry[i][0] > 0){
                    return false;
                }
            }
        }

        if( x < -1){
            b = false;
        }else{
            if(hitTest(boxAry, nowturnBoxAry, tempNowBox.nowX - 1, tempNowBox.nowY)){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
                b = false;
            }else{
                tempNowBox.move(-1, 0);
            }
        }

        return b;
    }

    /**
     * 方塊直接掉落到定位
     */
    public void quickDown(){
        while(true){
            if(!moveDown()){
                break;
            }
        }
    }

    /**
     * 順時針轉,true:可繼續順轉,false:不能順轉
     * @return
     */
    public boolean turnRight(){
        Box tempNowBox = nowBox;
        int rightTurn = tempNowBox.nextTurn(1);//取出順轉1次後的方塊位置索引值
        int turnW = tempNowBox.getWight(rightTurn);
        int turnH = tempNowBox.getHeight(rightTurn);
        int h = tempNowBox.nowY + turnH;
        int ny = tempNowBox.nowY;
        int nx = tempNowBox.nowX;
        int w = tempNowBox.nowX + turnW;
        int [][] turnAry = tempNowBox.getBoxAry(rightTurn);

        if(h > BOX_H){//判斷順轉後的方塊位置是否會超過boxAry的寬度或底部
            return false;
        }if(nx == -1){
            //if(w > BOX_W){//判斷是否轉向超出右牆,再判斷超出後，方塊左移之後是否會撞到其他方塊
                int tmpX = 1;

                if(hitTest(boxAry, turnAry, nx + tmpX, ny) ){
                    return false;
                }else{
                    tempNowBox.move(tmpX, 0);
                    nx = tempNowBox.nowX;
                }
            //}

            if(hitTest(boxAry, turnAry, nx, ny) ){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
                return false;
            }else{
                tempNowBox.turnRight();
                return true;
            }
        }else{
            if(w > BOX_W){//判斷是否轉向超出右牆,再判斷超出後，方塊左移之後是否會撞到其他方塊
                int tmpX = w - BOX_W;

                if(hitTest(boxAry, turnAry, nx + (-tmpX), ny) ){
                    return false;
                }else{
                    tempNowBox.move(-tmpX, 0);
                    nx = tempNowBox.nowX;
                }
            }

            if(hitTest(boxAry, turnAry, nx, ny) ){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
                return false;
            }else{
                tempNowBox.turnRight();
                return true;
            }
        }
    }

    /**
     * 逆時針轉,true:可繼續逆轉,false:不能逆轉
     * @return
     */
    public boolean turnLeft(){
        Box tempNowBox = nowBox;
        int leftTurn = tempNowBox.nextTurn(-1);//取出逆轉1次後的方塊位置索引值
        int turnH = tempNowBox.getHeight(leftTurn) + tempNowBox.nowY;
        int turnW = tempNowBox.getWight(leftTurn);
        int w = tempNowBox.nowX + turnW;
        int nx = tempNowBox.nowX;
        int ny = tempNowBox.nowY;
        int [][] turnAry = tempNowBox.getBoxAry(leftTurn);

        if(turnH > BOX_H){//判斷轉向是否超出高度
            return false;
        }if(nx == -1){
            //if(w > BOX_W){//判斷是否轉向超出右牆,再判斷超出後，方塊左移之後是否會撞到其他方塊
                int tmpX = 1;

                if(hitTest(boxAry, turnAry, nx + tmpX, ny) ){
                    return false;
                }else{
                    tempNowBox.move(tmpX, 0);
                    nx = tempNowBox.nowX;
                }
            //}

            if(hitTest(boxAry, turnAry, nx, ny) ){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
                return false;
            }else{
                tempNowBox.turnRight();
                return true;
            }
        }else{//沒超過高度
            if(w > BOX_W){//判斷是否超過寬度
                int tmpX = w - BOX_W;

                if(hitTest(boxAry, turnAry, nx + (-tmpX), ny) ){//判斷轉向後是否撞到其他方塊
                    return false;
                }else{//方塊轉向後超出寬度，因此將方塊左移tmpX格
                    tempNowBox.move(-tmpX, 0);
                    nx = tempNowBox.nowX;
                }
            }

            if(hitTest(boxAry, turnAry, nx, ny) ){//判斷目前位置的方塊是否撞到其他在大boxAry裡的方塊格
                return false;
            }else{//方塊可逆轉
                tempNowBox.turnLeft();
                return true;
            }
        }
    }

    /**
     * 真正清除整行滿的方塊
     * @param tempBoxAry
     * @param startIndex
     * @param line
     * @return
     */
    public void clearLine(String lineData){
        if(!lineData.equals("")){
            String [] data = lineData.split("[,]");

            for(int i = 0; i < data.length; i++){
                int line = Integer.parseInt(data[i]);
                moveLine(boxAry, 0, line);
            }
        }
    }

    /**
     * 清空整個可移動方塊區域的二維陣列
     */
    public void clearBoxAry(){
        for(int i = 0; i < boxAry.length; i++){
            for(int j = 0; j < boxAry[i].length; j++){
                //System.out.println("清方塊"+boxAry[i][j]);
                boxAry[i][j] = 0;
            }
        }
    }

    /**
     * 取得目前的整個遊戲畫面可移動方塊區域的二維陣列
     */
    public int [][] getBoxAry(){
        return boxAry;
    }

    /**
     * 取得目前二維陣列裡，疊的方塊到第幾個位置，0~20個單位<BR>
     * 必須在方塊落到底時呼叫(即GameLoop的cleanLine()被執行時)，才可得到正確的高度資料
     * @return
     */
    public int getNowBoxIndex(){
        int index = boxAry.length + 1;
        for(int i = 0; i < boxAry.length; i++){
            index--;
            for(int j = 0; j < boxAry[i].length; j++){
                if(boxAry[i][j] > 0){
                    return index;
                }
            }
        }
        return index;
    }

    /**
     * 取得可清除的行數資料,例如: "17,19" 表示可消除第17、19行,style為9表示不可消除的垃圾方塊
     * @return
     */
    public String getClearLine(){
        Box nb = nowBox;
        StringBuffer lineList = new StringBuffer();
        int ny = nb.nowY;
        //System.out.println("從第["+ny+"]行開始找方塊可消"+nb.nowX);

        for(int i = ny; i < boxAry.length; i++){
            int BW = boxAry[i].length;
            int checkCnt = 0;
            for(int j = 0; j < BW; j++){
                int boxStyle = boxAry[i][j];
                if(boxStyle > 0 && boxStyle != 9){
                    checkCnt++;
                    if(checkCnt == BW){
                        if(lineList.length() > 0){
                            lineList.append(",");
                        }
                        lineList.append(i);
                        //System.out.println("可消掉第["+i+"]行");
                    }
                }
            }
        }
        return lineList.toString();
    }

    /**
     * 新增垃圾方塊,一次新增一排,同一種style
     * @param style 新增的方塊style
     */
    public void addGarbageBox_oneLine(int style){
        int [][] tmpAry = boxAry;
        int [] tmp = tmpAry[0];

        int i = 0;
        while(i < tmpAry.length - 1){
            tmpAry[i] = tmpAry[i + 1];
            i++;
        }

        tmpAry[i] = tmp;
        for(int j = 0; j < tmpAry[i].length; j++){
            tmpAry[i][j] = style;
        }
    }


    /**
     * 新增垃圾方塊,一次新增一排,同一種style
     * @param styleList 新增的方塊style列表，格式為"1|2|3|4|5|6|0|0|2|3"
     */
    public void addGarbageBox_oneLine(String styleList){
        String [] styleAry = styleList.split("[|]");
        int [][] tmpAry = boxAry;
        int [] tmp = tmpAry[0];

        int i = 0;
        while(i < tmpAry.length - 1){
            tmpAry[i] = tmpAry[i + 1];
            i++;
        }

        tmpAry[i] = tmp;
        for(int j = 0; j < tmpAry[i].length && j < styleAry.length; j++){
            int style = Integer.parseInt(styleAry[j]);
            if(style > 0){//判斷收到的方塊是其他顏色全轉成方塊8的顏色
                style = 8;
            }
            tmpAry[i][j] = style;
        }
    }

    /**
     * 拿自己這次清掉的方塊行數去移除垃圾方塊，回傳自己還剩幾行方塊可扣掉炸彈<BR>
     * 此method僅能消掉style為9的垃圾方塊
     * @param count
     * @return
     */
    public int removeGarbageBox_oneLine(int count){
        if(count == 0){
            return 0;
        }

        int lineCount = count;//這次自己消掉幾行方塊
        int garbageCount = 0;//自己目前的垃圾方塊數

        //計算自己有幾行不能消掉的垃圾方塊
        for(int i =  boxAry.length - 1; i >= 0; i-- ){
            int style = boxAry[i][0];
            if(style == 9){
                garbageCount++;
            }
        }

        //以自己消除的行數去清除垃圾方塊行數
        for(int i = count - 1; i >= 0 && garbageCount > 0; i--){
            //int line = Integer.parseInt(data[i]);
            moveLine(boxAry, 0, boxAry.length - 1);
            garbageCount--;
            lineCount--;
        }

        return lineCount;
    }

    /**
     * 取得掉落中方塊
     * @return
     */
    public int [][] getNowBoxAry(){
        return nowBox.getNowturnBoxAry();
    }

    /**
     * 取得掉落中方塊
     * @return
     */
    public String getNowturnBoxStyleStr(){
        return nowBox.getNowturnBoxStyleStr();
    }

    /**
     * 取得掉落中方塊目前的x、y位置,w,h寬高
     * @return
     */
    public int [] getNowBoxXY(){
        Box tempNowBox = nowBox;
        int [] xy = new int[4];
        xy[0] = tempNowBox.nowX;
        xy[1] = tempNowBox.nowY;
        xy[2] = tempNowBox.getNowTurnWight();
        xy[3] = tempNowBox.getNowTurnHeight();

        return xy;
    }

    /**
     * 取得到第Y個位置會撞到方塊,回傳目前掉落中的方塊會撞到的底或其他方塊之後定格位置
     * @return
     */
    public int getDownY(){
        Box tempNowBox = nowBox;
        int nx = tempNowBox.nowX;
        int ny = tempNowBox.nowY;
        int [][] nbAry = tempNowBox.getNowturnBoxAry();

        while(true){
//            判斷方塊撞到底部
            int boxY = ny + tempNowBox.getNowTurnHeight();
            if(boxY >= BOX_H){
                return ny;
            }

            if(hitTest(boxAry, nbAry, nx, ny + 1)){//方塊沒碰撞到其他方塊，可下移
                break;
            }else{
                ny++;
            }
        }
        return ny;
    }

    /**
     * 畫上新位置方塊
     * @param b
     * @param x
     * @param y
     */
    public void addBox(){
        Box nb = nowBox;
        int [][] tempBoxAry = boxAry;
        int x = nb.nowX;
        int y = nb.nowY;
        int [][] b = nb.getNowturnBoxAry();

        for(int i = 0; i < b.length; i++){
            for(int j = 0; j < b[i].length; j++){
                if(b[i][j] > 0)
                    tempBoxAry[i+y][j+x] = nb.getStyle();
            }
        }
    }

    /**
     *
     * @param lineData
     * @param isGap
     * @return
     */
    public String getLineList(String lineData,boolean isGap){
        Box nb = nowBox;
        int [][] tempBoxAry = boxAry;
        StringBuffer lineListStr = new StringBuffer();
        String [] lineAry = lineData.split("[,]");

        if(isGap){
            int [][] downBox = nb.getNowturnBoxAry();
            int nx = nb.nowX;
            int ny = nb.nowY;
            for(int i = 0; i < downBox.length; i++){
                for(int j = 0; j < downBox[i].length; j++){
                    if(downBox[i][j] > 0){
                        //System.out.println("清空["+(i+oy)+"]["+(j+ox)+"]");
                        for(int k = 0; k < lineAry.length; k++){
                            if(lineAry[k].equals(String.valueOf(i+ny))){
                                tempBoxAry[i+ny][j+nx] = 0;
                                break;
                            }
                        }

                    }
                }
            }
        }


        for(int i = 0; i < lineAry.length; i++){
            int line = Integer.parseInt(lineAry[i]);

            int [] box = boxAry[line];
            StringBuffer lineStr = new StringBuffer();
            for(int j = 0; j < box.length; j++){
                if(lineStr.length() > 0){
                    lineStr.append("|");
                }
                lineStr.append(box[j]);
            }
            if(lineListStr.length() > 0){
                lineListStr.append("@");
            }
            lineListStr.append(lineStr);
            lineStr.setLength(0);
        }
        return lineListStr.toString();
    }

    //------------------------------public method--------------end--------------------



    //------------------------------private method----begin-------------------

    /**
     * 判斷目前的方塊，是否碰撞到boxAry裡其他的方塊
     * @param tempBoxAry
     * @param nb
     * @return
     */
    protected boolean hitTest(int [][] tempBoxAry,Box nb){
        int [][] newBox = nb.getNowturnBoxAry();
        int ny = nb.nowY;
        int nx = nb.nowX;

        return hitTest(tempBoxAry, newBox, nx, ny);
    }

    /**
     * 檢查方塊碰撞
     * @param tempBoxAry    20 x 10的大陣列
     * @param boxBaseAry    要新增上去的方塊陣列
     * @param nx            方塊目前的x位置
     * @param ny            方塊目前的y位置
     * @return
     */
    protected boolean hitTest(int [][] tempBoxAry, int [][] boxBaseAry, int nx, int ny){
        for(int i = 0; i < boxBaseAry.length; i++){
            for(int j = 0; j < boxBaseAry[i].length; j++){
                //System.out.println("方塊點["+i+"]["+j+"],碰撞點["+(i+ny)+"]["+(j+nx)+"]");
                int nbNum = boxBaseAry[i][j];
                if(nbNum > 0){
                    int byNum = tempBoxAry[i+ny][j+nx];
                    if(byNum > 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 清除整橫排滿的方塊
     * @param tmpAry
     * @param startIndex    開始的位置
     * @param endIndex        結束的位置
     */
    protected void moveLine(int [][] tmpAry, int startIndex, int endIndex){
        int [] tmp = tmpAry[endIndex];

        for(int i = endIndex - 1; i >= startIndex; i--){
            tmpAry[i+1] = tmpAry[i];
        }
        tmpAry[startIndex] = tmp;
        for(int j = 0; j < tmpAry[startIndex].length; j++){
            tmpAry[startIndex][j] = 0;
        }
    }


//    ------------------------------private method----end-------------------


    //測試用---------------------begin-----------------

    /**
     * 一開始時的預設地圖
     */
    public void defaultMap(){
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
        addGarbageBox_oneLine(9);
    }

//    測試用---------------------end-----------------

}
