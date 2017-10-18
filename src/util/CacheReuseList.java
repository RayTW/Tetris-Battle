package util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 
 * @author Ray Lee Created on 2017/10/18
 * 
 * @param <T>
 */
public class CacheReuseList<T> {
	private List<T> mObjects;
	private int mIndex;

	public CacheReuseList(int size, Callable<T> generatorObj) throws Exception {
		mObjects = new ArrayList<T>(size);

		for (int i = 0; i < size; i++) {
			mObjects.add(generatorObj.call());
		}
	}

	public T next() {
		int index = 0;
		synchronized (mObjects) {
			index = mIndex;
			mIndex++;
			if (mIndex >= mObjects.size()) {
				mIndex = 0;
			}
		}
		return mObjects.get(index);
	}

	public int size() {
		return mObjects.size();
	}

}
