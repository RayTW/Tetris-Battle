package tetris.view.component;

import java.awt.Graphics;

import tetris.view.listener.OnClickListener;

/**
 * 在畫面上可以被點擊的遊戲角色
 *
 * @author Ray Lee
 */
public abstract class ClickableRole extends Role {
  private OnClickListener onClickListener;

  @Override
  public void onDraw(Graphics canvas) {
    canvas.setColor(getColor());
    canvas.fillRect(getX(), getY(), getWidth(), getHeight());
  }

  public void onClick() {
    if (onClickListener != null) {
      onClickListener.onClick();
    }
  }

  public void setOnClickListener(OnClickListener listener) {
    onClickListener = listener;
  }
}
