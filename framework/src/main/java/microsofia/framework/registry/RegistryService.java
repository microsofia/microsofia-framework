package microsofia.framework.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.group.DistributedGroup;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.property.Property;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.map.Map;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class RegistryService extends Service implements IRegistryService, InvokerServiceAdapter.IStartable,InvokerServiceAdapter.IStoppable,IAllocationService{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;
	protected AtomixReplica atomixReplica;
	protected DistributedGroup group;
	protected Map<ServiceAddress, ServiceAddress> registries;
	protected Map<ServiceAddress, ServiceAddress> agents;
	protected Map<ServiceAddress, ServiceAddress> clients;
	protected Invoker invoker;
	protected InvokerServiceAdapter invokerService;

	public RegistryService(){
		invokerService=new InvokerServiceAdapter(this);
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	@Override
	public void start() {
		System.out.println("Start allocating!!!");
	}
	
	@Override
	public void stop() {
		System.out.println("Stop allocating!!!");
	}
	
	@Override
	public ServiceAddress allocate() throws Exception{
		System.out.println("Allocation arrived to requester");
		ServiceAddress result=null;
		System.out.println("before agents call");
		System.out.println("size=="+agents.size().get());
		Collection<ServiceAddress> ags=agents.values().get();
		System.out.println("after agents call "+ags);
		for (ServiceAddress sa : ags){
			result=sa;
			break;
		}
		return result;
	}

	public void init() throws Exception{
		initAddress();
		
		List<Address> adr=new ArrayList<>();
		for (RegistryConfiguration.Address a : registryConfiguration.getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}
		
		@SuppressWarnings("unchecked")
		AtomixReplica.Builder builder=AtomixReplica.builder(new Address("localhost",registryConfiguration.getPort()))
												   .withStorage(new Storage("logs/"+registryConfiguration.getPort()))
												   .withResourceTypes((Class)Map.class,Invoker.class);
		atomixReplica=builder.build();
		atomixReplica.serializer().register(ServiceAddress.class,1986);
		atomixReplica.bootstrap(adr).join();

		registries=atomixReplica.getResource("registries",Map.class).get();
		agents=atomixReplica.getResource("agents",Map.class).get();
		clients=atomixReplica.getResource("clients",Map.class).get();

		registries.put(serviceAddress,serviceAddress).get();
		registries.get(serviceAddress).get();
		
		group=atomixReplica.getGroup("group").get();
		invoker=atomixReplica.getResource("invoker",Invoker.class).get();
		
		group.join(""+registryConfiguration.getPort()).get();
		group.election().onElection(term -> {
			if (term.leader().id().equals(""+registryConfiguration.getPort())){
				System.out.println("leader =="+registryConfiguration.getPort()+" this=="+this);
				invoker.setInvokerService(invokerService);
			}
		});
				
		System.out.println("Registry started...");
	}
}
