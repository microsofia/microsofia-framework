package microsofia.framework.agent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.property.Property;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.typology.Typology;
import microsofia.framework.service.Service;

@Server("fwk")
public class AgentService extends Service implements IAgentService{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;	
	protected AtomixClient atomixClient;
	protected Typology typology;
	
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
		
		atomixClient=AtomixClient.builder().build();
		atomixClient.connect(adr).get();

		typology=atomixClient.getResource("typology", Typology.class).get();
		typology.addAgent(serviceAddress);

		System.out.println("Agent started...");
	}
}