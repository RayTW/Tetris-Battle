package tetris.view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

/**
 * 顯示文字
 *
 * @author Ray Lee
 */
public class Label extends Role {
  private Font font;
  private String text = "";
  private int fontStyle = Font.PLAIN;
  private int fontSize = 10;
  private boolean hidden = false;

  public Label() {
    setColor(Color.BLACK);
  }

  public Label(int width, int height) {
    setWidth(width);
    setHeight(height);
    setColor(Color.BLACK);
  }

  @Override
  public void onDraw(Graphics canvas) {
    if (hidden) {
      return;
    }
    canvas.setColor(getColor());
    Font currentFont = canvas.getFont();

    if (font == null) {
      font = currentFont.deriveFont(fontStyle, fontSize);
    }
    canvas.setFont(font);

    int x = getX();
    int y = getY();

    canvas.setColor(getColor());

    if (text.indexOf("\n") == -1) {
      canvas.drawString(text, x, y);
    } else {
      String[] txt = text.split("\n");
      FontMetrics fm = canvas.getFontMetrics(font);
      Rectangle2D bounds = fm.getStringBounds(text, canvas);
      int height = (int) bounds.getHeight();

      for (int i = 0; i < txt.length; i++) {
        canvas.drawString(txt[i], x, y + (height * i));
      }
    }

    canvas.setFont(currentFont);
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public boolean isHidden() {
    return this.hidden;
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
}
