package tetris.view;

import tetris.view.component.ComponentView;

public class ViewFactory {

  public ViewFactory() {}

  public ComponentView create(ViewName name, int width, int height) {
    ComponentView view = null;

    switch (name) {
      case SINGLE:
        view = new GameView(width, height);
        break;
      case BATTLE:
        view = new BattleView(width, height);
        break;
      case MENU:
        view = new MenuView(width, height);
        break;
      default:
    }
    
    return view;
  }
}
