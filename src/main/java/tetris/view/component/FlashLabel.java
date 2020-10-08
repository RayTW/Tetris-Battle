package tetris.view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * 遊戲標題
 *
 * @author Ray Lee
 */
public class FlashLabel extends Role {
  private int colorAlpha = 255;
  private int alphaFlag = 8;
  private Font font;
  private String text = "";
  private int fontStyle = Font.PLAIN;
  private int fontSize = 10;
  private boolean isFlash = false;

  public FlashLabel() {
    setColor(Color.BLACK);
  }

  @Override
  public void onDraw(Graphics canvas) {
    canvas.setColor(getColor());
    Font currentFont = canvas.getFont();

    if (font == null) {
      font = currentFont.deriveFont(fontStyle, fontSize);
    }
    canvas.setFont(font);
    int x = getX();
    int y = getY();

    // 讓文字淡入淡出效果
    if (isFlash) {
      colorAlpha += alphaFlag;
      if (colorAlpha > 255) {
        colorAlpha = 255;
        alphaFlag = -alphaFlag;
      }
      if (colorAlpha < 0) {
        colorAlpha = 0;
        alphaFlag = -alphaFlag;
      }
    }

    canvas.setColor(
        new Color(getColor().getRed(), getColor().getGreen(), getColor().getBlue(), colorAlpha));
    canvas.drawString(text, x, y);

    canvas.setFont(currentFont);
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setFont(int style, int size) {
    fontStyle = style;
    fontSize = size;
  }

  /**
   * 文字閃礫效果
   *
   * @param enable
   */
  public void setFlash(boolean enable) {
    isFlash = enable;
    if (isFlash) {
      colorAlpha = 0;
    }
  }
}
