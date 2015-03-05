package casia.isiteam.videosearch.master;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.protocol.Protocol.Request;
import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.ProtocolParam;
import casia.isiteam.videosearch.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientRequestHandler extends
		SimpleChannelInboundHandler<Protocol.Request> {

	private MasterCmdServer masterCmdServer;
	public ClientRequestHandler(MasterCmdServer masterCmdServer) {
		// TODO Auto-generated constructor stub
		this.masterCmdServer=masterCmdServer;
	}
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Request msg)
			throws Exception {
		// TODO Auto-generated method stub

		switch (msg.getHead().getMethodType()) {
		
		case ADD_VIDEO:
		case DELETE_VIDEO:
			dispatchRequest(ctx,msg);
			break;
		case SEARCH_VIDEO:
			handleSearch(ctx, msg);
			break;
		default:
			break;

		}
	}

	private void dispatchRequest(ChannelHandlerContext ctx, Request msg){
		String fileID=msg.getBody().getContext();
		String[] ret=Util.getFileGroupAndName(fileID);
		
		for(SlaveIndexerClient slaveIndexerClient:masterCmdServer.getSlaveIndexerClients()){
			if(slaveIndexerClient.getGroupName().equals(ret[0])){
				
				Request.Head.Builder headBuilder=Request.Head.newBuilder();
				headBuilder.setMessageType(Protocol.MessageType.NORMAL);
				headBuilder.setID(masterCmdServer.getRequestID());
				headBuilder.setMethodType(msg.getHead().getMethodType());
				
				Request.Body.Builder bodyBuilder=Request.Body.newBuilder();
				bodyBuilder.setContext(msg.getBody().getContext());				
				Request.Builder builder=Request.newBuilder();
				
				builder.setHead(headBuilder.build());
				builder.setBody(bodyBuilder.build());
				
				Request request=builder.build();
				
				slaveIndexerClient.getClientChannel().writeAndFlush(request);
				
				masterCmdServer.getRequestMap().put(request.getHead().getID(), ctx);
				break;
			}
		}
		
	}
	
	private void handleSearch(ChannelHandlerContext ctx, Request msg) throws FileNotFoundException{
		
		
		String context=msg.getBody().getContext();
		String [] strs=ProtocolParam.seperator.split(context);
		
		if(strs.length <=2){
			
		}
		String fileName=strs[0];
		long fileSize=Long.parseLong(strs[1]);
		
		ctx.pipeline().addFirst(new ChunkedReadHandler(fileName, fileSize));
		
		
	}
}

class ChunkedReadHandler extends ChannelHandlerAdapter{
	private long fileSize;
	private File file;
	private FileOutputStream ofs;
	private long readedSize=0;
	public ChunkedReadHandler(String fileName, long fileSize) throws FileNotFoundException{
		this.fileSize=fileSize;
		this.file = new File(fileName);
		
		ofs=new FileOutputStream(this.file);
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf buf= (ByteBuf) msg;		
		readedSize +=buf.readableBytes();
		if(buf.isReadable()){
			
			byte[] bytes=new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			ofs.write(bytes);
		}
		
		if(readedSize >= fileSize){
			ctx.pipeline().remove(this);
			ofs.flush();
			ofs.close();
			System.out.println("close file");
		}
		buf.release();
	}
}

