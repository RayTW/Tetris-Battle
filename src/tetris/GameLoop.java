package tetris;

import java.util.Random;

import tetris.box.Box;
import tetris.box.CheckCleanLineThread;
import tetris.box.CleanLineListener;
import tetris.box.GameBox;

/**
 * 控制遊戲流程
 * 
 * @author Ray
 * 
 */
public class GameLoop implements Runnable, CleanLineListener {
	private float mFastSec;// 快移的秒數
	private float mSec;
	private Random mRand;
	private GameBox mGameBox;
	private boolean mIsRun;
	private boolean mIsPause;
	private boolean mIsGameOver; // 是否遊戲結束
	private boolean mIsClean; // 目前是否有方塊到底
	private ViewDelegate mDelegate;
	private CheckCleanLineThread mCheckCleanThread;

	private int mFlag; // 目前使用的方塊位置
	private String[] mStyleAry;// 預戴方塊buffer區

	public GameLoop() {
		mIsRun = true;
		mRand = new Random();
		initialize();
	}

	public void initialize() {
		mFlag = 0;
		mStyleAry = new String[0];
		mSec = 0.2f;
		mFastSec = 0.1f;
		mGameBox = new GameBox();
		mIsPause = false;
		mIsGameOver = false;
		mCheckCleanThread = new CheckCleanLineThread();
		mCheckCleanThread.setCleanLineListener(this);
		mCheckCleanThread.startThread();
		setBoxList(getRandBox(5));// 設定使用5組亂數排列方塊進行遊戲
		nextCreatBox();
	}

	public void startGame() {
		new Thread(this).start();
	}

	public void stopGame() {
		mIsRun = false;
	}

