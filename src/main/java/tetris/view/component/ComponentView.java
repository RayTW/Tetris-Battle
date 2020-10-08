package tetris.view.component;

import java.awt.event.MouseEvent;
import javax.swing.JComponent;

import org.json.JSONObject;

import tetris.view.ViewName;
import tetris.view.listener.OnChangeViewListener;

public abstract class ComponentView extends JComponent {
  private static final long serialVersionUID = 1L;

  private OnChangeViewListener changeViewListener;
  private boolean darkMode = false;

  public ComponentView() {}

  public void initialize() {}

  public void setOnChangeViewListener(OnChangeViewListener listener) {
    changeViewListener = listener;
  }

  public void changeView(ViewName name) {
    changeView(name, new JSONObject());
  }

  public void changeView(ViewName name, JSONObject params) {
    if (changeViewListener != null) {
      changeViewListener.onChangeView(name, params);
    }
  }

  public void setDarkMode(boolean enable) {
    darkMode = enable;
  }

  public boolean isDarkMode() {
    return darkMode;
  }

  public void onKeyCode(int code) {}

  public void onMouseClicked(MouseEvent e) {}

  public void release() {}
}
