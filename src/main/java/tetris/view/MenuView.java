package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ColorUIResource;
import org.json.JSONArray;
import org.json.JSONObject;
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
  private Label battleText; // 網路對戰選項
  private Label howToPlayText;
  private FlashLabel pressEnter;
  private int arrow;

  @SuppressWarnings("unchecked")
  private Pair<ViewName, String[]>[] options =
      new Pair[] {
        new Pair<>(ViewName.SINGLE, new String[] {">SINGLE", "BATTLE", "HOW TO PLAY"}),
        new Pair<>(ViewName.BATTLE, new String[] {"SINGLE", ">BATTLE", "HOW TO PLAY"}),
        new Pair<>(ViewName.HOW_TO_PLAY, new String[] {"SINGLE", "BATTLE", ">HOW TO PLAY"})
      };

  public MenuView(JSONObject params) {
    super(params);
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
    singleText.setLocation(config.zoom(55), config.zoom(340));
    singleText.setFont(Font.BOLD, config.zoom(35));
    add(singleText);

    battleText = new Label();
    battleText.setLocation(config.zoom(55), config.zoom(375));
    battleText.setFont(Font.BOLD, config.zoom(35));
    add(battleText);

    howToPlayText = new Label();
    howToPlayText.setLocation(config.zoom(55), config.zoom(410));
    howToPlayText.setFont(Font.BOLD, config.zoom(35));
    add(howToPlayText);

    setModeArrow(arrow);
  }

  @Override
  public void onKeyCode(int code) {
    if (code == KeyEvent.VK_ENTER) {
      ViewName mode = options[arrow].getFirst();

      if (mode == ViewName.SINGLE) {
        // 單機
        changeView(mode);
      } else if (mode == ViewName.BATTLE) {
        if (doConnectSettings()) {
          // 對戰
          changeView(ViewName.MATCHING);
        }
      } else if (mode == ViewName.HOW_TO_PLAY) {
        changeView(mode);
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
    howToPlayText.setText(options[r].getSecond()[2]);
  }

  private boolean doConnectSettings() {
    JTextField name = new JTextField();
    JTextField host = new JTextField();
    JTextField port = new JTextField();

    final JComponent[] inputs =
        new JComponent[] {
          new JLabel("User Name"), name, new JLabel("Host"), host, new JLabel("Port"), port
        };

    if (Config.get().isDarkMode()) {
      UIManager.put("OptionPane.background", new ColorUIResource(Color.DARK_GRAY));
      UIManager.put("Panel.background", new ColorUIResource(Color.DARK_GRAY));
      Arrays.stream(inputs).forEach(o -> o.setForeground(Color.WHITE));
      name.setForeground(Color.BLACK);
      host.setForeground(Color.BLACK);
      port.setForeground(Color.BLACK);
    }

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

  private void debug() {
    JSONObject json = new JSONObject();
    JSONArray ary = new JSONArray();

    json.put("position", 1);

    JSONObject userInfo1 = new JSONObject();
    userInfo1.put("position", 0);
    userInfo1.put("name", "debug");
    ary.put(userInfo1);

    JSONObject userInfo2 = new JSONObject();
    userInfo2.put("position", 1);
    userInfo2.put("name", "debug2");
    ary.put(userInfo2);
    json.put("users", ary);

    changeView(ViewName.BATTLE, json); // TODO test
  }

  @Override
  public void release() {
    super.release();
  }
}
