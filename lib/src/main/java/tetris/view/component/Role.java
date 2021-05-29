package tetris.view.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * 場景上的全部角色都要繼承的class.
 *
 * @author Ray Lee
 */
public class Role {
  // 顯示出來的範圍
  private int localX;
  private int localY;
  private int width;
  private int height;
  private Color color;
  private boolean enableCenter;
  private Image image;

  public Role() {
    enableCenter = false;
  }

  public void setEnableCenter(boolean enable) {
    enableCenter = enable;
  }

  /**
   * 判斷其他物件是否碰撞到目前物件.
   *
   * @param obj 角色
   */
  public boolean hitTest(Role obj) {
    int hx = obj.getX();
    int hy = obj.getY();
    int hh = obj.height;
    int hw = obj.width;

    return ((hx + hw > getX())
        && (hx < getX() + width)
        && (hy + hh > getY())
        && (hy < getY() + height));
  }

  /**
   * 碰撞測試.
   *
   * @param x 位置x
   * @param y 位置y
   * @param w 高
   * @param h 寬
   */
  public boolean hitTest(int x, int y, int w, int h) {
    return (((x + w) > getX())
        && (x < getX() + width)
        && ((y + h) > getY())
        && (y < getY() + height));
  }

  /** 取得x. */
  public int getX() {
    if (enableCenter) {
      return localX - (width / 2);
    }
    return localX;
  }

  /** 取得y. */
  public int getY() {
    if (enableCenter) {
      return localY - (height / 2);
    }
    return localY;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  /**
   * 設定x.
   *
   * @param x 位置x
   */
  public void setX(int x) {
    if (enableCenter) {
      this.localX = x + (width / 2);
    } else {
      this.localX = x;
    }
  }

  /**
   * 設定x.
   *
   * @param y 位置y
   */
  public void setY(int y) {
    if (enableCenter) {
      this.localY = y + (height / 2);
    } else {
      this.localY = y;
    }
  }

  public void setLocation(int x, int y) {
    setX(x);
    setY(y);
  }

  public void setWidth(int w) {
    width = w;
  }

  public void setHeight(int h) {
    height = h;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public Image getImage() {
    return image;
  }

  public void onDraw(Graphics canvas) {}
}
