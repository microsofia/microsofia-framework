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
import io.atomix.variables.DistributedLong;
import microsofia.container.application.PropertyConfig;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.FrameworkException;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.registry.lookup.LookupResult;
import microsofia.framework.registry.lookup.LookupService;
import microsofia.framework.service.Service;

@Server("fwk")
public class RegistryService extends Service<AtomixReplica,RegistryInfo> implements IRegistryService{
	private static final Log log=LogFactory.getLog(RegistryService.class);
	@Inject
	protected RegistryConfiguration registryConfiguration;
	protected DistributedLong globalLookupId;
	protected DistributedGroup group;
	protected Map<Long, AgentInfo> agents;
	protected Map<Long,LookupResult> lookupResults;
	protected LookupService lookupService;
	protected InvokerServiceAdapter invokerServiceAdapter;

	public RegistryService(){
		lookupService=new LookupService();
		invokerServiceAdapter=new InvokerServiceAdapter(lookupService);
	}
	
	@Override
	public ILookupService getLookupService(){
		return lookupService;
	}
	
	@Override
	public List<IAgentService> getAgents() throws Exception{
		return getProxies(IAgentService.class, agents.values().get());
	}
	
	public RegistryConfiguration getRegistryConfiguration() {
		return registryConfiguration;
	}

	public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
		this.registryConfiguration = registryConfiguration;
	}
	
	@Override
	public RegistryInfo getInfo(){
		return serviceInfo;
	}
	
	@Override
	protected RegistryInfo createServiceInfo() {
		return new RegistryInfo();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void start(){
		try{
			export();
			
			List<Address> adr=new ArrayList<>();
			for (RegistryConfiguration.Member a : registryConfiguration.getMember()){
				adr.add(new Address(a.getHost(), a.getPort()));
			}

			String localHost=(registryConfiguration.getHost()!=null ? registryConfiguration.getHost() : "localhost");
			String id=localHost+"/"+registryConfiguration.getPort();
			
			Properties properties=PropertyConfig.toPoperties(registryConfiguration.getProperties());
			AtomixReplica.Builder builder=AtomixReplica.builder(new Address(localHost,registryConfiguration.getPort()),properties)
													   .withStorage(new Storage("logs/"+id))
													   .withResourceTypes((Class)Map.class,Invoker.class);
			atomix=builder.build();
			configureSerializer();
			atomix.bootstrap(adr).join();
			configureResources();
			
			globalLookupId=atomix.getLong(KEY_LOOKUP_ID).get();
			agents=atomix.getResource(KEY_AGENTS,Map.class).get();
			group=atomix.getGroup(KEY_INVOKER_GROUP).get();
			lookupResults=atomix.getResource(KEY_LOOKUP_RESULT,Map.class).get();

			registries.put(serviceInfo.getPid(),serviceInfo).get();
			
			lookupService.setExecutorService(executorService);
			lookupService.setAgents(agents);
			lookupService.setClients(clients);
			lookupService.setLookupResultId(globalLookupId);
			lookupService.setLookupResults(lookupResults);
			
			
			group.join(id).get();
			group.election().onElection(term -> {
				if (term.leader().id().equals(id)){
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
		try{
			registries.remove(serviceInfo.getPid()).get();
		}catch(Throwable th){
			log.debug(th.getMessage(), th);
		}
		try{
			atomix.shutdown().get();
		}catch(Throwable th){
			log.debug(th.getMessage(), th);
		}
		try{			
			unexport();
		}catch(Throwable th){
			log.debug(th.getMessage(), th);
		}
		super.stop();
		log.info("Registry stopped.");
	}
}
