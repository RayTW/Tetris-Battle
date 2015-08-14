package tetris.box;

/**
 * 各種方塊，目前設定有7種形狀可設定
 * @author Ray
 *
 */
public class Box {
    /** 1.長條 */
    public final static String STYLE1 = "0,0|0,1|0,2|0,3@0,0|1,0|2,0|3,0";    
    /** 2.反L */
    public final static String STYLE2 = "0,0|1,0|1,1|1,2@0,0|0,1|1,0|2,0@0,0|0,1|0,2|1,2@0,1|1,1|2,0|2,1";
    /** 3.L */
    public final static String STYLE3 = "0,2|1,0|1,1|1,2@0,0|1,0|2,0|2,1@0,0|0,1|0,2|1,0@0,0|0,1|1,1|2,1";
    /** 4.正方 */
    public final static String STYLE4 = "0,0|0,1|1,0|1,1";
    /** 5.正閃電 */
    public final static String STYLE5 = "0,1|0,2|1,0|1,1@0,0|1,0|1,1|2,1";
    /** 6.T型 */
    public final static String STYLE6 = "0,1|1,0|1,1|1,2@0,1|1,1|1,2|2,1@1,0|1,1|1,2|2,1@0,1|1,0|1,1|2,1";
    /** 7.反閃電 */
    public final static String STYLE7 = "0,0|0,1|1,1|1,2@0,1|1,0|1,1|2,0";

    private final static String [] STYLE_LIST = {STYLE1,STYLE2,STYLE3,STYLE4,STYLE5,STYLE6,STYLE7};

    public int nowX;            //目前的x位置
    public int nowY;            //目前的y位置

    protected int nowturn;            //目前的轉向
    protected int style;            //此方塊種類代碼
    protected int [][][] boxAry;    //此方塊轉向後的值
    protected int [][] hw;        //此方塊轉向後的高、寬    

    public Box() {
        BoxBaseInit();
    }

    public void BoxBaseInit(){
        nowturn = 0;
        boxAry = new int[0][0][0];
        style = 1;
        hw = new int[0][0];
    }

    public void resetXY(){
        nowX = 0;
        nowY = 0;
    }

    /**
     * 指定style建立方塊型狀
     * style:
     * 1.長條<BR>
     * 2.反L<BR>
     * 3.L <BR>
     * 4.正方<BR>
     * 5.正閃電 <BR>
     * 6.T型<BR>
     * 7.反閃電 <BR>
     *
     * @param style
     */
    public void setBoxData(int s){
        if(s < 0 || s > STYLE_LIST.length){
            System.out.println("設定的方塊型別不存在");
            return;
        }
        String data = STYLE_LIST[s - 1];
        style = s;
        setStyleData(data);
    }

    /**
     * 設定方塊類型
     * "[形狀1]_高,寬@[形狀2]_高,寬..."<BR>
     * 形狀:二維陣列位置1|二維陣列位置1|...<BR>
     * 接受格式為"0,0|0,1|0,2|0,3@0,0|1,0|2,0|3,0"<BR>
     * @param data STYLE1 or STYLE2, STYLE3...
     */
    public void setStyleData(String data){
        String [] ary = data.split("[@]");
        boxAry = new int[ary.length][][];
        hw = new int[ary.length][2];

        for(int i = 0; i < ary.length; i++){
            String [] box = ary[i].split("[|]");
            int h = 0;
            int w = 0;

            //找尋方塊各方裡x最大格數與y的最大格數來當寬與高,找完之後因設定值為從0開始，需要再將高、寬各+1
            for(int j = 0; j < box.length; j++){
                String [] bAry = box[j].split("[,]");
                int x = bAry[0].charAt(0) - '0';
                int y = bAry[1].charAt(0) - '0';
                
                if(x > h){
                    h = x;
                }
                if(y > w){
                    w = y;
                }
            }
            h++;
            w++;
            hw[i][0] = h;
            hw[i][1] = w;
            
            boxAry[i] = new int[h][w];

            for(int j = 0; j < box.length; j++){
                String [] bAry = box[j].split("[,]");
                int x = bAry[0].charAt(0) - '0';
                int y = bAry[1].charAt(0) - '0';
                boxAry[i][x][y] = style;
            }
        }
    }

