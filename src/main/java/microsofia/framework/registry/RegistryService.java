package microsofia.framework.registry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;
import microsofia.container.module.endpoint.IServer;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.msofiarmi.MSofiaRMIServer;
import microsofia.container.module.property.Property;
import microsofia.framework.registry.atomix.ServicesAddress;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class RegistryService extends Service implements IRegistryService{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;
	@Inject
	@Server("fwk")
	private IServer server;
	protected AtomixReplica atomixReplica;
	protected ServicesAddress addresses;

	public RegistryService(){
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	public void init() throws Exception{
		List<Address> adr=new ArrayList<>();
		for (RegistryConfiguration.Address a : registryConfiguration.getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}

		@SuppressWarnings("unchecked")
		AtomixReplica.Builder builder=AtomixReplica.builder(new Address("localhost",registryConfiguration.getPort()))
												   .withStorage(new Storage("logs/"+registryConfiguration.getPort()))
												   .withResourceTypes(ServicesAddress.class);
		atomixReplica=builder.build();
		

		atomixReplica.bootstrap(adr).join();
		
		addresses=atomixReplica.getResource("registry",ServicesAddress.class).get();
		ServiceAddress serviceAddress=new ServiceAddress();
		serviceAddress.setObjectAddress(((MSofiaRMIServer)server).getLocalServer().getObjectAddress(this));
		addresses.add(serviceAddress);
		
		System.out.println("Registry started...");
	}
}
