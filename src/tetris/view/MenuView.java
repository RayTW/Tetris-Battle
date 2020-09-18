package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import tetris.Config;
import tetris.view.component.RepaintView;
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
  private boolean isRepain = true;
  private Thread repainThread;
  private int arrow;

  @SuppressWarnings("unchecked")
  private Pair<ViewName, String[]>[] options =
      new Pair[] {
        new Pair<>(ViewName.SINGLE, new String[] {">SINGLE", "BATTLE"}),
        new Pair<>(ViewName.BATTLE, new String[] {"SINGLE", ">BATTLE"})
      };

  public MenuView(int width, int height) {
    super(width, height);
    repainThread =
        new Thread(
            () -> {
              while (isRepain) {
                try {
                  TimeUnit.MILLISECONDS.sleep(33);
                  repaint();
                } catch (InterruptedException e) {
                }
              }
            });
    repainThread.start();
  }

  @Override
  public void initialize() {
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
        // 對戰
        changeView(ViewName.BATTLE);
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

  @Override
  public void release() {
    isRepain = false;
    repainThread.interrupt();
  }
}
