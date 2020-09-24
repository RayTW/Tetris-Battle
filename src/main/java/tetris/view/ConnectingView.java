package tetris.view;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import tetris.Config;
import tetris.view.component.Label;
import tetris.view.component.RepaintView;

/**
 * 對戰連線畫面
 *
 * @author Ray
 */
public class ConnectingView extends RepaintView {
  private static final long serialVersionUID = 1L;
  private Label connectingText;
  private Thread thread;
  private boolean isConnecting = true;;

  public ConnectingView(int width, int height) {
    super(width, height);
  }

  @Override
  public void init() {
    Config config = Config.get();
    connectingText = new Label();
    connectingText.setLocation(config.zoom(80), config.zoom(250));
    connectingText.setFont(Font.BOLD, config.zoom(35));

    add(connectingText);
    thread =
        new Thread(
            () -> {
              int cnt = 0;
              while (isConnecting) {
                cnt++;
                if (cnt > 3) {
                  cnt = 0;
                }
                String repeated = new String(new char[cnt]).replace("\0", ".");

                connectingText.setText("Connecting" + repeated);
                try {
                  TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                }
              }
            });
    thread.start();
    new Thread(
            () -> {
              try {
                TimeUnit.SECONDS.sleep(1);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              changeView(ViewName.BATTLE);
            })
        .start();
  }

  @Override
  public void onKeyCode(int code) {
    if (code == KeyEvent.VK_ESCAPE) {
      changeView(ViewName.MENU);
      return;
    }
  }

  @Override
  public void release() {
    super.release();
    isConnecting = false;
    thread.interrupt();
  }
}
