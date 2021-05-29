package tetris.view.component;

import java.awt.Font;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * 彈跳視窗.
 *
 * @author ray
 */
public class DialogLabel extends JDialog {
  private static final long serialVersionUID = 1L;

  private JLabel lable;

  public DialogLabel() {
    lable = new JLabel();
    add(lable);
  }

  public JLabel getLabel() {
    return lable;
  }

  public void setLabelText(String text) {
    lable.setText(text);
  }

  public void setLableFont(Font font) {
    lable.setFont(font);
  }
}
