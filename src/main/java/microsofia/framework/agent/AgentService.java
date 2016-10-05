package microsofia.framework.agent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.module.endpoint.IServer;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.msofiarmi.MSofiaRMIServer;
import microsofia.container.module.property.Property;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.atomix.ServicesAddress;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class AgentService extends Service implements IAgentService{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;
	@Inject
	@Server("fwk")
	private IServer server;
	protected AtomixClient atomixClient;
	protected ServicesAddress addresses;
	
	public AgentService(){
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	public void init() throws Exception{
		List<Address> adr=new ArrayList<>();
		for (RegistryConfiguration.Address a : getRegistryConfiguration().getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}
		
		atomixClient=AtomixClient.builder().build();
		atomixClient.connect(adr).get();

		addresses=atomixClient.getResource("agents", ServicesAddress.class).get();

		ServiceAddress serviceAddress=new ServiceAddress();
		serviceAddress.setObjectAddress(((MSofiaRMIServer)server).getLocalServer().getObjectAddress(this));
		addresses.add(serviceAddress);
				
		System.out.println("Agent started...");
	}
}
