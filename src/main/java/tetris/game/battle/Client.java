package tetris.game.battle;

import java.util.Optional;
import org.json.JSONObject;
import util.KcpRttClient;

public class Client {
  private static Client instance;
  private Optional<KcpRttClient> kcp;
  private String roomId;

  private Client() {
    kcp = Optional.ofNullable(null);
  }

  /**
   * 取得單一Client物件.
   *
   * @return
   */
  public static Client get() {
    if (instance == null) {
      synchronized (Client.class) {
        if (instance == null) {
          instance = new Client();
        }
      }
    }

    return instance;
  }

  /**
   * 建立連線.
   *
   * @param host 伺服器位置
   * @param port 伺服器埠號
   */
  public void connect(String host, int port) {
    KcpRttClient k = new KcpRttClient.Builder().setHost(host).setPort(port).build();
    try {
      k.connect();
      kcp = Optional.of(k);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public Optional<KcpRttClient> getKcp() {
    return kcp;
  }

  public void write(JSONObject json) {
    kcp.ifPresent(k -> k.write(json));
  }

  public void write(String msg) {
    kcp.ifPresent(k -> k.write(msg));
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getRoomId() {
    return roomId;
  }

  /** 關閉連線. */
  public void close() {
    kcp.ifPresent(
        k -> {
          try {
            k.close();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        });
  }
}
