package tetris.view.component;

import java.awt.Font;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class JDialogLabel extends JDialog {
  private static final long serialVersionUID = 1L;

  private JLabel lable;

  public JDialogLabel() {
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
