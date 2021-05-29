package tetris.view;

import java.awt.Font;
import java.awt.event.KeyEvent;

import org.json.JSONObject;

import tetris.Config;
import tetris.view.component.Label;
import tetris.view.component.RepaintView;

/**
 * 遊戲說明
 *
 * @author Ray
 */
public class HowToPlayView extends RepaintView {
  private static final long serialVersionUID = 1L;
  private Label text;

  public HowToPlayView(JSONObject params) {
    super(params);
  }

  @Override
  public void init() {
    Config config = Config.get();
    text = new Label();
    text.setLocation(config.zoom(80), config.zoom(50));
    text.setFont(Font.CENTER_BASELINE, config.zoom(20));

    text.setText(
        "Keyboard Operating\n\n"
            + "esc:Back page\n\n"
            + "↑:Turn \n\n"
            + "↓:Down\n\n"
            + "←:Left\n\n"
            + "→:Right\n\n"
            + "space:Quick down\n\n"
            + "enter:Stop/Resume");

    add(text);
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
  }
}
