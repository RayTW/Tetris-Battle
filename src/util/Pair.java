package util;

/**
 * 
 * @author Ray Lee Created on 2017/10/18
 * 
 * @param <F>
 * @param <S>
 */
public class Pair<F, S> {
	private F mFirst;
	private S mSecond;

	public Pair(F first, S second) {
		this.mFirst = first;
		this.mSecond = second;
	}

	public void setFirst(F first) {
		this.mFirst = first;
	}

	public void setSecond(S second) {
		this.mSecond = second;
	}

	public F getFirst() {
		return mFirst;
	}

	public S getSecond() {
		return mSecond;
	}
}
