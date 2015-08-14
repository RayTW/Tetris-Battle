package tetris;

import java.util.Random;

import tetris.box.Box;
import tetris.box.CheckCleanLineThread;
import tetris.box.CleanLineI;
import tetris.box.GameBox;


/**
 * 控制遊戲流程
 * @author Ray
 *
 */
public class GameLoop implements Runnable,CleanLineI{
    private float fastSec;//快移的秒數
    private float sec;
    private Random rand;
    private GameBox gameBox;
    private boolean isRun;
    private boolean isPause;
    private boolean isGameOver;    //是否遊戲結束
    private boolean isClean;    //目前是否有方塊到底
    private boolean fastEnable;//true :啟用快移
    private ViewDelegate delegate;
    private CheckCleanLineThread checkCleanThread;

    private int flag;            //目前使用的方塊位置
    private String [] styleAry;//預戴方塊buffer區

    public GameLoop() {
        isRun = true;
        rand = new Random();
        GameLoopInit();
    }

    public void GameLoopInit(){
        flag = 0;
        styleAry = new String[0];
        sec = 0.2f;
        fastSec = 0.1f;
        gameBox = new GameBox();
        fastEnable = false;
        isPause = false;
        isGameOver = false;
        checkCleanThread = new CheckCleanLineThread();
        checkCleanThread.setObj(this);
        checkCleanThread.startThread();
        setBoxList(getRandBox(5));//設定使用5組亂數排列方塊進行遊戲
        nextCreatBox();
    }

    public void startGame(){
        new Thread(this).start();
    }

    public void stopGame(){
        isRun = false;
    }