    public int getStyle(){
        return style;
    }

    /**
     * 取得目前有幾種方塊種類
     * @return
     */
    public static int getStyleCount(){
        return STYLE_LIST.length;
    }

    /**
     * 設定目前轉向
     * @param n
     */
    public void setTurn(int n){
        nowturn = n;
    }

    /**
     * 取得目前轉向
     * @return
     */
    public int getTurn(){
        return nowturn;
    }

    public int nextTurn(int n){
        int tmpTurn = nowturn;

        tmpTurn += n;
        if(tmpTurn < 0){
            tmpTurn = getTrunKind() - 1;
        }

        tmpTurn %= getTrunKind();

        return tmpTurn;
    }

    /**
     * 逆時針轉向
     */
    public void turnLeft(){
        nowturn--;
        if(nowturn < 0){
            nowturn = getTrunKind() - 1;
        }
    }

    /**
     * 順時針轉向
     *
     */
    public void turnRight(){
        nowturn++;
        nowturn %= getTrunKind();
    }

    /**
     * 取得方塊有幾種轉向
     * @return
     */
    public int getTrunKind(){
        return boxAry.length;
    }

    /**
     * 取得指定轉向的寬
     * @param n
     * @return
     */
    public int getWight(int n){
        if(n >=0 && n < hw.length){
            return hw[n][1];
        }
        return 0;
    }

    /**
     * 取得目前轉向的寬
     * @return
     */
    public int getNowTurnWight(){
        return getWight(nowturn);
    }

    /**
     * 取得指定轉向的高
     * @param n
     * @return
     */
    public int getHeight(int n){
        if(n >=0 && n < hw.length){
            return hw[n][0];
        }
        return 0;
    }

    /**
     * 取得目前轉向的高
     * @return
     */
    public int getNowTurnHeight(){
        return getHeight(nowturn);
    }

    /**
     * 取得目前方塊形狀
     * @return
     */
    public int [][] getNowturnBoxAry(){
        return getBoxAry(nowturn);
    }
    
    /**
     * 取得目前方塊形狀
     * @return
     */
    public String getNowturnBoxStyleStr(){
        return getBoxStyleStr(nowturn);
    }

    /**
     * 取得指定轉向的方塊形狀
     * @param index
     * @return
     */
    public int [][] getBoxAry(int index){
        if(index >=0 && index < boxAry.length){
            return boxAry[index];
        }
        return null;
    }

    /**
     * 取得指定轉向的方塊形狀(以字串格式)，例如:"[形狀1]_高,寬@[形狀2]_高,寬
     * @param index
     * @return
     */
    public String getBoxStyleStr(int index){
        //System.out.println("目前轉向"+index);
        if(index >=0 && index < boxAry.length){
            String data = STYLE_LIST[style - 1];
            String [] ary = data.split("[@]");
            return ary[index];
        }
        return "";
    }

    /**
     * 移動x幾格，y幾格,並將舊的位置記下
     * @param x
     * @param y
     */
    public void move(int x,int y){
        nowY += y;
        nowX += x;
    }

    /**
     * 印出指定轉向的圖
     * @param index
     */
    public void printBox(int index){
        int tmp [][] = boxAry[index];

        for(int i = 0; i < tmp.length; i++){
            for(int j = 0; j < tmp[i].length; j++){
                if(tmp[i][j] > 0){
                    System.out.print("口");
                }else{
                    System.out.print("　");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printNowturnBox(){
        printBox(nowturn);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        Box box = new Box();
        box.setBoxData(3);

        for(int i = 0; i < 50; i++){

            box.turnLeft();
            printAry(box.getNowturnBoxAry());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO 自動產生 catch 區塊
                e.printStackTrace();
            }
        }

        /*for(int style = 1; style <= BoxBase.getStyleCount(); style++){
            BoxBase box = new BoxBase();
            box.setBoxData(style);
            for(int i = 0; i < box.getTrunKind(); i++){
                box.printBox(i);
            }
        }*/
    }
    


    //測試用，印出陣列
    public static void printAry(int [][] tmp){
        for(int i = 0; i < tmp.length; i++){
            for(int j = 0; j < tmp[i].length; j++){
                if(tmp[i][j] == 1){
                    System.out.print("口");
                }else{
                    System.out.print("　");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

}
