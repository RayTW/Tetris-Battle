package tetris;

/**
 * 遊戲設定
 * 
 * @author Ray Lee Created on 2017/10/18
 */
public class Config {
	private static Config sInstance = new Config();

	// 消除方塊行數可獲得的分數
	private int[] mCleanLinesScore = { 0, 100, 300, 500, 800 };

	// 畫面比例
	private double mScreenScale = 1;

	// 重新開始等待秒數
	private int mNextRoundDelaySecond = 3;

	// 重用音效緩存的筆數
	private int mSoundCacheCount = 20;

	private Config() {
	}

	public static Config get() {
		return sInstance;
	}

	/**
	 * 單列 100 雙列 300 三列 500 四列 800
	 * 
	 * @param cleanLines
	 *            消除行數
	 * @return
	 */
	public int getCleanLinesScore(int cleanLines) {
		if (cleanLines > 0 && cleanLines < mCleanLinesScore.length) {
			return mCleanLinesScore[cleanLines];
		}
		return 0;
	}

	/**
	 * 將傳入的數值加乘畫面縮放比率後回傳
	 * 
	 * @param value
	 * @return
	 */
	public int convertValueViaScreenScale(int value) {
		return (int) (value * mScreenScale);
	}

	/**
	 * 重新開始等待秒數
	 * 
	 * @return
	 */
	public int getNextRoundDelaySecond() {
		return mNextRoundDelaySecond;
	}

	/**
	 * 重用音效緩存的筆數
	 * 
	 * @return
	 */
	public int getSoundCacheCount() {
		return mSoundCacheCount;
	}
}
