package microsofia.framework.client;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.property.Property;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.IAllocationService;
import microsofia.framework.registry.RegistryConfiguration;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class Client extends Service implements IClient{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;	
	protected AtomixClient atomixClient;
	protected Map<ServiceAddress,ServiceAddress> clients;
	protected Invoker invoker;
	protected IAllocationService allocationService;
	
	public Client(){
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	@SuppressWarnings("unchecked")
	public void connect() throws Exception{
		initAddress();
		
		List<Address> adr=new ArrayList<>();
		for (RegistryConfiguration.Address a : getRegistryConfiguration().getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}
		
		atomixClient=AtomixClient.builder().withResourceTypes((Class)Map.class,Invoker.class).build();
		atomixClient.serializer().register(ServiceAddress.class,1986);
		atomixClient.connect(adr).get();

		clients=atomixClient.getResource("clients", Map.class).get();
		invoker=atomixClient.getResource("invoker", Invoker.class).get();
		allocationService=invoker.getProxy(IAllocationService.class);

		clients.put(serviceAddress,serviceAddress).get();
		clients.get(serviceAddress).get();

		System.out.println("Client connected...");
		
		Thread.sleep(5000);
		ServiceAddress sa=allocationService.allocate();
		System.out.println("Allocation worked!!!!!!!!!! Response= "+sa);

	}
}
