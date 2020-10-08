package tetris.view.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * 場景上的全部角色都要繼承的class
 *
 * @author Ray Lee
 */
public class Role {
  // 顯示出來的範圍
  private int x;
  private int y;
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
   * 判斷其他物件是否碰撞到目前物件
   *
   * @param obj
   * @return
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

  public boolean hitTest(int x, int y, int w, int h) {
    return (((x + w) > getX())
        && (x < getX() + width)
        && ((y + h) > getY())
        && (y < getY() + height));
  }

  public int getX() {
    if (enableCenter) {
      return x - (width / 2);
    }
    return x;
  }

  public int getY() {
    if (enableCenter) {
      return y - (height / 2);
    }
    return y;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public void setX(int x) {
    if (enableCenter) {
      this.x = x + (width / 2);
    } else {
      this.x = x;
    }
  }

  public void setY(int y) {
    if (enableCenter) {
      this.y = y + (height / 2);
    } else {
      this.y = y;
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
