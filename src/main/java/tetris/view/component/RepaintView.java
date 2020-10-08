package tetris.view.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import tetris.Config;
import util.FpsCounter;

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
  private boolean isRepain = true;
  private Font fpsFont;
  private Thread repainThread;
  private FpsCounter fpsCounter;

  public RepaintView(int width, int height) {
    rolePool = new CopyOnWriteArrayList<>();
    screenWidth = width;
    screenHeight = height;

    // 設定畫面大小
    setSize(width, height);
    repainThread = new Thread(this::doRepain);
    fpsCounter = new FpsCounter();
  }

  @Override
  public final void initialize() {
    init();
    repainThread.start();
    fpsCounter.start();
  }

  public void init() {}

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

    if (isDarkMode()) {
      canvas.setColor(Color.DARK_GRAY);
      canvas.fillRect(0, 0, screenWidth, screenHeight);
    }

    onPaintComponent(canvas);
    fpsCounter.frame();

    if (Config.get().isDisplayFps()) {
      if (fpsFont == null) {
        Font currentFont = canvas.getFont();
        Font newFont = currentFont.deriveFont(Font.BOLD, Config.get().zoom(14));
        fpsFont = newFont;
      }
      canvas.setColor(Color.BLACK);
      canvas.setFont(fpsFont);
      canvas.drawString(
          "FPS:" + fpsCounter.get() + "/" + Config.get().getMaxFps(),
          getWidth() - Config.get().zoom(80),
          Config.get().zoom(20));
    }

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

  private void doRepain() {
    while (isRepain) {
      try {
        Thread.sleep(Config.get().getRepainMills());
        try {
          SwingUtilities.invokeAndWait(super::repaint);
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      } catch (InterruptedException e) {
      }
    }
  }

  @Override
  public void release() {
    isRepain = false;
    repainThread.interrupt();
    fpsCounter.stop();
  }
}
