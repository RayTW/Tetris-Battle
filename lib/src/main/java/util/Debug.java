package util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 除錯工具.
 *
 * @author Ray Lee Created on 2017/10/18
 */
public class Debug {
  private SimpleDateFormat simpleDateFormat;
  private static Debug instance = new Debug();
  private boolean console;

  private Debug() {
    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  }

  public static Debug get() {
    return instance;
  }

  public void console(boolean enable) {
    console = enable;
  }

  /**
   * 輸出console.
   *
   * @param str 日誌
   */
  public void print(String str) {
    if (console) {
      System.out.print(simpleDateFormat.format(new Date()) + " " + str);
    }
  }

  /**
   * 輸出console.
   *
   * @param str 日誌
   */
  public void println(String str) {
    if (console) {
      System.out.println(simpleDateFormat.format(new Date()) + " " + str);
    }
  }

  /**
   * 輸出錯誤.
   *
   * @param cause 例外
   */
  public String toString(Throwable cause) {
    return cause.toString()
        + System.lineSeparator()
        + Arrays.stream(cause.getStackTrace())
            .map(String::valueOf)
            .reduce((a, b) -> a.concat(b).concat(System.lineSeparator()))
            .get();
  }
}
