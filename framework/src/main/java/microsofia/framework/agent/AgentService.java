package microsofia.framework.agent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.property.Property;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class AgentService extends Service implements IAgentService{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;	
	protected AtomixClient atomixClient;
	protected Map<ServiceAddress, ServiceAddress> agents;
	
	public AgentService(){
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	public void init() throws Exception{
		initAddress();
		
		List<Address> adr=new ArrayList<>();
		for (RegistryConfiguration.Address a : getRegistryConfiguration().getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}

		atomixClient=AtomixClient.builder().withResourceTypes((Class)Map.class,Invoker.class).build();
		atomixClient.serializer().register(ServiceAddress.class,1986);
		atomixClient.connect(adr).get();

		agents=atomixClient.getResource("agents", Map.class).get();
		agents.put(serviceAddress,serviceAddress).get();
		agents.get(serviceAddress).get();

		System.out.println("Agent started...");
	}
}
