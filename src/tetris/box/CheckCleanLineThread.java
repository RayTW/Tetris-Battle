package tetris.box;

public class CheckCleanLineThread extends Thread {
	private float mSec = 0.1f;
	private boolean mIsRun;
	private CleanLineListener mListener;

	public CheckCleanLineThread() {
		mIsRun = true;
		System.out.println("CheckCleanLineThread,new,hash["+hashCode()+"]");
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
//			System.out.println("CheckCleanLineThread,run,hash["+hashCode()+"]");
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
		// System.out.println("CheckCleanLineThread close;");
		System.out.println("CheckCleanLineThread,close,hash["+hashCode()+"]");
		mListener = null;
	}

}
