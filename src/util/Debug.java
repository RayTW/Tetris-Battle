package util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 除錯工具
 * @author Ray Lee 
 * Created on 2017/10/18
 */
public class Debug {
	private SimpleDateFormat mSimpleDateFormat;
	private static Debug sInstance = new Debug();

	private Debug() {
		mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	}

	
	public static Debug get(){
		return sInstance;
	}
	
	public void print(String str){
		System.out.print(mSimpleDateFormat.format(new Date()) + " " + str);
	}
	
	public void println(String str){
		System.out.println(mSimpleDateFormat.format(new Date()) + " " + str);
	}
}
