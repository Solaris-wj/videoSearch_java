package casia.isiteam.videosearch.slave;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.io.IOException;
import casia.isiteam.videosearch.protocol.Protocol;

public class SlaveIndexer {
	private IndexImpl indexImpl = null;

	String tempFileDir = null;
	String dataDir = null;
	String logDir = null;
	String configFilePath = null;
	String algoConfFilePath = null;

	String masterHost = null;
	int masterPort;

	String host = null;
	int port;
	Configuration configuration = null;

	public SlaveIndexer(String confFilePath, String algoConfFilePath)
			throws IOException {
		this.configFilePath = confFilePath;
		this.algoConfFilePath = algoConfFilePath;

		configuration = new Configuration(configFilePath, this);
		indexImpl = new IndexImpl(dataDir, logDir, configFilePath);

	}

	public void start() throws InterruptedException {
		// 网络事件处理线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 配置客户端启动类
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new ProtobufVarint32FrameDecoder());
							pipeline.addLast(new ProtobufDecoder(
									Protocol.Request.getDefaultInstance()));
							pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
							pipeline.addLast(new ProtobufEncoder());
							
							pipeline.addLast(new SlaveHeartBeatHandler());

						}
					});
			// 连接服务器 同步等待成功
			ChannelFuture f = b.connect(host, port).sync();

			// 同步等待客户端通道关闭
			f.channel().closeFuture().sync();
		} finally {
			// 释放线程组资源
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("args <1");
			System.out.println("must provide configure file path");
		}

		String configFilePathString = args[0];
		@SuppressWarnings("unused")
		SlaveIndexer slaveIndexer = new SlaveIndexer(configFilePathString, null);
	}
}
