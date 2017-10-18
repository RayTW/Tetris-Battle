package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;

import tetris.Config;
import tetris.GameEvent;
import tetris.GameLoop;
import util.AudioPlayer;
import util.Debug;

/**
 * 此類別只做畫面處理，不做方塊移動運算，所有GameLoop類別所觸發的事件會通知此類別的tetrisEvent() method
 * 
 * @author Ray
 * 
 */
public class GameView extends JComponent implements ViewDelegate {
	private int mNextBoxCount = 3; // 下次要出現的方塊可顯示個數
	private int[][][] mBoxBuffer; // 下次要出現的方塊style
	private int mBoxStartX; // 掉落方塊的初始位置x
	private int mBoxStartY; // 掉落方塊的初始位置y
	private int mGameScreenWidth; // 遊戲畫面寬
	private int mGameScreenHeight; // 遊戲畫面高
	private int mSingleBoxWidth; // 每個方塊格寬
	private int mSingleBoxHeight; // 每個方塊格高
	private int mRightNextBoxesX; // 右側方塊的位置x
	private int mRightNextBoxesHeightSpacing; // 右側方塊的位置y間距
	private int mScoreLocationY; // 分數顯示位置
	private int mLevelLocationY; // 等級顯示位置
	private int mGameOverLocationX; // 遊戲結束顯示位置x
	private int mGameOverLocationY; // 遊戲結束顯示位置y
	private int mNextRoundCountdownSecondLocationX; // 下局倒數秒數顯示位置x
	private int mNextRoundCountdownSecondLocationY; // 下局倒數秒數顯示位置y
	private int mLinesLocationY; // 方塊消除累計行數顯示位置
	private Font mScoreFont;
	private Image mCanvasBuffer = null;
	private Color[] mColor = { null, new Color(0, 255, 255, 250), new Color(0, 0, 255, 250), new Color(0, 255, 0, 250),
			new Color(255, 0, 0, 250), new Color(255, 255, 0, 250), new Color(255, 0, 255, 250),
			new Color(50, 100, 150, 250) };
	private Color mShadowColor = new Color(0, 0, 0, 128);

	private GameLoop mGameLoop; // 遊戲邏輯(無畫面)
	private AudioPlayer mBackgroundMusic;// 播放背景音樂
	private InfoBar mInfoBar;

	public GameView() {
		mBackgroundMusic = playMusic("sound/music.wav");
	}

	public void initialize() {
		mScoreFont = null;
		Config config = Config.get();

		mBoxStartX = config.convertValueViaScreenScale(62);
		mBoxStartY = config.convertValueViaScreenScale(79);
		mGameScreenWidth = config.convertValueViaScreenScale(350);
		mGameScreenHeight = config.convertValueViaScreenScale(480);
		mSingleBoxWidth = config.convertValueViaScreenScale(19);
		mSingleBoxHeight = config.convertValueViaScreenScale(19);
		mRightNextBoxesX = config.convertValueViaScreenScale(160);
		mRightNextBoxesHeightSpacing = config.convertValueViaScreenScale(50);

		// 分數位置
		mLevelLocationY = Config.get().convertValueViaScreenScale(20);
		mLinesLocationY = Config.get().convertValueViaScreenScale(45);
		mScoreLocationY = Config.get().convertValueViaScreenScale(70);
		
		//遊戲結束
		mGameOverLocationX = Config.get().convertValueViaScreenScale(100);
		mGameOverLocationY = Config.get().convertValueViaScreenScale(250);
		mNextRoundCountdownSecondLocationX = Config.get().convertValueViaScreenScale(155);
		mNextRoundCountdownSecondLocationY = Config.get().convertValueViaScreenScale(270);

		// 分數、消除行數、等級
		mInfoBar = new InfoBar();
		// 建立遊戲邏輯
		mGameLoop = new GameLoop();

		// 設定使用GameView代理遊戲邏輯進行畫面的繪圖
		mGameLoop.setDelegate(this);

		// 設定方塊掉落秒數為
		mGameLoop.setSec(Config.get().getBoxFallSpeed(mInfoBar.getLevel()));

		// 設定下次要出現的方塊style個數為顯示3個
		mBoxBuffer = getBufBox(mGameLoop, mNextBoxCount);

		// 啟動遊戲邏輯執行緒
		mGameLoop.startGame();

		// 設定畫面大小
		setSize(mGameScreenWidth, mGameScreenHeight);
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

		audio.setPlayCount(playCount);// 播放次數
		audio.play();
		return audio;
	}

