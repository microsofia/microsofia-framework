package microsofia.framework.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.group.DistributedGroup;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.property.Property;
import microsofia.framework.registry.allocator.AllocationRequest;
import microsofia.framework.registry.allocator.AllocationResponse;
import microsofia.framework.registry.allocator.Allocator;
import microsofia.framework.registry.allocator.IAllocatorLifecycle;
import microsofia.framework.registry.typology.Typology;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class RegistryService extends Service implements IRegistryService{
	@Inject
	@Property("registry")
	private RegistryConfiguration registryConfiguration;
	protected AtomixReplica atomixReplica;
	protected DistributedGroup group;
	protected Typology typology;
	protected Allocator allocator;

	public RegistryService(){
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
		for (RegistryConfiguration.Address a : registryConfiguration.getAddress()){
			adr.add(new Address(a.getHost(), a.getPort()));
		}

		@SuppressWarnings("unchecked")
		AtomixReplica.Builder builder=AtomixReplica.builder(new Address("localhost",registryConfiguration.getPort()))
												   .withStorage(new Storage("logs/"+registryConfiguration.getPort()))
												   .withResourceTypes(Typology.class,Allocator.class);
		atomixReplica=builder.build();
		
		atomixReplica.bootstrap(adr).join();

		group=atomixReplica.getGroup("group").get();		
		typology=atomixReplica.getResource("typology",Typology.class).get();
		allocator=atomixReplica.getResource("allocator",Allocator.class).get();
		
		typology.addRegistry(serviceAddress);
				
		group.join(""+registryConfiguration.getPort()).get();
		group.election().onElection(term -> {
			if (term.leader().id().equals(""+registryConfiguration.getPort())){
				System.out.println("leader =="+registryConfiguration.getPort()+" this=="+this);
				allocator.setAllocatorLifecyle(new IAllocatorLifecycle() {
					
					@Override
					public void stopAllocating() {
						System.out.println("Stop allocating!!!");
					}
					
					@Override
					public void startAllocating() {
						System.out.println("Start allocating!!!");
					}
					
					@Override
					public AllocationResponse allocate(AllocationRequest request) {
						ServiceAddress result=null;
						AllocationResponse response=new AllocationResponse(request);
						try {
							List<ServiceAddress> agents=typology.getAgents().get();
							for (ServiceAddress sa : agents){
								result=sa;
								break;
							}
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						response.setServiceAddress(result);
						return response;
					}
				});
			}
		});
				
		System.out.println("Registry started...");
	}
}
