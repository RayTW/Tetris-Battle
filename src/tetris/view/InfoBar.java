package tetris.view;

/**
 * 分數、消除行數、遊戲等級等等資訊
 * 
 * @author Ray Lee Created on 2017/10/18
 */
public class InfoBar {
	private long mScore;// 遊戲分數
	private int mLevel;// 遊戲等級
	private int mCleanedCount;// 已消除的行數
	private int mWaitNextRoundSecond;//遊戲結束後倒數秒數

	public InfoBar() {
		initialize();
	}

	public void initialize() {
		mScore = 0;
		mLevel = 0;
		mCleanedCount = 0;
		mWaitNextRoundSecond = 0;
	}

	public long getScore() {
		return mScore;
	}

	public void setScore(long score) {
		mScore = score;
	}

	public void addScore(long score) {
		mScore += score;
	}

	public int getLevel() {
		return mLevel;
	}

	public void setLevel(int level) {
		mLevel = level;
	}

	public void addLevel(int level) {
		mLevel += level;
	}

	public int getCleanedCount() {
		return mCleanedCount;
	}

	public void setCleanedCount(int cleanedCount) {
		mCleanedCount = cleanedCount;
	}

	public void addCleanedCount(int cleanedCount) {
		mCleanedCount += cleanedCount;
	}
	
	public void setWaitNextRoundSecond(int second){
		mWaitNextRoundSecond = second;
	}
	
	public int getWaitNextRoundSecond(){
		return mWaitNextRoundSecond;
	}
	
	public void addWaitNextRoundSecond(int second){
		mWaitNextRoundSecond += second;
	}
}
