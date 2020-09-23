package tetris.view.component;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 可繪圖的view.
 *
 * @author Ray Lee Created on 2020/09/16
 */
public class RepaintView extends ComponentView {
  private static final long serialVersionUID = 1L;

  private Image canvasBuffer = null;
  private int screenWidth;
  private int screenHeight;
  private List<Role> rolePool;

  public RepaintView(int width, int height) {
    rolePool = new CopyOnWriteArrayList<>();
    screenWidth = width;
    screenHeight = height;
    // 設定畫面大小
    setSize(width, height);
  }

  // 雙緩衝區繪圖
  @Override
  public final void paintComponent(Graphics g) {
    Graphics canvas = null;

    if (canvasBuffer == null) {
      canvasBuffer = createImage(screenWidth, screenHeight); // 新建一張image的圖
    } else {
      canvasBuffer.getGraphics().clearRect(0, 0, screenWidth, screenHeight);
    }
    canvas = canvasBuffer.getGraphics();

    onPaintComponent(canvas);

    // 將暫存的圖，畫到前景
    g.drawImage(canvasBuffer, 0, 0, this);
  }

  public void add(Role role) {
    rolePool.add(role);
  }

  public void onPaintComponent(Graphics g) {
    rolePool.stream().forEach(r -> r.onDraw(g));
  }

  @Override
  public void onKeyCode(int code) {}

  @Override
  public void onMouseClicked(MouseEvent e) {
    rolePool
        .stream()
        .filter(o -> (o instanceof ClickableRole) && o.hitTest(e.getX(), e.getY(), 2, 2))
        .map(ClickableRole.class::cast)
        .forEach(r -> r.onClick());
  }
}