    @Override
	public void run(){
        while(isRun){
            try {
                if(!isPause && !isGameOver){//沒有按暫停才可玩
                    if(!gameBox.moveDown()){//方塊已到底停住,不能再往下移
                        isClean = true;
                    }
                    putDelegateCode("repaint", "");
                    //printAry(gameBox.getBoxAry());
                }
                Thread.sleep((int)(1000 * (fastEnable ? fastSec:sec)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        close();
    }

    /**
     * 設定預載的方塊style
     * @param boxList
     */
    public void setBoxList(String boxList){
        if(!boxList.equals("")){
            styleAry = boxList.split("[|]");
        }
    }
    
    /**
     * 取得n組亂數排列方塊,例如取1組為:"1|5|4|3|2|6|7"
     * @param n
     * @return
     */
    public String getRandBox(int n){
        int [] boxAry = new int[Box.getStyleCount()];
        StringBuffer styleList = new StringBuffer();
        
        //初始化可使用的方塊style,目前為1~7
        for(int i = 1; i <= boxAry.length; i++){
            boxAry[i - 1] = i;
        }
        
        //將1~7方塊亂數排列後，轉成字串回傳
        for(int i = 0; i < n; i++){
            int [] ary = randBoxAry(boxAry);
            for(int j = 0; j < ary.length; j++){
                if(styleList.length() > 0){
                    styleList.append("|");
                }
                styleList.append(ary[j]);
            }
        }
        
        return styleList.toString();
    }
    
    /**
     * 亂數打亂方塊排列
     * @param ary
     * @return
     */
    private int [] randBoxAry(int [] ary){
        for(int i = 0; i < ary.length; i++){
            int tmpIndex = rand.nextInt(ary.length);
            int style = ary[tmpIndex];
            ary[tmpIndex] = ary[i];
            ary[i] = style;
        }
        return ary;
    }

    /**
     * 取出預載方塊buffer下一個方塊
     * @return
     */
    public int nextBox(){
        int style = 0;
        if(flag > styleAry.length - 1){
            flag = 0;
        }
        style = Integer.parseInt(styleAry[flag]);
        flag++;
        return style;
    }

    /**
     * 取出預載方塊buffer n個方塊
     * @param n
     * @return
     */
    public String [] getN_box(int n){
        String [] nBox = new String[n];
        int tmpFlag = flag;

        for(int i = 0; i < n; i++){
            if(tmpFlag > styleAry.length - 1){
                tmpFlag = 0;
            }
            nBox[i] = styleAry[tmpFlag];
            tmpFlag++;
        }
        return nBox;
    }

    /**
     * 控制方塊下移1格
     */
    public boolean moveDown(){
        return gameBox.moveDown();
    }

    /**
     * 控制方塊左移1格
     */
    public boolean moveLeft(){
        if(isClean) return false;

        return gameBox.moveLeft();
    }

    /**
     * 控制方塊右移1格
     */
    public boolean moveRight(){
        if(isClean) return false;

        return gameBox.moveRight();
    }

    /**
     * 控制方塊順轉1次
     */
    public boolean turnLeft(){
        if(isClean) return false;

        return gameBox.turnLeft();
    }

    /**
     * 控制方塊逆轉1次
     */
    public boolean turnRight(){
        if(isClean) return false;

        return gameBox.turnRight();
    }

    /**
     * 方塊直接掉落到定位
     */
    public void quickDown(){
        if(isClean) return;
        gameBox.quickDown();
        isClean = true;
    }


    /**
     * 新增垃圾方塊,一次新增一排,同一種style
     * @param style 新增的方塊style
     * @param lineCount 要新增幾行垃圾方塊
     */
    public void addGarbageBox(int style, int lineCount){
        for(int i = 0; i < lineCount; i++){
            gameBox.addGarbageBox_oneLine(style);
        }
    }

    /**
     * 新增垃圾方塊,一次新增一排,同一種style
     * @param styleList 新增的方塊style列表，格式為"1|2|3|4|5|6|0|0|2|3"
     */
    public void addGarbageBox_oneLine(String styleList){
        gameBox.addGarbageBox_oneLine(styleList);
    }

    /**
     * 快速下降移動
     * @param b true:啟用快移,false:關閉快移
     */
    public void fastDown(boolean b){
        fastEnable = b;
    }

    /**
     * 遊戲暫停
     */
    public void pause(){
        isPause = true;
    }

    /**
     * 目前是否暫停中
     * @return
     */
    public boolean isPause(){
        return isPause;
    }

    /**
     * 繼續遊戲(有按暫停之後使用)
     */
    public void rusme(){
        isPause = false;
    }

    /**
     * 設定GameOver狀態,true:遊戲結束,false:遊戲未結束
     * @param b
     * @return
     */
    public void setGameOver(boolean b){
        isGameOver = b;
    }

    /**
     * 取得目前是否遊戲結束
     * @return
     */
    public boolean isGameOver(){
        return isGameOver;
    }

    /**
     * 設定代理者
     * @param o
     */
    public void setDelegate(ViewDelegate o){
        delegate = o;
    }

    /**
     * 設定目前方塊掉落等待秒數
     * @param s
     */
    public void setSec(float s){
        sec = s;
    }

    /**
     * 取得目前方塊掉落等待秒數
     * @return
     */
    public float getSec(){
        return sec;
    }

    /**
     * 發送資料給代理者
     * @param code
     * @param data
     */
    public void putDelegateCode(String code, String data){
        if(delegate != null){
            delegate.tetrisEvent(code, data);
        }
    }

    /**
     * 取得目前二維陣列裡，疊的方塊到第幾個位置，0~20個單位<BR>
     * 必須在方塊落到底時呼叫(即GameLoop的cleanLine()被執行時)，才可得到正確的高度資料
     * @return
     */
    public int getNowBoxIndex(){
        return gameBox.getNowBoxIndex();
    }

    /**
     * 取得目前的整個遊戲畫面可移動方塊區域的二維陣列
     */
    public int [][] getBoxAry(){
        return gameBox.getBoxAry();
    }
    /**
     * 亂數產生方塊
     */
    public boolean randCreatBox(){
        int style = rand.nextInt(Box.getStyleCount())+1;
        return gameBox.createBaseObj(style);
    }

    /**
     * 從buffer取方塊建立
     * @return
     */
    public boolean nextCreatBox(){
        int style = nextBox();
        return gameBox.createBaseObj(style);
    }
    
    public int [][] createBox(int style){
    	return gameBox.createBox(style);
    }

    @Override
	public void cleanLine() {
        gameBox.addBox();
        putDelegateCode("repaint", "");
        putDelegateCode("boxDown", "");

        //    System.out.println("cleanLine----被執行-------");

        //取得可消除的行數
        String lineData = gameBox.getClearLine();

        if(!lineData.equals("")){
            putDelegateCode("willCleanLine", lineData);
            gameBox.clearLine(lineData);//實際將可消除的方塊行數移除
            putDelegateCode("cleanLineOK", "");
        }

        putDelegateCode("garbageBox", lineData);


        boolean isOK = nextCreatBox();//建立方塊
        if(!isOK){//建立失敗
            isGameOver = true;
            putDelegateCode("repaint", "");
            //printAry(gameBox.getBoxAry());
            putDelegateCode("gameOver", "");
        }
        putDelegateCode("repaint", "");
        putDelegateCode("creatBox", "");
        isClean = false;
        checkCleanThread = new CheckCleanLineThread();
        checkCleanThread.setObj(this);
        checkCleanThread.startThread();
    }

    /**
     * 目前掉落方塊已定格中，進行檢查可消方塊
     */
    @Override
	public boolean isClean() {
        return isClean;
    }

    /**
     * 清空整個畫面所有方塊
     */
    public void clearBox(){
        gameBox.clearBoxAry();
    }

    /**
     * 拿自己這次清掉的方塊行數去移除垃圾方塊，回傳自己還剩幾行方塊可扣掉炸彈<BR>
     * 此method僅能消掉style為9的垃圾方塊
     * @param count
     * @return
     */
    public int removeGarbageBox_oneLine(int count){
        return gameBox.removeGarbageBox_oneLine(count);
    }

    /**
     * 取得掉落中方塊
     * @return
     */
    public int [][] getNowBoxAry(){
        return gameBox.getNowBoxAry();
    }

    /**
     * 取得掉落中方塊目前的x、y位置
     * @return
     */
    public int [] getNowBoxXY(){
        return gameBox.getNowBoxXY();
    }

    /**
     * 取得到第Y個位置會撞到方塊
     * @return
     */
    public int getDownY(){
        return gameBox.getDownY();
    }


    public void close(){
        checkCleanThread.stopThread();
        delegate = null;
        rand = null;
        checkCleanThread = null;
        gameBox = null;
        styleAry = null;
    }

    /**
     * isGap為false時 取得指定index行的方塊串，格式為"1|2|3|4|5||6|7|1|2|3@1|2|3|4|5||6|7|1|2|3@..."<BR>
     * isGap為true時 取得指定index行的方塊串，格式為"1|2|3|4|5||6|7|1|0|0@1|2|3|4|5||6|7|1|2|0@..."
     *
     * @param lineData 接收格式為:17,19,5...
     * @param isGap true 取出的已被消除行數資料是未加上掉落方塊，false取出可被消行數是加上掉落方塊
     * @return
     */
    public String getLineList(String lineData,boolean isGap){
        return gameBox.getLineList(lineData, isGap);
    }

    //印出陣列
    /*    public void printAry(int [][] tmp){
        //String digit = "０１２３４５６７８９";
        System.out.println("==================");
        for(int i = 0; i < tmp.length; i++){
            System.out.print("|");
            for(int j = 0; j < tmp[i].length; j++){
                if(tmp[i][j] > 0){
                    System.out.print("口");
                    //System.out.print(digit.charAt(tmp[i][j]));
                }else{
                    System.out.print("　");
                }
            }
            System.out.println("|");
        }
        System.out.println("==================");
    }*/

    /**
     * @param args
     */
    public static void main(String[] args) {
        GameLoop gl = new GameLoop();
        //gl.startGame();
        gl.setBoxList("1|2|3|4");


        System.out.println("gl.nextBox()"+gl.nextBox());
        System.out.println("gl.nextBox()"+gl.nextBox());

        String [] b = gl.getN_box(3);

        for(int i = 0; i < b.length; i++){
            System.out.println(b[i]);
        }
    }



}