	@Override
	public void run() {
		while (mIsRun) {
			try {
				if (!mIsPause && !mIsGameOver) {// 沒有按暫停才可玩
					if (!mGameBox.moveDown()) {// 方塊已到底停住,不能再往下移
						mIsClean = true;
					}
					putDelegateCode(GameEvent.REPAINT, "");
				}
				Thread.sleep((int) (1000 * mSec));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		close();
	}

	/**
	 * 設定預載的方塊style
	 * 
	 * @param boxList
	 */
	public void setBoxList(String boxList) {
		if (!boxList.equals("")) {
			mStyleAry = boxList.split("[|]");
		}
	}

	/**
	 * 取得n組亂數排列方塊,例如取1組為:"1|5|4|3|2|6|7"
	 * 
	 * @param n
	 * @return
	 */
	public String getRandBox(int n) {
		int[] boxAry = new int[Box.getStyleCount()];
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
	 * 亂數打亂方塊排列
	 * 
	 * @param ary
	 * @return
	 */
	private int[] randBoxAry(int[] ary) {
		for (int i = 0; i < ary.length; i++) {
			int tmpIndex = mRand.nextInt(ary.length);
			int style = ary[tmpIndex];
			ary[tmpIndex] = ary[i];
			ary[i] = style;
		}
		return ary;
	}

	/**
	 * 取出預載方塊buffer下一個方塊
	 * 
	 * @return
	 */
	public int nextBox() {
		int style = 0;
		if (mFlag > mStyleAry.length - 1) {
			mFlag = 0;
		}
		style = Integer.parseInt(mStyleAry[mFlag]);
		mFlag++;
		return style;
	}

	/**
	 * 取出預載方塊buffer n個方塊
	 * 
	 * @param n
	 * @return
	 */
	public String[] getAnyCountBox(int n) {
		String[] nBox = new String[n];
		int tmpFlag = mFlag;

		for (int i = 0; i < n; i++) {
			if (tmpFlag > mStyleAry.length - 1) {
				tmpFlag = 0;
			}
			nBox[i] = mStyleAry[tmpFlag];
			tmpFlag++;
		}
		return nBox;
	}

	/**
	 * 控制方塊下移1格
	 */
	public boolean moveDown() {
		return mGameBox.moveDown();
	}

	/**
	 * 控制方塊左移1格
	 */
	public boolean moveLeft() {
		if (mIsClean)
			return false;

		return mGameBox.moveLeft();
	}

	/**
	 * 控制方塊右移1格
	 */
	public boolean moveRight() {
		if (mIsClean)
			return false;

		return mGameBox.moveRight();
	}

	/**
	 * 控制方塊順轉1次
	 */
	public boolean turnLeft() {
		if (mIsClean)
			return false;

		return mGameBox.turnLeft();
	}

	/**
	 * 控制方塊逆轉1次
	 */
	public boolean turnRight() {
		if (mIsClean)
			return false;

		return mGameBox.turnRight();
	}

	/**
	 * 方塊直接掉落到定位
	 */
	public void quickDown() {
		if (mIsClean)
			return;
		mGameBox.quickDown();
		mIsClean = true;
	}

	/**
	 * 遊戲暫停
	 */
	public void pause() {
		mIsPause = true;
	}

	/**
	 * 目前是否暫停中
	 * 
	 * @return
	 */
	public boolean isPause() {
		return mIsPause;
	}

	/**
	 * 繼續遊戲(有按暫停之後使用)
	 */
	public void rusme() {
		mIsPause = false;
	}

	/**
	 * 設定GameOver狀態,true:遊戲結束,false:遊戲未結束
	 * 
	 * @param b
	 * @return
	 */
	public void setGameOver(boolean b) {
		mIsGameOver = b;
	}

	/**
	 * 取得目前是否遊戲結束
	 * 
	 * @return
	 */
	public boolean isGameOver() {
		return mIsGameOver;
	}

	/**
	 * 設定代理者
	 * 
	 * @param o
	 */
	public void setDelegate(ViewDelegate o) {
		mDelegate = o;
	}

	/**
	 * 設定目前方塊掉落等待秒數
	 * 
	 * @param s
	 */
	public void setSec(float s) {
		mSec = s;
	}

	/**
	 * 取得目前方塊掉落等待秒數
	 * 
	 * @return
	 */
	public float getSec() {
		return mSec;
	}

	/**
	 * 發送資料給代理者
	 * 
	 * @param code
	 * @param data
	 */
	public void putDelegateCode(GameEvent code, String data) {
		if (mDelegate != null) {
			mDelegate.tetrisEvent(code, data);
		}
	}

	/**
	 * 取得目前二維陣列裡，疊的方塊到第幾個位置，0~20個單位<BR>
	 * 必須在方塊落到底時呼叫(即GameLoop的cleanLine()被執行時)，才可得到正確的高度資料
	 * 
	 * @return
	 */
	public int getNowBoxIndex() {
		return mGameBox.getNowBoxIndex();
	}

	/**
	 * 取得目前的整個遊戲畫面可移動方塊區域的二維陣列
	 */
	public int[][] getBoxAry() {
		return mGameBox.getBoxAry();
	}

	/**
	 * 亂數產生方塊
	 */
	public boolean randCreatBox() {
		int style = mRand.nextInt(Box.getStyleCount()) + 1;
		return mGameBox.createBaseObj(style);
	}

	/**
	 * 從buffer取方塊建立
	 * 
	 * @return
	 */
	public boolean nextCreatBox() {
		int style = nextBox();
		return mGameBox.createBaseObj(style);
	}

	public int[][] createBox(int style) {
		return mGameBox.createBox(style);
	}

	@Override
	public void cleanLine() {
		mGameBox.addBox();
		putDelegateCode(GameEvent.REPAINT, "");
		putDelegateCode(GameEvent.BOX_DOWN, "");

		// System.out.println("cleanLine----被執行-------");

		// 取得可消除的行數
		String lineData = mGameBox.getClearLine();

		if (!lineData.equals("")) {
			putDelegateCode(GameEvent.CLEANING_LINE, lineData);
			mGameBox.clearLine(lineData);// 實際將可消除的方塊行數移除
			putDelegateCode(GameEvent.CLEANED_LINE, "");
		}

		putDelegateCode(GameEvent.BOX_GARBAGE, lineData);

		boolean isOK = nextCreatBox();// 建立方塊
		if (!isOK) {// 建立失敗
			mIsGameOver = true;
			putDelegateCode(GameEvent.REPAINT, "");
			// printAry(gameBox.getBoxAry());
			putDelegateCode(GameEvent.GAME_OVER, "");
		}
		putDelegateCode(GameEvent.REPAINT, "");
		putDelegateCode(GameEvent.BOX_NEXT, "");
		mIsClean = false;
		mCheckCleanThread = new CheckCleanLineThread();
		mCheckCleanThread.setCleanLineListener(this);
		mCheckCleanThread.startThread();
	}

	/**
	 * 目前掉落方塊已定格中，進行檢查可消方塊
	 */
	@Override
	public boolean isClean() {
		return mIsClean;
	}

	/**
	 * 清空整個畫面所有方塊
	 */
	public void clearBox() {
		mGameBox.clearBoxAry();
	}

	/**
	 * 取得掉落中方塊
	 * 
	 * @return
	 */
	public int[][] getNowBoxAry() {
		return mGameBox.getNowBoxAry();
	}

	/**
	 * 取得掉落中方塊目前的x、y位置
	 * 
	 * @return
	 */
	public int[] getNowBoxXY() {
		return mGameBox.getNowBoxXY();
	}

	/**
	 * 取得到第Y個位置會撞到方塊
	 * 
	 * @return
	 */
	public int getDownY() {
		return mGameBox.getDownY();
	}

	public void close() {
		mCheckCleanThread.stopThread();
		mDelegate = null;
		mRand = null;
		mCheckCleanThread = null;
		mGameBox = null;
		mStyleAry = null;
	}

	/**
	 * isGap為false時
	 * 取得指定index行的方塊串，格式為"1|2|3|4|5||6|7|1|2|3@1|2|3|4|5||6|7|1|2|3@..."<BR>
	 * isGap為true時
	 * 取得指定index行的方塊串，格式為"1|2|3|4|5||6|7|1|0|0@1|2|3|4|5||6|7|1|2|0@..."
	 * 
	 * @param lineData
	 *            接收格式為:17,19,5...
	 * @param isGap
	 *            true 取出的已被消除行數資料是未加上掉落方塊，false取出可被消行數是加上掉落方塊
	 * @return
	 */
	public String getLineList(String lineData, boolean isGap) {
		return mGameBox.getLineList(lineData, isGap);
	}

	// 印出陣列
	/*
	 * public void printAry(int [][] tmp){ //String digit = "０１２３４５６７８９";
	 * System.out.println("=================="); for(int i = 0; i < tmp.length;
	 * i++){ System.out.print("|"); for(int j = 0; j < tmp[i].length; j++){
	 * if(tmp[i][j] > 0){ System.out.print("口");
	 * //System.out.print(digit.charAt(tmp[i][j])); }else{
	 * System.out.print("　"); } } System.out.println("|"); }
	 * System.out.println("=================="); }
	 */

}
