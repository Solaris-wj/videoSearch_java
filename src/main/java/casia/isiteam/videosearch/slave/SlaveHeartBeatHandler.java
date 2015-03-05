package casia.isiteam.videosearch.slave;

import java.util.List;
import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.protocol.Protocol.Request;
import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * handler to accept slave index registration
 * 
 * @author dell
 *
 */
public class SlaveHeartBeatHandler extends MessageToMessageDecoder<Protocol.Request> {

	@Override
	protected void decode(ChannelHandlerContext ctx, Request msg,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub

		// 收到心跳
		if (msg.getHead().getMessageType() == Protocol.MessageType.HEART_BEAT) {
			// TODO 收到心跳， 回复
			Protocol.Response.Builder builder = Protocol.Response.newBuilder();
			builder.setHead(Protocol.Response.Head.newBuilder().setMessageType(
					Protocol.MessageType.HEART_BEAT));
			ctx.writeAndFlush(builder.build());
		} else {
			out.add(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// cause.printStackTrace();
		ctx.close();
		Util.printContextInfo(cause.getMessage());
	}

}