	// 接收鍵盤事件
	public void keyCode(int code) {
		if (mGameLoop.isGameOver()) {
			return;
		}
		if (!mGameLoop.isPause()) {
			switch (code) {
			case KeyEvent.VK_UP:// 上,順轉方塊
				mGameLoop.turnRight();
				tetrisEvent(GameEvent.BOX_TURN, null);
				break;
			case KeyEvent.VK_DOWN:// 下,下移方塊
				mGameLoop.moveDown();
				addMoveDownScore();
				break;
			case KeyEvent.VK_LEFT:// 左,左移方塊
				mGameLoop.moveLeft();
				break;
			case KeyEvent.VK_RIGHT:// 右,右移方塊
				mGameLoop.moveRight();
				break;
			case KeyEvent.VK_SPACE:// 空白鍵,快速掉落方塊
				mGameLoop.quickDown();
				break;
			case KeyEvent.VK_S:// S鍵,暫停
				mGameLoop.pause();
				break;
			default:
			}
		} else {
			if (code == KeyEvent.VK_R) {// R鍵,回到遊戲繼續
				mGameLoop.rusme();
			}
		}

		// 每次按了鍵盤就將畫面重繪
		repaint();
	}

	// 雙緩衝區繪圖
	@Override
	public void paintComponent(Graphics g) {
		Graphics canvas = null;

		if (mCanvasBuffer == null) {
			mCanvasBuffer = createImage(mGameScreenWidth, mGameScreenHeight);// 新建一張image的圖
		} else {
			mCanvasBuffer.getGraphics().clearRect(0, 0, mGameScreenWidth, mGameScreenHeight);
		}
		canvas = mCanvasBuffer.getGraphics();

		// 把整個陣列要畫的圖，畫到暫存的畫布上去(即後景)
		int[][] boxAry = mGameLoop.getBoxAry();
		showBacegroundBox(boxAry, canvas);

		// 畫掉落中的方塊
		int[] xy = mGameLoop.getNowBoxXY();
		int[][] box = mGameLoop.getNowBoxAry();

		// 畫陰影
		shadow(xy, box, canvas, mGameLoop.getDownY());

		showDownBox(xy, box, canvas);

		// 畫右邊下次要出現的方塊
		showBufferBox(mBoxBuffer, canvas);

		// 顯示分數
		showInfoBar(mInfoBar, canvas);
		
		// 顯示遊戲結束，並倒數秒數
		showGameOver(mInfoBar, canvas);

		// 將暫存的圖，畫到前景
		g.drawImage(mCanvasBuffer, 0, 0, this);
	}

