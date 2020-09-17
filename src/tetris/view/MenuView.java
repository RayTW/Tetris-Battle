package tetris.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import tetris.Config;
import tetris.view.component.Button;
import tetris.view.component.MulticolorText;

public class MenuView extends ControlView {
  private static final long serialVersionUID = 1L;

  private MulticolorText menuTitle;
  private Button singleButton; // 單人遊玩選項按鈕
  private boolean isRepain = true;
  private Thread repainThread;

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
        new MulticolorText(
            "TETRIS",
            new Color[] {
              Color.RED, Color.GREEN, Color.ORANGE, Color.PINK, Color.BLUE, Color.YELLOW
            },
            config.zoom(52));
    menuTitle.setLocation(config.zoom(22), config.zoom(120));
    menuTitle.setFont(Font.BOLD, config.zoom(90));
    add(menuTitle);

    singleButton = new Button(config.zoom(130), config.zoom(50));
    singleButton.setLocation(config.zoom(100), config.zoom(350));
    singleButton.setFont(Font.BOLD, config.zoom(35));
    singleButton.setText("ENTER");
    singleButton.setTextFlash(true);
    singleButton.setOnClickListener(
        () -> {
          getOnChangeViewListener().onChangeView(ViewChangeEvent.GAME);
        });

    add(singleButton);
  }

  public void onKeyCode(int code) {
    if (code == KeyEvent.VK_ENTER) {
      getOnChangeViewListener().onChangeView(ViewChangeEvent.GAME);
    }
  }

  public void release() {
    isRepain = false;
    repainThread.interrupt();
  }
}
