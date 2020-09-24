package tetris.view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

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
    canvas.drawString(text, x, y);

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
