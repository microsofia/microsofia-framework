package microsofia.framework.client;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Server;

//TODO can either run a main client/agent/registry or be embedded
@Server("fwk")
public class ClientService extends AbstractClientService<ClientInfo> implements IClientService{
	private Log log=LogFactory.getLog(ClientService.class);
	@Inject
	protected ClientConfiguration clientConfiguration;	
	
	public ClientService(){
	}

	@Override
	public ClientConfiguration getClientConfiguration(){
		return clientConfiguration;
	}
	
	@Override
	public ClientInfo getInfo(){
		return serviceInfo;
	}

	@Override
	protected ClientInfo createServiceInfo() {
		return new ClientInfo();
	}
	
	protected void internalStart() throws Exception{
		clients.put(serviceInfo.getPid(),serviceInfo).get();
		log.info("Client connected...");
	}

	@Override
	protected void internalStop() throws Exception{
		clients.remove(serviceInfo.getPid()).get();
		log.info("Client disconnected.");
	}
}
