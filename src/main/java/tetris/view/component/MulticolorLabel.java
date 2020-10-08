package tetris.view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * 遊戲標題
 *
 * @author Ray Lee
 */
public class MulticolorLabel extends Role {
  private int colorAlpha = 255;
  private int alphaFlag = 8;
  private int textGap = 0;
  private int fontStyle = Font.PLAIN;
  private int fontSize = 10;
  private Font titleFont;
  private String text = "";
  private Color[] textColor = {};
  private boolean isFlash = false;

  public MulticolorLabel(String text, Color[] textColor, int textGap) {
    this.text = text;
    this.textColor = textColor;
    this.textGap = textGap;
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

  @Override
  public void onDraw(Graphics canvas) {
    canvas.setColor(getColor());
    Font currentFont = canvas.getFont();

    if (titleFont == null) {
      titleFont = currentFont.deriveFont(fontStyle, fontSize);
    }
    canvas.setFont(titleFont);
    int x = getX();
    int y = getY();
    int gap = textGap;

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

    for (int i = 0; i < text.length(); i++) {
      canvas.setColor(
          new Color(
              textColor[i].getRed(), textColor[i].getGreen(), textColor[i].getBlue(), colorAlpha));
      canvas.drawString(String.valueOf(text.charAt(i)), x + gap * i, y);
    }

    canvas.setFont(currentFont);
  }
}
