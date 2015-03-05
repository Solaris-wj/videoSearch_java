package casia.isiteam.videosearch.master;

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.ProtocolParam;
import casia.isiteam.videosearch.util.Util;

/**
 * 
 * @author dell
 * 
 */
public class MasterIndexer {


	
	String host;
	int port;
	
	
	
	public MasterIndexer(String host, int port) {
		this.host = host;
		this.port = port;

		
	}


	public static void main(String[] args) {
		if(args.length <2){
			System.out.println("args.length < 2");
			System.out.println("input server host and port");
		}
		String host=args[0];
		int port=Integer.parseInt(args[1]);
		MasterIndexer masterIndexer=new MasterIndexer(host, port);
		masterIndexer.start();
	}
}

