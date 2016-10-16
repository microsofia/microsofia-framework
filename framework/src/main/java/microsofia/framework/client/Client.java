package microsofia.framework.client;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.FrameworkException;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.service.ServiceAddress;

//TODO can either run a main client/agent/registry or be embedded
@Server("fwk")
public class Client extends AbstractClient implements IClient{
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
	public void start(){
		super.start();
		try{
			System.out.println("Client connected...");
			Thread.sleep(5000);
			ServiceAddress sa=lookupService.searchAgent();
			
			IAgentService agentService=getAgentService(sa);
			System.out.println("Allocation worked!!!!!!!!!! Response= "+sa);
			System.out.println("Allocation worked!!!!!!!!!! Response= "+agentService.getServiceInfo());
			
			List<IRegistryService> registries=getRegistries();
			for (IRegistryService reg : registries){
				System.out.println("REgistry found=="+reg);
			}
		}catch(Throwable th){
			throw new FrameworkException(th.getMessage(), th);
		}
		log.info("Client ready...");
	}
	
	@Override
	public void stop(){
		super.stop();
		log.info("Client stopped.");
	}

	@Override
	protected String getServiceAddressMap() {
		return "clients";
	}
}
