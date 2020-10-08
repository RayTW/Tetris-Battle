package tetris.view;

import tetris.view.component.ComponentView;

public class ViewFactory {

  public ViewFactory() {}

  public ComponentView create(ViewName name, int width, int height, boolean darkMode) {
    ComponentView view = null;

    switch (name) {
      case SINGLE:
        view = new SingleView(width, height);
        break;
      case MATCHING:
        view = new MatchingView(width, height);
        break;
      case BATTLE:
        view = new BattleView(width, height);
        break;
      case MENU:
        view = new MenuView(width, height);
        break;
      case HOW_TO_PLAY:
        view = new HowToPlayView(width, height);
        break;
      default:
    }

    view.setDarkMode(darkMode);
    return view;
  }
}
