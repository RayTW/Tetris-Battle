package tetris;

/**
 * 遊戲設定
 * 
 * @author Ray Lee Created on 2017/10/18
 */
public class Config {
	private static Config sInstance = new Config();
	
	//遊戲版本
	private String mVersion = "1.3";

	// 消除方塊行數可獲得的分數
	private int [] mCleanLinesScore = { 0, 100, 300, 500, 800 };
	
	//方塊掉落速度(秒)
	private float [] mBoxFallSpeed = { 2.0f, 1.5f, 1.0f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.08f };

	//每個等級升下一級所需的行數
	private int mLevelUpLines = 10;
	
	// 畫面比例
	private double mScreenScale = 1;

	// 重新開始等待秒數
	private int mNextRoundDelaySecond = 5;

	// 重用音效緩存的筆數
	private int mSoundCacheCount = 20;
	
	//下移1格時可獲得的分數
	private int mMoveDownScore = 5;
	
	//快速下移時可獲得的分數
	private int mQuickDownScore = 10;

	private Config() {
	}

	public static Config get() {
		return sInstance;
	}
	
	/**
	 * 取得遊戲版本
	 * @return
	 */
	public String getVersion(){
		return mVersion;
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
	
	/**
	 * 用等級取得方塊掉落速度
	 * @param level
	 * @return
	 */
	public float getBoxFallSpeed(int level){
		if(level >= 0 && level < mBoxFallSpeed.length){
			return mBoxFallSpeed[level];
		}
		return mBoxFallSpeed[mBoxFallSpeed.length - 1];
	}
	
	/**
	 * 已消除的方塊行數轉換為對應的等級
	 * 
	 * @param lines
	 * @return
	 */
	public int linesConvertLevel(int lines){
		return (lines / mLevelUpLines);
	}
	
	/**
	 * 取得下移1格時可獲得的分數
	 * @return
	 */
	public int getMoveDownScore(){
		return mMoveDownScore;
	}
	
	/**
	 * 取得快速下速方塊可獲得的分數
	 * @return
	 */
	public int getQuickDownScore(){
		return mQuickDownScore;
	}
}
