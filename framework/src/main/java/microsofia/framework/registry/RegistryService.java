package microsofia.framework.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.group.DistributedGroup;
import microsofia.container.application.PropertyConfig;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.FrameworkException;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.IClient;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.LookupService;
import microsofia.framework.service.Service;
import microsofia.framework.service.ServiceAddress;

@Server("fwk")
public class RegistryService extends Service implements IRegistryService{
	private static final Log log=LogFactory.getLog(RegistryService.class);
	@Inject
	private RegistryConfiguration registryConfiguration;
	protected AtomixReplica atomixReplica;
	protected DistributedGroup group;
	protected Map<ServiceAddress, ServiceAddress> registries;
	protected Map<ServiceAddress, ServiceAddress> agents;
	protected Map<ServiceAddress, ServiceAddress> clients;
	protected Invoker invoker;
	protected LookupService lookupService;
	protected InvokerServiceAdapter invokerServiceAdapter;

	public RegistryService(){
		lookupService=new LookupService();
		invokerServiceAdapter=new InvokerServiceAdapter(lookupService);
	}
	
	@Override
	public List<IAgentService> getAgents() throws Exception{
		return getProxies(IAgentService.class, agents);
	}
	
	@Override
	public List<IClient> getClients() throws Exception{
		return getProxies(IClient.class, clients);
	}

	@Override
	public List<IRegistryService> getRegistries() throws Exception{
		return getProxies(IRegistryService.class, registries);
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}

	@Override
	public void start(){
		try{
			export();
			init();
			
			List<Address> adr=new ArrayList<>();
			for (RegistryConfiguration.Member a : registryConfiguration.getMember()){
				adr.add(new Address(a.getHost(), a.getPort()));
			}
	
			Properties properties=PropertyConfig.toPoperties(registryConfiguration.getProperties());
			@SuppressWarnings("unchecked")
			AtomixReplica.Builder builder=AtomixReplica.builder(new Address((registryConfiguration.getHost()!=null ? registryConfiguration.getHost() : "localhost"),registryConfiguration.getPort()),properties)
													   .withStorage(new Storage("logs/"+registryConfiguration.getPort()))
													   .withResourceTypes((Class)Map.class,Invoker.class);
			atomixReplica=builder.build();
			atomixReplica.serializer().register(ServiceAddress.class,1986);
			atomixReplica.bootstrap(adr).join();
	
			registries=atomixReplica.getResource("registries",Map.class).get();//TODO put all strings in static final
			agents=atomixReplica.getResource("agents",Map.class).get();
			clients=atomixReplica.getResource("clients",Map.class).get();
	
			registries.put(serviceAddress,serviceAddress).get();
			registries.get(serviceAddress).get();
			
			group=atomixReplica.getGroup("group").get();
			invoker=atomixReplica.getResource("invoker",Invoker.class).get();
			
			lookupService.setAgents(agents);
			
			group.join(""+registryConfiguration.getPort()).get();
			group.election().onElection(term -> {
				if (term.leader().id().equals(""+registryConfiguration.getPort())){
					invoker.setInvokerService(invokerServiceAdapter);
				}
			});
			
			log.info("Registry ready...");
		}catch(Throwable th){
			throw new FrameworkException(th.getMessage(), th);
		}
	}
	
	@Override
	public void stop(){
		unexport();
		log.info("Registry stopped.");
	}
}
