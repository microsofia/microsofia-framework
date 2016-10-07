package microsofia.framework.client;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jetty.util.TopologicalSort;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.property.Property;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.registry.allocator.AllocationRequest;
import microsofia.framework.registry.allocator.AllocationResponse;
import microsofia.framework.registry.allocator.Allocator;
import microsofia.framework.registry.typology.Typology;
import microsofia.framework.service.Service;

@Server("fwk")
public class Client extends Service implements IClient{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;	
	protected AtomixClient atomixClient;
	protected Typology typology;
	protected Allocator allocator;
	
	public Client(){
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	public void connect() throws Exception{
		initAddress();
		
		List<Address> adr=new ArrayList<>();
		for (RegistryConfiguration.Address a : getRegistryConfiguration().getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}
		
		atomixClient=AtomixClient.builder().withResourceTypes(Typology.class,Allocator.class).build();
		atomixClient.connect(adr).get();

		typology=atomixClient.getResource("typology", Typology.class).get();
		allocator=atomixClient.getResource("allocator", Allocator.class).get();

		typology.addClient(serviceAddress);

		Thread.sleep(5000);
		AllocationRequest request=new AllocationRequest();
		AllocationResponse response=allocator.allocate(request).get();
		System.out.println("Allocation worked!!!!!!!!!! Response= "+response);

		System.out.println("Client connected...");
	}
}
