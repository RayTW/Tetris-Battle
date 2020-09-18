package tetris;

import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import tetris.view.GameView;
import tetris.view.MenuView;
import tetris.view.component.RepaintView;
import tetris.view.component.ComponentView;

/**
 * 俄羅斯方塊主程式,建立GameView並add之後執行
 *
 * @author Ray Lee
 */
public class Tetris extends JFrame {
  private static final long serialVersionUID = 1L;
  private ComponentView view;

  public Tetris() {}

  public void initialize() {
    view = new MenuView(getWidth(), getHeight());
    Container pane = getContentPane();

    pane.addContainerListener(
        new ContainerListener() {

          @Override
          public void componentAdded(ContainerEvent e) {
            if (e.getChild() instanceof RepaintView) {
              RepaintView o = (RepaintView) e.getChild();

              o.initialize();
            }
          }

          @Override
          public void componentRemoved(ContainerEvent e) {
            if (e.getChild() instanceof RepaintView) {
              RepaintView o = (RepaintView) e.getChild();

              o.release();
            }
          }
        });

    view.setOnChangeViewListener(
        event -> {
          pane.remove(view);
          switch (event) {
            case SINGLE:
              view = new GameView(getWidth(), getHeight());
              break;
            case MENU:
              view = new MenuView(getWidth(), getHeight());
              break;
            default:
          }
          pane.add(view);
        });
    pane.add(view);

    // 鍵盤事件處理
    addKeyListener(
        new KeyListener() {
          @Override
          public void keyReleased(KeyEvent e) {}

          @Override
          public void keyPressed(KeyEvent e) {
            if (view != null) {
              view.onKeyCode(e.getKeyCode());
            }
          }

          @Override
          public void keyTyped(KeyEvent e) {}
        });
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            // 滑鼠左鍵
            if (e.getButton() == MouseEvent.BUTTON1) {
              if (view != null) {
                view.onMouseClicked(e);
              }
            }
          }
        });
  }

  /** @param args */
  public static void main(String[] args) {
    Tetris tetris = new Tetris();
    tetris.setTitle("俄羅斯方塊-" + Config.get().getVersion());
    tetris.setSize(Config.get().zoom(350), Config.get().zoom(480) + 20);
    tetris.setLocation(350, 50);
    tetris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tetris.setResizable(false); // 視窗放大按鈕無效
    tetris.initialize();
    tetris.setVisible(true);
  }
}
