package casia.isiteam.videosearch.master;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;


import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.Util;

/**
 * 
 * @author dell
 * 
 */
public class SlaveManager {

	public static final ByteBuf delimiterBuf = Unpooled
			.copiedBuffer(new String("\r\n").getBytes());
	public static final int MAX_LINE = 256;

	// 已经报告地址和端口的slave，等待连接检索服务
	BlockingQueue<SlaveIndexerClient> unConnSlaves;
	//CopyOnWriteArraySet<SlaveIndexerService> slaveIndexer;

	CopyOnWriteArraySet<SlaveIndexerClient> slaveIndexerClients;
	
	String host;
	int registerPort;

	public SlaveManager(String host, int port) {
		this.host = host;
		this.registerPort = port;

		unConnSlaves = new LinkedBlockingDeque<SlaveIndexerClient>();

		slaveIndexerClients = new CopyOnWriteArraySet<SlaveIndexerClient>();

	}

	public boolean addRegisteredSlave(SlaveIndexerClient slaveIndexerClient) {
		return unConnSlaves.offer(slaveIndexerClient);
	}

	public void start() {
		Thread registerThread = new Thread(new RegisterService(this));
		registerThread.start();
		
		Thread connThread = new Thread();
		connThread.start();
	}
}

class RegisterService implements Runnable {

	SlaveManager slaveManager;

	public RegisterService(SlaveManager slaveManager) {
		this.slaveManager = slaveManager;
	}

	public void run() {

		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			final SlaveManager slaveManager = this.slaveManager;
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
									pipeline.addLast(new DelimiterBasedFrameDecoder(
											SlaveManager.MAX_LINE,
											SlaveManager.delimiterBuf));
									pipeline.addLast(new StringDecoder());
									pipeline.addLast(new RegisterHandler(
											slaveManager));

									// outbound
									pipeline.addLast(new StringEncoder());
								}
							});

			// 绑定端口 等待绑定成功
			ChannelFuture f = b.bind(slaveManager.host,
					slaveManager.registerPort).sync();
			// 等待服务器退出
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			Util.printContextInfo(e.getMessage());			
			return ; 
		} finally {
			workGroup.shutdownGracefully();
		}
	}
}

class ConnectSlaveService implements Runnable {
	SlaveManager slaveManager;

	public ConnectSlaveService(SlaveManager slaveManager) {
		this.slaveManager = slaveManager;
	}

	@Override
	public void run() {

		SlaveIndexerClient slaveIndexerClient;
		try {
			slaveIndexerClient = slaveManager.unConnSlaves.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Util.printContextInfo(e.getMessage());
			return ;
		}

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

						}
					});

			// 连接服务器 同步等待成功
			ChannelFuture f = b.connect(slaveIndexerClient.getSocketAddr()).sync();

			slaveIndexerClient.setClientChannel(f.channel());
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			Util.printContextInfo(e.getMessage());
			group.shutdownGracefully();
		} finally {
			// 释放线程组资源
			group.shutdownGracefully();
		}
	}
}