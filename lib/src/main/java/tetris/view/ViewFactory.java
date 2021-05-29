package tetris.view;

import javax.swing.JFrame;
import org.json.JSONObject;
import tetris.Config;
import tetris.view.component.ComponentView;

/**
 * 產出view工廠.
 *
 * @author ray
 */
public class ViewFactory {
  private Integer width = null;
  private Integer height = null;

  public ViewFactory() {}

  public ComponentView create(JFrame jframe, Config config, ViewName name) {
    return create(jframe, config, name, new JSONObject());
  }

  /**
   * 創建遊戲場景.
   *
   * @param jframe 主場
   * @param config 設定
   * @param name 場景名稱
   * @param params 參數
   */
  public ComponentView create(JFrame jframe, Config config, ViewName name, JSONObject params) {
    ComponentView view = null;
    if (width == null) {
      width = jframe.getWidth();
    }
    if (height == null) {
      height = jframe.getHeight();
    }
    // 將變寬的畫面原還原始大小
    if (jframe.getWidth() != width) {
      jframe.setSize(width, height);
    }

    params.put("width", width);
    params.put("height", height);

    switch (name) {
      case SINGLE:
        view = new SingleView(params);
        break;
      case MATCHING:
        view = new MatchingView(params);
        break;
      case BATTLE:
        // 讓對戰頁變寬，右邊要畫對手的遊戲畫面
        int newWidth = (int) (width * 1.8);
        params.put("width", newWidth);
        view = new BattleView(params);
        jframe.setSize(newWidth, height);
        break;
      case MENU:
        view = new MenuView(params);
        break;
      case HOW_TO_PLAY:
        view = new HowToPlayView(params);
        break;
      default:
    }

    view.setDarkMode(config.isDarkMode());
    return view;
  }
}
