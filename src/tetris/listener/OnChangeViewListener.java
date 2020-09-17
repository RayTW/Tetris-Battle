package tetris.listener;

import tetris.view.ViewChangeEvent;

/**
 * view切換事件
 *
 * @author Ray Lee
 */
public interface OnChangeViewListener {
  /**
   * 當有view切換事件時
   *
   * @param view 即將前往的場景名稱
   */
  public void onChangeView(ViewChangeEvent view);
}