	// 畫定住的方塊與其他背景格子
	private void showBacegroundBox(int[][] boxAry, Graphics buffImg) {
		for (int i = 0; i < boxAry.length; i++) {
			for (int j = 0; j < boxAry[i].length; j++) {
				int style = boxAry[i][j];
				if (style > 0) {// 畫定住的方塊
					drawBox(style, j, i, buffImg);
				} else {// 畫其他背景格子
					buffImg.drawRect(mBoxStartX + (mSingleBoxWidth * j), mBoxStartY + (mSingleBoxHeight * i),
							mSingleBoxWidth, mSingleBoxHeight);
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
						buffImg.setColor(mColor[style]);
						buffImg.fill3DRect(mRightNextBoxesX + (mSingleBoxWidth * (j + 5)),
								(n * mRightNextBoxesHeightSpacing) + (mSingleBoxHeight * (i + 5)), mSingleBoxWidth,
								mSingleBoxHeight, true);
					}
				}
			}
		}
	}

	// 畫陰影
	private void shadow(int[] xy, int[][] box, Graphics buffImg, int index) {
		int boxX = xy[0];
		// int boxY = xy[1];

		for (int i = 0; i < box.length; i++) {
			for (int j = 0; j < box[i].length; j++) {
				int style = box[i][j];
				if (style > 0) {
					buffImg.setColor(mShadowColor);
					buffImg.fill3DRect(mBoxStartX + (mSingleBoxWidth * (j + boxX)),
							mBoxStartY + (mSingleBoxHeight * (i + index)), mSingleBoxWidth, mSingleBoxHeight, true);
				}
			}
		}
	}

	private void showInfoBar(InfoBar info, Graphics buffImg) {
		if (mScoreFont == null) {
			Font currentFont = buffImg.getFont();
			Font newFont = currentFont.deriveFont(Font.BOLD, Config.get().convertValueViaScreenScale(20));
			mScoreFont = newFont;
		}
		// 調整分數字型
		buffImg.setFont(mScoreFont);

		buffImg.setColor(Color.RED);
		buffImg.drawString("LEVEL:" + info.getLevel(), 2, mLevelLocationY);
		buffImg.setColor(Color.BLACK);
		buffImg.drawString("SCORE:" + info.getScore(), 2, mLinesLocationY);
		buffImg.setColor(Color.BLUE);
		buffImg.drawString("LINES:" + info.getCleanedCount(), 2, mScoreLocationY);
	}
	
	private void showGameOver(InfoBar info, Graphics buffImg){
		if(mGameLoop.isGameOver()){
			buffImg.setColor(Color.DARK_GRAY);
			buffImg.drawString("GAME OVER", mGameOverLocationX, mGameOverLocationY);
			
			buffImg.drawString(String.valueOf(info.getWaitNextRoundSecond() + 1), mNextRoundCountdownSecondLocationX, mNextRoundCountdownSecondLocationY);
			
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
		buffImg.setColor(mColor[style]);
		buffImg.fill3DRect(mBoxStartX + (mSingleBoxWidth * x), mBoxStartY + (mSingleBoxHeight * y), mSingleBoxWidth,
				mSingleBoxHeight, true);
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
	 * 所有
	 */
	@Override
	public void tetrisEvent(GameEvent code, String data) {
		// 收到重畫自己畫面的陣列
		if (GameEvent.REPAINT == code) {
			repaint();
			return;
		}
		if (GameEvent.BOX_TURN == code) {
			playSound("sound/turn.wav");

			return;
		}
		// 方塊落到底
		if (GameEvent.BOX_DOWN == code) {
			Debug.get().println("做方塊到底定位動畫 現在方塊高度[" + mGameLoop.getNowBoxIndex() + "]");

			// 做方塊到底定位動畫
			playSound("sound/down.wav");

			return;
		}
		// 建立完下一個方塊
		if (GameEvent.BOX_NEXT == code) {
			mBoxBuffer = getBufBox(mGameLoop, mNextBoxCount);
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
			mInfoBar.addCleanedCount(lines.length);
			mInfoBar.addScore(Config.get().getCleanLinesScore(lines.length));
			
			if(tryLevelUp()){
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
			mInfoBar.setWaitNextRoundSecond(Config.get().getNextRoundDelaySecond());
			
			while(mInfoBar.getWaitNextRoundSecond() > 0){
				repaint();
				Debug.get().println(mInfoBar.getWaitNextRoundSecond() + "秒後開始新局...");
				mInfoBar.addWaitNextRoundSecond(-1);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}	
			}
			// 重置分數
			mInfoBar.initialize();
			// 清除全畫面方塊
			mGameLoop.clearBox();
			
			// 設定方塊掉落秒數
			mGameLoop.setSec(Config.get().getBoxFallSpeed(mInfoBar.getLevel()));

			// 當方塊到頂時，會自動將GameOver設為true,因此下次要開始時需設定遊戲為false表示可進行遊戲
			mGameLoop.setGameOver(false);
		}
		return;
	}
	
	/**
	 * 試著計算是否提升等級1級，並重設方塊掉落速度
	 */
	private boolean tryLevelUp(){
		int currentLevel = mInfoBar.getLevel();
		int newLevel = Config.get().linesConvertLevel(mInfoBar.getCleanedCount());
		
		if(currentLevel != newLevel){
			mInfoBar.setLevel(newLevel);
			mGameLoop.setSec(Config.get().getBoxFallSpeed(mInfoBar.getLevel()));
			return true;
		}
		return false;
	}

	public void objEvent(String code, Object obj) {

	}

	private void addMoveDownScore() {
		if (mGameLoop.getDownY() > 0) {
			Debug.get().println("加分 :" + mGameLoop.getDownY());
		}
	}
}
