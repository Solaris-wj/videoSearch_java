package casia.isiteam.videosearch.master;

import casia.isiteam.videosearch.protocol.Protocol;
import casia.isiteam.videosearch.protocol.Protocol.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SlaveResponseHandler extends SimpleChannelInboundHandler<Protocol.Response> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Response msg)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
