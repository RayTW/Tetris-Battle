package tetris;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import tetris.view.ViewFactory;
import tetris.view.ViewName;
import tetris.view.component.RepaintView;
import tetris.view.listener.OnChangeViewListener;
import util.AudioManager;
import util.AudioManager.OnPreloadListener;
import tetris.view.component.ComponentView;
import tetris.view.component.JDialogLabel;

/**
 * 俄羅斯方塊主程式,建立GameView並add之後執行
 *
 * @author Ray Lee
 */
public class Tetris extends JFrame implements OnChangeViewListener {
  private static final long serialVersionUID = 1L;
  private ComponentView view;
  private ViewFactory viewFactory;

  public Tetris() {}

  public void initialize() {
    Container pane = getContentPane();
    viewFactory = new ViewFactory();
    view = viewFactory.create(ViewName.MENU, getWidth(), getHeight());

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

    view.setOnChangeViewListener(this);
    pane.add(view);

    // 鍵盤事件處理
    addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (view != null) {
              view.onKeyCode(e.getKeyCode());
            }
          }
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

    preload();
  }

  @Override
  public void onChangeView(ViewName event) {
    getContentPane().remove(view);
    view.setOnChangeViewListener(null);
    view = viewFactory.create(event, getWidth(), getHeight());
    view.setOnChangeViewListener(this);
    getContentPane().add(view);
  }

  private void preload() {
    JDialogLabel dialog = newLoadingDialog();
    ArrayList<String> audioPath = new ArrayList<>();

    dialog.setLabelText("loading...");
    audioPath.add("/sound/music.wav");
    audioPath.add("/sound/down.wav");
    audioPath.add("/sound/turn.wav");

    AudioManager.get()
        .preload(
            audioPath,
            new OnPreloadListener() {

              @Override
              public void onLoaded(String path) {
                SwingUtilities.invokeLater(() -> dialog.setLabelText("load audio : " + path));
              }

              @Override
              public void onCompleted() {
                SwingUtilities.invokeLater(
                    () -> {
                      dialog.dispose();
                      Tetris.this.setVisible(true);
                    });
              }
            });
  }

  public JDialogLabel newLoadingDialog() {
    JDialogLabel jDialog = new JDialogLabel();
    jDialog.setLayout(new GridBagLayout());
    jDialog.setLableFont(new Font("Dialog", Font.ITALIC, 14));
    jDialog.setSize(250, 50);
    jDialog.setLocation(
        getWidth() + (jDialog.getWidth() / 2), getHeight() + (jDialog.getHeight() / 2));
    jDialog.setResizable(false);
    jDialog.setModal(false);
    jDialog.setUndecorated(true);
    jDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    jDialog.setLocationRelativeTo(null);
    jDialog.setVisible(true);

    return jDialog;
  }

  /** @param args */
  public static void main(String[] args) {
    Tetris tetris = new Tetris();

    tetris.setTitle("俄羅斯方塊-" + Config.get().getVersion());
    tetris.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tetris.setSize(Config.get().zoom(350), Config.get().zoom(480) + 20);

    // 遊戲啟動後畫面會置中
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    double x = (1 - (screen.getHeight() / tetris.getHeight())) / 2;
    double y = (1 - (screen.getWidth() / tetris.getWidth())) / 2;
    tetris.setLocation((int) x, (int) y);

    tetris.setLocationRelativeTo(null);

    tetris.setResizable(false); // 視窗放大按鈕無效
    tetris.initialize();
  }
}
