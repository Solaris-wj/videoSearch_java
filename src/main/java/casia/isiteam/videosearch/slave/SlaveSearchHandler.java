package casia.isiteam.videosearch.slave;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.protocol.Protocol.Request;
import casia.isiteam.videosearch.util.ProtocolParam;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SlaveSearchHandler extends
		SimpleChannelInboundHandler<Protocol.Request> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Request msg)
			throws Exception {
		// TODO Auto-generated method stub

		switch (msg.getHead().getMethodType()) {
		
		case ADD_VIDEO:
			handleAdd(ctx, msg);
			break;
		case DELETE_VIDEO:
			handleDelete(ctx, msg);
			break;
		case SEARCH_VIDEO:
			handleSearch(ctx, msg);
			break;
		default:
			break;

		}
	}

	private void handleAdd(ChannelHandlerContext ctx, Request msg){
		
	}
	private void handleDelete(ChannelHandlerContext ctx, Request msg){
		
	}
	private void handleSearch(ChannelHandlerContext ctx, Request msg) throws FileNotFoundException{
		//ctx.pipeline().addFirst("fileTransfer",new ChunkedWriteHandler());
		
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

