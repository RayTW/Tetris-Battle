package tetris.view;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import tetris.Config;
import tetris.game.battle.Client;
import tetris.view.component.Label;
import tetris.view.component.RepaintView;
import util.Debug;

/**
 * 對戰連線畫面
 *
 * @author Ray
 */
public class MatchingView extends RepaintView {
  private static final long serialVersionUID = 1L;
  private Label connectingText;
  private Thread thread;
  private boolean isConnecting = true;;

  public MatchingView(JSONObject params) {
    super(params);
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

                connectingText.setText("Matching" + repeated);
                try {
                  TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                }
              }
            });
    thread.start();
    Debug.get().println("connect " + Config.get().getHost() + "/" + Config.get().getPort());
    Client.get().connect(Config.get().getHost(), Config.get().getPort());

    JSONObject params = new JSONObject();

    Client.get()
        .getKcp()
        .ifPresent(
            k -> {
              k.setOnReadedListener(
                  msg -> {
                    JSONObject json = new JSONObject(msg);
                    if (json.getInt("code") == 300) {
                      params.put("position", json.getInt("position"));
                      return;
                    }
                    if (json.getInt("code") == 400) {

                      params.put("users", json.getJSONArray("users"));
                      changeView(ViewName.BATTLE, params);
                    }
                  });
            });

    JSONObject json = new JSONObject();

    json.put("code", 1);
    json.put("name", Config.get().getUserName());

    Client.get().write(json);
  }

  @Override
  public void onKeyCode(int code) {
    if (code == KeyEvent.VK_ESCAPE) {
      Client.get().close();
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
