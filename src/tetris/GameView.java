package tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;

import util.AudioPlayer;

/**
 * 此類別只做畫面處理，不做方塊移動運算，所有GameLoop類別所觸發的事件會通知此類別的tetrisEvent() method
 * 
 * @author 吉他手 Ray
 * 
 */
public class GameView extends JComponent implements ViewDelegate {
	private int mNextBoxCount = 3; // 下次要出現的方塊可顯示個數
	private int[][][] mBoxBuffer; // 下次要出現的方塊style
	private int mScreenScale = 1; // 畫面比例
	private int mBoxStartX = 62 * mScreenScale; // 掉落方塊的初始位置x
	private int mBoxStartY = 79 * mScreenScale; // 掉落方塊的初始位置y
	private int mGameScreenWidth = 350 * mScreenScale; // 遊戲畫面寬
	private int mGameScreenHeight = 480 * mScreenScale; // 遊戲畫面高
	private int mSingleBoxWidth = 19 * mScreenScale; // 每個方塊格寬
	private int mSingleBoxHeight = 19 * mScreenScale; // 每個方塊格高
	private int mShdowBoxAlpha = 125; // 陰影透明度0~250
	private long mScore;
	private Image mCanvasBuffer = null;
	private Color[] mColor = { null, new Color(0, 255, 255, 250),
			new Color(0, 0, 255, 250), new Color(0, 255, 0, 250),
			new Color(255, 0, 0, 250), new Color(255, 255, 0, 250),
			new Color(255, 0, 255, 250), new Color(50, 100, 150, 250) };
	private Color[] mColorAlpha = { null, new Color(0, 255, 255, mShdowBoxAlpha),
			new Color(0, 0, 255, mShdowBoxAlpha), new Color(0, 255, 0, mShdowBoxAlpha),
			new Color(255, 0, 0, mShdowBoxAlpha), new Color(255, 255, 0, mShdowBoxAlpha),
			new Color(255, 0, 255, mShdowBoxAlpha), new Color(50, 100, 150, mShdowBoxAlpha) };

	private GameLoop mGameLoop; // 遊戲邏輯(無畫面)
	private AudioPlayer mBackgroundMusic;//播放背景音樂

	public GameView() {
		mBackgroundMusic = playAudio("sound/music.wav", 0);
	}

	public void initialize() {
		// 建立遊戲邏輯
		mGameLoop = new GameLoop();

		// 設定使用GameView代理遊戲邏輯進行畫面的繪圖
		mGameLoop.setDelegate(this);

		// 設定方塊掉落秒數為0.5秒
		mGameLoop.setSec(0.5f);

		// 設定下次要出現的方塊style個數為顯示3個
		mBoxBuffer = getBufBox(mGameLoop, mNextBoxCount);

		// 啟動遊戲邏輯執行緒
		mGameLoop.startGame();

		// 設定畫面大小
		setSize(mGameScreenWidth, mGameScreenHeight);
	}

	public AudioPlayer playAudio(String path, int playCount) {
		AudioPlayer audio = new AudioPlayer();
		path = "/" + path;
		audio.loadAudio(path, this);

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
		showDownBox(xy, box, canvas);

		// 畫右邊下次要出現的方塊
		showBufferBox(mBoxBuffer, canvas);

		// 畫陰影
		shadow(xy, box, canvas, mGameLoop.getDownY());

		// 顯示分數
		showScore(mScore, canvas);

		// 將暫存的圖，畫到前景
		g.drawImage(mCanvasBuffer, 0, 0, this);
	}

