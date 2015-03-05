package casia.isiteam.videosearch.master;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
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
import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.ProtocolParam;
import casia.isiteam.videosearch.util.Util;

public class MasterCmdServer implements Runnable {

	private MasterIndexer masterIndexer;
	private CopyOnWriteArraySet<SlaveIndexerClient> slaveIndexerClients;
	private ConcurrentMap<Integer,ChannelHandlerContext> requestMap;
	private int requestID=0;
	
	private String host;
	private int port;
	
	public MasterCmdServer(String host,int port) {
		// TODO Auto-generated constructor stub
		this.host=host;
		this.port=port;

		slaveIndexerClients = new CopyOnWriteArraySet<SlaveIndexerClient>();
		requestMap=new ConcurrentHashMap<Integer, ChannelHandlerContext>();
	}
	
	
	public ConcurrentMap<Integer, ChannelHandlerContext> getRequestMap() {
		return requestMap;
	}
	
	public CopyOnWriteArraySet<SlaveIndexerClient> getSlaveIndexerClients() {
		return slaveIndexerClients;
	}
	public int getRequestID(){
		if(requestID >= Long.MAX_VALUE){
			requestID=0;
		}		
		return requestID++;
	}
	
	public void addSlaveIndexerClients(SlaveIndexerClient slaveIndexerClient) {
		slaveIndexerClients.add(slaveIndexerClient);
	}

	public void removeSlaveIndexerClients(SlaveIndexerClient slaveIndexerClient) {
		slaveIndexerClients.remove(slaveIndexerClient);
	}
	
	@Override
	public void run() {
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

									// 过滤掉心跳包和slave初次链接的包
									pipeline.addLast(new MasterHeartBeatHandler(
											masterCmdServer));
									pipeline.addLast(new ClientRequestHandler(
											masterCmdServer));

								}
							});

			// 绑定端口 等待绑定成功
			ChannelFuture f = b.bind(host, port)
					.sync();
			// 等待服务器退出
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			Util.printContextInfo(e.getMessage());
			return;
		} finally {
			workGroup.shutdownGracefully();
		}
	}

}
