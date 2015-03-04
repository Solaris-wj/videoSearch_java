package casia.isiteam.videosearch.slave;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.ws.Endpoint;


public class SlaveIndexer {
	private IndexImpl indexImpl=null;

	String tempFileDir=null;
	String dataDir=null;
	String logDir=null;
	String configFilePath=null;
	String algoConfFilePath=null;
	
	String masterHost=null;
	int masterPort;
	
	String localhost=null;
	int localPort;
	Configuration configuration=null;
	public SlaveIndexer(String confFilePath, String algoConfFilePath) throws IOException, URISyntaxException {
		this.configFilePath=confFilePath;	
		this.algoConfFilePath=algoConfFilePath;
		
		configuration = new Configuration(configFilePath,this);			
		indexImpl=new IndexImpl(dataDir,logDir,configFilePath);		
		SlaveIndexerService indexerService=new SlaveIndexerServiceImpl(dataDir, logDir, algoConfFilePath);
		
		URL url=new URL("http", "0.0.0.0", localPort, SlaveRegisterService.class.getSimpleName());
		Endpoint.publish(url.toString(), indexerService);		
		
		registerToMaster(url.toString());
		
	}
	
	private boolean registerToMaster(String indexServiceURL) {
		

		try {
			EventLoopGroup workGroup=new NioEventLoopGroup();
			
			Bootstrap bootstrap=new Bootstrap();
			bootstrap.group(workGroup);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler( new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			bootstrap.connect();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return true;
	}
	
	public IndexImpl getInderJNI() {
		return indexImpl;
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException{
		if(args.length < 1){
			System.out.println("args <1");
			System.out.println("must provide configure file path");
		}
		
		String configFilePathString=args[0];
		@SuppressWarnings("unused")
		SlaveIndexer slaveIndexer=new SlaveIndexer(configFilePathString, null);			
	}
}
