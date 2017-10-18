package tetris.view;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import tetris.Config;

/**
 * 俄羅斯方塊主程式,建立GameView並add之後執行
 * 
 * @author 吉他手 Ray
 * 
 */
public class Tetris extends JFrame {
	private GameView mGameView;

	public Tetris() {
		initialize();
	}

	public void initialize() {
		mGameView = new GameView();
		mGameView.initialize();
		getContentPane().add(mGameView);

		// 鍵盤事件處理
		addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (mGameView != null) {
					mGameView.keyCode(e.getKeyCode());
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Tetris tetris = new Tetris();
		tetris.setTitle("俄羅斯方塊V1.3");
		tetris.setSize(Config.get().convertValueViaScreenScale(350), Config.get().convertValueViaScreenScale(480) + 20);
		tetris.setLocation(350, 50);
		tetris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tetris.setResizable(false);// 視窗放大按鈕無效
		tetris.setVisible(true);

	}

}
