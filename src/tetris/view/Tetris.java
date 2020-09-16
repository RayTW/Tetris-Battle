package tetris.view;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import tetris.Config;

/**
 * 俄羅斯方塊主程式,建立GameView並add之後執行
 *
 * @author Ray Lee
 */
public class Tetris extends JFrame {
  private static final long serialVersionUID = 1L;
  private GameView gameView;

  public Tetris() {
    initialize();
  }

  public void initialize() {
    gameView = new GameView();
    gameView.initialize();
    getContentPane().add(gameView);

    // 鍵盤事件處理
    addKeyListener(
        new KeyListener() {
          @Override
          public void keyReleased(KeyEvent e) {}

          @Override
          public void keyPressed(KeyEvent e) {
            if (gameView != null) {
              gameView.keyCode(e.getKeyCode());
            }
          }

          @Override
          public void keyTyped(KeyEvent e) {}
        });
  }

  /** @param args */
  public static void main(String[] args) {
    Tetris tetris = new Tetris();
    tetris.setTitle("俄羅斯方塊-" + Config.get().getVersion());
    tetris.setSize(
        Config.get().convertValueViaScreenScale(350),
        Config.get().convertValueViaScreenScale(480) + 20);
    tetris.setLocation(350, 50);
    tetris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tetris.setResizable(false); // 視窗放大按鈕無效
    tetris.setVisible(true);
  }
}
