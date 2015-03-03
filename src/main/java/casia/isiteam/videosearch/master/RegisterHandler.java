package casia.isiteam.videosearch.master;

import java.net.InetSocketAddress;
import casia.isiteam.videosearch.slave.client.SlaveIndexerClient;
import casia.isiteam.videosearch.util.Util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;



/**
 * handler to accept slave index registration
 * @author dell
 *
 */
public class RegisterHandler extends SimpleChannelInboundHandler<String>{

	private SlaveManager slaveManager=null;
	public RegisterHandler(SlaveManager slaveManager) {
		// TODO Auto-generated constructor stub
		this.slaveManager=slaveManager;
	}
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String msg)
			throws Exception {
		// TODO Auto-generated method stub
		
		ChannelFuture cf=null;
		try {
			InetSocketAddress addr = (InetSocketAddress)ctx.channel().remoteAddress();
			int port=Integer.parseInt(msg);
			SlaveIndexerClient slaveIndexerClient=new SlaveIndexerClient();
			slaveIndexerClient.setSocketAddr(new InetSocketAddress(addr.getAddress(),port));
			
			slaveManager.addRegisteredSlave(slaveIndexerClient);
			
			cf=ctx.writeAndFlush(new String("OK"));
			
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
		
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //cause.printStackTrace();
        ctx.close();
        Util.printContextInfo(cause.getMessage());       
    }

}
