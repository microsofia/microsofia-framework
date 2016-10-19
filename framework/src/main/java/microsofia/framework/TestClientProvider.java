package microsofia.framework;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.AbstractModule;

import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.ClientService;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;

public class TestClientProvider implements IClientProvider {
	@Inject
	private ClientService clientService;
	
	public TestClientProvider(){
	}
	
	@Override
	public String getImplementation() {
		return "testclient";
	}

	@Override
	public List<AbstractModule> getGuiceModules() {
		return null;
	}

	@Override
	public List<Class<?>> getInjectedClasses() {
		return null;
	}

	@Override
	public void start() throws Exception {
		clientService.start();
		Thread.sleep(5000);

		LookupRequest request=new LookupRequest();
		request.setClientInfo(clientService.getInfo());
		request.setServiceName("test");
		LookupResult result=clientService.getLookupService().searchAgent(request);
		if (result.getAgentInfo()==null){
			System.out.println("Allocation worked!!!!!!!!!! no agent found!");
		
		}else{
			IAgentService agentService=clientService.getAgentService(result.getAgentInfo());
			System.out.println("Allocation worked!!!!!!!!!! Response= "+result.getAgentInfo());
			System.out.println("Allocation worked!!!!!!!!!! Response= "+agentService.getInfo());
		}
			
		List<IRegistryService> registries=clientService.getRegistries();
		for (IRegistryService reg : registries){
			System.out.println("REgistry found=="+reg);
		}
	}

	@Override
	public void stop() throws Exception {
		clientService.stop();
	}
}
