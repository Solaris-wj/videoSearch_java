package casia.isiteam.videosearch.master;

import java.util.List;
import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.protocol.Protocol.Request;
import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.ProtocolParam;
import casia.isiteam.videosearch.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;



/**
 * handler to accept slave index registration
 * @author dell
 *
 */
public class RegisterHandler extends MessageToMessageDecoder<Protocol.Request>{

	private SlaveManager slaveManager=null;
	private SlaveIndexerClient slaveIndexerClient=null;
	
	public RegisterHandler(SlaveManager slaveManager) {
		// TODO Auto-generated constructor stub
		this.slaveManager=slaveManager;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ChannelFuture cf=null;
		try {

			//添加心跳处理
			ctx.channel().pipeline().addLast(ProtocolParam.idleHandlerName,new IdleStateHandler(ProtocolParam.idleReadTimeInSeconds, ProtocolParam.idleWriteTimeInSeconds, 0));
			
			SlaveIndexerClient slaveIndexerClient=new SlaveIndexerClient();
			slaveIndexerClient.setClientChannel(ctx.channel());
			
			slaveManager.addSlaveIndexerClients(slaveIndexerClient);
			this.slaveIndexerClient=slaveIndexerClient;
			
			//建立连接后马上发送心跳
			IdleStateEvent event=IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
			ctx.fireUserEventTriggered(event);
			
		} catch (NumberFormatException e) {
			// TODO: handle exception
			cf=ctx.writeAndFlush(new String("ERR"));
			Util.printContextInfo(null);
		}
		finally{
			final Channel ch=cf.channel();
			cf.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// TODO Auto-generated method stub
					ch.close();
				}
			});
		}	
	};	


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt)throws Exception {
    	
    	if(evt instanceof IdleStateEvent){
    		IdleStateEvent event=(IdleStateEvent)evt;
    		if(event.state()==IdleState.READER_IDLE){
    			//未收到slave的心跳包，关闭连接，移除slave，并且记录
    			//TODO 记录
    			ctx.close();
    			slaveManager.removeSlaveIndexerClients(slaveIndexerClient);
    		}else if(event.state()==IdleState.WRITER_IDLE){
    			//发送心跳包给客户端
    			Protocol.Request.Builder builder=Protocol.Request.newBuilder();
    			builder.setHead(Protocol.Request.Head.newBuilder().setMessageType(Protocol.MessageType.HEART_BEAT));		
    			ctx.writeAndFlush(builder.build());
    		}
    	}
    }


	@Override
	protected void decode(ChannelHandlerContext ctx, Request msg,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		
		
		//收到心跳
		if(msg.getHead().getMessageType() == Protocol.MessageType.HEART_BEAT){
			//TODO 收到心跳，记录
			
		} else {
			out.add(msg);
		}
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //cause.printStackTrace();
        ctx.close();
        Util.printContextInfo(cause.getMessage());       
    }

        
}
