package casia.isiteam.videosearch.slave.client;

import io.netty.channel.Channel;

public class SlaveIndexerClient {

	String groupName=null;
	Channel clientChannel=null;
	
	@Override
	public int hashCode(){
		return  groupName.hashCode();
	}
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Channel getClientChannel() {
		return clientChannel;
	}

	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}
}
