package tetris.box;

public class CheckCleanLineThread extends Thread {
	private float mSec = 0.1f;
	private boolean mIsRun;
	private CleanLineListener mListener;

	public CheckCleanLineThread() {
		mIsRun = true;
	}

	public void setCleanLineListener(CleanLineListener o) {
		mListener = o;
	}

	public void startThread() {
		start();
	}

	public void stopThread() {
		mIsRun = false;
	}

	@Override
	public void run() {
		while (mIsRun) {
			if (mListener != null) {
				if (mListener.isClean()) {
					mListener.cleanLine();
					break;
				}
			}
			try {
				Thread.sleep((int) (mSec * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		close();
	}

	public void close() {
		mListener = null;
	}

}
