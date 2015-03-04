package casia.isiteam.videosearch.slave.client;

import java.net.SocketAddress;

import io.netty.channel.Channel;

public class SlaveIndexerClient {
	//String slaveName;
	//SocketAddress socketAddr=null;
	Channel clientChannel=null;
	
	@Override
	public int hashCode(){
		return  clientChannel.hashCode();
	}

	
//	public SocketAddress getSocketAddr() {
//		return socketAddr;
//	}

//	public void setSocketAddr(SocketAddress socketAddr) {
//		this.socketAddr = socketAddr;
//	}

	public Channel getClientChannel() {
		return clientChannel;
	}

	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}
	
	
//	@Override
//	public boolean equals(Object o){
//		if(! (o instanceof SlaveIndexerClient))
//			return false;
//		
//		SlaveIndexerClient slaveIndexerClient=(SlaveIndexerClient)o;
//		
//		return false;		
//	}
}
