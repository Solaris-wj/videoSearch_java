package casia.isiteam.videosearch.master;

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
public class MasterHeartBeatHandler extends MessageToMessageDecoder<Protocol.Request> {

	private MasterCmdServer masterCmdServer = null;
	private SlaveIndexerClient slaveIndexerClient = null;

	public MasterHeartBeatHandler(MasterCmdServer masterCmdServer) {
		// TODO Auto-generated constructor stub
		this.masterCmdServer = masterCmdServer;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		// 建立连接后马上发送心跳
		IdleStateEvent event = IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
		ctx.fireUserEventTriggered(event);

	};

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {

		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				// 未收到slave的心跳包，关闭连接，移除slave，并且记录
				// TODO 记录
				ctx.close();
				masterCmdServer.removeSlaveIndexerClients(slaveIndexerClient);
			} else if (event.state() == IdleState.WRITER_IDLE) {
				// 发送心跳包给客户端
				Protocol.Request.Builder builder = Protocol.Request
						.newBuilder();
				builder.setHead(Protocol.Request.Head.newBuilder()
						.setMessageType(Protocol.MessageType.HEART_BEAT));
				ctx.writeAndFlush(builder.build());
			}
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Request msg,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub

		// 收到心跳
		if (msg.getHead().getMessageType() == Protocol.MessageType.HEART_BEAT) {
			// TODO 收到心跳，记录


			String groupName=msg.getBody().getContext();
			
			SlaveIndexerClient slaveIndexerClient = new SlaveIndexerClient();
			slaveIndexerClient.setClientChannel(ctx.channel());
			slaveIndexerClient.setGroupName(groupName);
			masterCmdServer.addSlaveIndexerClients(slaveIndexerClient);
			this.slaveIndexerClient = slaveIndexerClient;
			
			
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
