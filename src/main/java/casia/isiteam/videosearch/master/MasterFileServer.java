package casia.isiteam.videosearch.master;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.util.ProtocolParam;

public class MasterFileServer implements Runnable{

	private String host;
	int port;
	
	public MasterFileServer(String host,int port) {
		// TODO Auto-generated constructor stub
		this.host=host;
		this.port=port;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			final MasterCmdServer masterCmdServer = this;
			// 配置服务器启动类
			ServerBootstrap b = new ServerBootstrap();
			b.group(workGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new LoggingHandler(LogLevel.INFO))
					// 配置日志输出
					.childHandler(
							new ChannelInitializer<ServerSocketChannel>() {
								@Override
								protected void initChannel(
										ServerSocketChannel ch)
										throws Exception {
									ChannelPipeline pipeline = ch.pipeline();

									// inbound
									pipeline.addLast(new ProtobufVarint32FrameDecoder());
									pipeline.addLast(new ProtobufDecoder(
											Protocol.Request
													.getDefaultInstance()));

									// outbound
									pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
									pipeline.addLast(new ProtobufEncoder());

									pipeline.addLast(
											ProtocolParam.idleHandlerName,
											new IdleStateHandler(
													ProtocolParam.idleReadTimeInSeconds,
													ProtocolParam.idleWriteTimeInSeconds,
													0));


								}
							});

			// 绑定端口 等待绑定成功
			ChannelFuture f = b.bind(host, port)
					.sync();
			// 等待服务器退出
			f.channel().closeFuture().sync();
	}

}
