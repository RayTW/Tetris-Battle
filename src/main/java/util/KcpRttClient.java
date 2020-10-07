package util;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.json.JSONObject;
import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Measures RTT(Round-trip time) for KCP.
 *
 * <p>Sends a message to server and receive a response from server to measure RTT.
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class KcpRttClient {
  private int conv;
  private String host;
  private int port;
  private ChannelFuture channelFuture;
  private EventLoopGroup group;
  private OnConnectedListener onConnectedListener;
  private OnReadedListener onReadedListener;
  private OnDisconnectedListener onDisconnectedListener;

  private KcpRttClient() {}

  public static class Builder {
    private int conv;
    private String host;
    private int port;

    public KcpRttClient build() {
      Objects.requireNonNull(this.host, "host == null");

      KcpRttClient client = new KcpRttClient();

      client.conv = this.conv > 0 ? this.conv : 10;
      client.host = this.host;
      client.port = this.port;

      return client;
    }

    public Builder setConv(int conv) {
      this.conv = conv;
      return this;
    }

    public Builder setHost(String host) {
      this.host = host;
      return this;
    }

    public Builder setPort(int port) {
      this.port = port;
      return this;
    }
  }

  public void connect() throws InterruptedException {
    // Configure the client.
    group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group)
        .channel(UkcpClientChannel.class)
        .handler(
            new ChannelInitializer<UkcpChannel>() {
              @Override
              public void initChannel(UkcpChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(
                    new ChannelInboundHandlerAdapter() {
                      @Override
                      public void channelRegistered(ChannelHandlerContext ctx) throws Exception {}

                      @Override
                      public void channelActive(final ChannelHandlerContext ctx) {
                        UkcpChannel kcpCh = (UkcpChannel) ctx.channel();
                        kcpCh.conv(conv);

                        if (onConnectedListener != null) {
                          onConnectedListener.onConnected();
                        }
                      }

                      @Override
                      public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        if (onDisconnectedListener != null) {
                          onDisconnectedListener.onDisconnected();
                        }
                      }

                      @Override
                      public void channelRead(final ChannelHandlerContext ctx, Object msg) {
                        ByteBuf buf = (ByteBuf) msg;
                        CharSequence str =
                            buf.getCharSequence(0, buf.capacity(), StandardCharsets.UTF_8);
                        buf.release();

                        if (onReadedListener != null) {
                          onReadedListener.onReaded(str.toString());
                        }
                      }

                      @Override
                      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        // Close the connection when an exception is raised.
                        Debug.get()
                            .println(
                                "client.exceptionCaught,ctx="
                                    + ctx
                                    + ",\n"
                                    + Debug.get().toString(cause));
                        ctx.close();
                      }
                    });
              }
            });
    ChannelOptionHelper.nodelay(b, true, 20, 2, true).option(UkcpChannelOption.UKCP_MTU, 512);

    // Start the client.
    channelFuture = b.connect(host, port).sync();
  }

  public void close() throws InterruptedException {
    try {
      // Wait until the connection is closed.
      channelFuture.channel().closeFuture();
    } finally {
      // Shut down the event loop to terminate all threads.
      group.shutdownGracefully();
    }
  }

  public void write(JSONObject json) {
    write(json.toString());
  }

  public void write(String msg) {
    ByteBuf buf = Unpooled.wrappedBuffer(msg.getBytes(StandardCharsets.UTF_8));
    channelFuture.channel().writeAndFlush(buf);
  }

  public void write(ByteBuf buf) {
    channelFuture.channel().writeAndFlush(buf);
  }

  public void setOnConnectedListener(OnConnectedListener listener) {
    onConnectedListener = listener;
  }

  public void setOnReadedListener(OnReadedListener listener) {
    onReadedListener = listener;
  }

  public void setOnDisconnectedListener(OnDisconnectedListener listener) {
    onDisconnectedListener = listener;
  }

  public static interface OnConnectedListener {
    void onConnected();
  }

  public static interface OnReadedListener {
    void onReaded(String msg);
  }

  public static interface OnDisconnectedListener {
    void onDisconnected();
  }
}