	// 畫定住的方塊與其他背景格子
	private void showBacegroundBox(int[][] boxAry, Graphics buffImg) {
		for (int i = 0; i < boxAry.length; i++) {
			for (int j = 0; j < boxAry[i].length; j++) {
				int style = boxAry[i][j];
				if (style > 0) {// 畫定住的方塊
					// buffImg.fillOval(BOX_START_X + (BOX_IMG_W *
					// j),BOX_START_Y + (BOX_IMG_H * i),BOX_IMG_W,BOX_IMG_H);
					drawBox(style, j, i, buffImg);
				} else {// 畫其他背景格子
					buffImg.drawRect(mBoxStartX + (mSingleBoxWidth * j), mBoxStartY
							+ (mSingleBoxHeight * i), mSingleBoxWidth, mSingleBoxHeight);
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
						// buffImg.drawOval(160 + (BOX_IMG_W * (j + 5)),0 +
						// (BOX_IMG_H * (i + 5)), BOX_IMG_W, BOX_IMG_H);
						buffImg.setColor(mColor[style]);
						buffImg.fill3DRect(160 + (mSingleBoxWidth * (j + 5)),
								(n * 50) + (mSingleBoxHeight * (i + 5)), mSingleBoxWidth,
								mSingleBoxHeight, true);
						// buffImg.setColor(Color.BLACK);
						// buffImg.drawRect(160 + (BOX_IMG_W * (j + 5)),(n * 50)
						// + (BOX_IMG_H * (i + 5)), BOX_IMG_W, BOX_IMG_H);
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
					buffImg.setColor(mColorAlpha[style]);
					buffImg.fill3DRect(mBoxStartX + (mSingleBoxWidth * (j + boxX)),
							mBoxStartY + (mSingleBoxHeight * (i + index)), mSingleBoxWidth,
							mSingleBoxHeight, true);
				}
			}
		}
	}

	private void showScore(long sc, Graphics buffImg) {
		buffImg.setColor(Color.RED);
		buffImg.drawString("SCORE:" + sc, 50, 50);
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
		// buffImg.fill3DRect(arg0, arg1, arg2, arg3, arg4)
		buffImg.setColor(mColor[style]);
		buffImg.fill3DRect(mBoxStartX + (mSingleBoxWidth * x), mBoxStartY
				+ (mSingleBoxHeight * y), mSingleBoxWidth, mSingleBoxHeight, true);
		buffImg.setColor(Color.BLACK);
		// buffImg.drawRect(BOX_START_X + (BOX_IMG_W * x),BOX_START_Y +
		// (BOX_IMG_H * y),BOX_IMG_W,BOX_IMG_H);
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
			playAudio("sound/turn.wav", 1);

			return;
		}
		// 方塊落到底
		if (GameEvent.BOX_DOWN == code) {
			System.out.println("做方塊到底定位動畫 現在方塊高度["
					+ mGameLoop.getNowBoxIndex() + "]");

			// 做方塊到底定位動畫
			playAudio("sound/down.wav", 1);

			return;
		}
		// 建立完下一個方塊
		if (GameEvent.BOX_NEXT == code) {
			mBoxBuffer = getBufBox(mGameLoop, mNextBoxCount);
			return;
		}
		// 有方塊可清除,將要清除方塊,可取得要消去的方塊資料
		if (GameEvent.CLEANING_LINE == code) {
			System.out.println("有方塊可清除,將要清除方塊,可取得要消去的方塊資料");
			mScore += (mGameLoop.getNowBoxIndex() + 1) * 100;// 加分數
			// System.out.println("垃圾方塊模式==>,index位置["+data+"],資料為["+tetrisGame.getLineList(data,false)+"]");
			// System.out.println("凹陷方塊模式==>index位置["+data+"],資料為["+tetrisGame.getLineList(data,true)+"]");
			return;
		}
		// 方塊清除完成
		if (GameEvent.CLEANED_LINE == code) {
			System.out.println("方塊清除完成" + data);

			return;
		}
		// 計算自己垃圾方塊數
		if (GameEvent.BOX_GARBAGE == code) {

			return;
		}
		// 方塊頂到最高處，遊戲結束
		if (GameEvent.GAME_OVER == code) {
			System.out.println("秒後重新...");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			mScore = 0;
			// 清除全畫面方塊
			mGameLoop.clearBox();

			// 當方塊到頂時，會自動將GameOver設為true,因此下次要開始時需設定遊戲為false表示可進行遊戲
			mGameLoop.setGameOver(false);
		}
		return;
	}

	public void objEvent(String code, Object obj) {

	}
}
