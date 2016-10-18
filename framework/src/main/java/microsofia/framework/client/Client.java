package microsofia.framework.client;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;

//TODO can either run a main client/agent/registry or be embedded
@Server("fwk")
public class Client extends AbstractClient<ClientInfo> implements IClient{
	private Log log=LogFactory.getLog(Client.class);
	@Inject
	protected ClientConfiguration clientConfiguration;	
	
	public Client(){
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

		Thread.sleep(5000);
		LookupRequest request=new LookupRequest();
		request.setClientInfo(serviceInfo);
		request.setServiceName("test");
		LookupResult result=lookupService.searchAgent(request);
		if (result.getAgentInfo()==null){
			System.out.println("Allocation worked!!!!!!!!!! no agent found!");
		
		}else{
			IAgentService agentService=getAgentService(result.getAgentInfo());
			System.out.println("Allocation worked!!!!!!!!!! Response= "+result.getAgentInfo());
			System.out.println("Allocation worked!!!!!!!!!! Response= "+agentService.getInfo());
		}
			
		List<IRegistryService> registries=getRegistries();
		for (IRegistryService reg : registries){
			System.out.println("REgistry found=="+reg);
		}
		log.info("Client connected...");
	}

	@Override
	protected void internalStop() throws Exception{
		clients.remove(serviceInfo.getPid()).get();
		log.info("Client disconnected.");
	}
}
