package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import tetris.Config;
import tetris.view.component.RepaintView;
import util.Debug;
import util.Pair;
import tetris.view.component.FlashLabel;
import tetris.view.component.Label;
import tetris.view.component.MulticolorLabel;

public class MenuView extends RepaintView {
  private static final long serialVersionUID = 1L;

  private MulticolorLabel menuTitle;
  private Label singleText; // 單人遊玩選項
  private Label battleText; // 單人遊玩選項
  private FlashLabel pressEnter;
  private int arrow;

  @SuppressWarnings("unchecked")
  private Pair<ViewName, String[]>[] options =
      new Pair[] {
        new Pair<>(ViewName.SINGLE, new String[] {">SINGLE", "BATTLE"}),
        new Pair<>(ViewName.BATTLE, new String[] {"SINGLE", ">BATTLE"})
      };

  public MenuView(int width, int height) {
    super(width, height);
  }

  @Override
  public void init() {
    Config config = Config.get();

    menuTitle =
        new MulticolorLabel(
            "ＴＥＴＲＩＳ",
            new Color[] {
              Color.RED, Color.GREEN, Color.ORANGE, Color.PINK, Color.BLUE, Color.YELLOW
            },
            config.zoom(50));
    menuTitle.setLocation(config.zoom(10), config.zoom(120));
    menuTitle.setFont(Font.BOLD, config.zoom(70));
    menuTitle.setFlash(false);
    add(menuTitle);

    pressEnter = new FlashLabel();
    pressEnter.setLocation(config.zoom(100), config.zoom(250));
    pressEnter.setFont(Font.BOLD, config.zoom(20));
    pressEnter.setText("PRESS ENTER");
    pressEnter.setFlash(true);
    pressEnter.setColor(Color.ORANGE);
    add(pressEnter);

    singleText = new Label();
    singleText.setLocation(config.zoom(100), config.zoom(350));
    singleText.setFont(Font.BOLD, config.zoom(35));
    add(singleText);

    battleText = new Label();
    battleText.setLocation(config.zoom(100), config.zoom(385));
    battleText.setFont(Font.BOLD, config.zoom(35));
    add(battleText);

    setModeArrow(arrow);
  }

  @Override
  public void onKeyCode(int code) {
    if (code == KeyEvent.VK_ENTER) {
      ViewName mode = options[arrow].getFirst();

      if (mode == ViewName.SINGLE) {
        // 單機
        changeView(ViewName.SINGLE);
      } else if (mode == ViewName.BATTLE) {
        if (doConnectSettings()) {
          // 對戰
          changeView(ViewName.CONNECTING);
        }
      }
    } else if (code == KeyEvent.VK_UP) { // 遊標上移
      arrow--;
      arrow += options.length;
      arrow %= options.length;
      setModeArrow(arrow);
    } else if (code == KeyEvent.VK_DOWN) { // 遊標下移
      arrow++;
      arrow %= options.length;
      setModeArrow(arrow);
    }
  }

  private void setModeArrow(int r) {
    singleText.setText(options[r].getSecond()[0]);
    battleText.setText(options[r].getSecond()[1]);
  }

  private boolean doConnectSettings() {
    JTextField name = new JTextField();
    JTextField host = new JTextField();
    JTextField port = new JTextField();

    final JComponent[] inputs =
        new JComponent[] {
          new JLabel("User Name"), name, new JLabel("Host"), host, new JLabel("Port"), port
        };

    name.setText(Config.get().getUserName());
    // 讓user name輸入框取得focus
    name.addAncestorListener(
        new AncestorListener() {

          @Override
          public void ancestorAdded(AncestorEvent event) {}

          @Override
          public void ancestorRemoved(AncestorEvent event) {}

          @Override
          public void ancestorMoved(AncestorEvent event) {
            SwingUtilities.invokeLater(
                new Runnable() {
                  @Override
                  public void run() {
                    name.requestFocusInWindow();
                  }
                });
          }
        });

    int result =
        JOptionPane.showConfirmDialog(null, inputs, "Connect settings", JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      Debug.get()
          .println("You entered " + name.getText() + ", " + host.getText() + ", " + port.getText());

      if (host.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "host error", "error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      if (port.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "port error", "error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      if (name.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "user name error", "error", JOptionPane.ERROR_MESSAGE);
        return false;
      }

      Config.get().setHostPort(host.getText(), Integer.parseInt(port.getText()));
      Config.get().setUserName(name.getText());
      return true;
    } else {
      Debug.get().println("User canceled / closed the dialog");
      return false;
    }
  }

  @Override
  public void release() {
    super.release();
  }
}
