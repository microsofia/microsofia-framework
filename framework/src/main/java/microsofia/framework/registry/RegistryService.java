package microsofia.framework.registry;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.atomix.AtomixReplica;
import io.atomix.group.DistributedGroup;
import io.atomix.variables.DistributedLong;
import microsofia.container.module.atomix.AtomixConfig;
import microsofia.container.module.atomix.Cluster;
import microsofia.container.module.atomix.ClusterConfiguration;
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
import microsofia.framework.service.AbstractService;
import microsofia.framework.service.AtomixConfigurator;

@Singleton
@Server("fwk")
public class RegistryService extends AbstractService<AtomixReplica,RegistryInfo> implements IRegistryService{
	private static final Log log=LogFactory.getLog(RegistryService.class);
	@Inject
	@ClusterConfiguration(configurator={AtomixConfigurator.class},resources={Map.NonAnnotatedMap.class,Invoker.class})
	@Cluster("registry")
	protected AtomixReplica atomix;
	@Inject
	@Cluster("registry")
	protected AtomixConfig atomixConfig;
 	protected DistributedLong globalLookupId;
	protected DistributedGroup group;
	protected Map<Long, AgentInfo> agents;
	protected Map<Long,LookupResult> lookupResults;
	@Inject
	protected LookupService lookupService;
	@Inject
	protected InvokerServiceAdapter invokerServiceAdapter;

	public RegistryService(){
	}
	
	@Override
	public AtomixReplica getAtomix(){
		return atomix;
	}
	
	@Override
	public ILookupService getLookupService(){
		return lookupService;
	}
	
	@Override
	public List<IAgentService> getAgents() throws Exception{
		return getProxies(IAgentService.class, agents.values().get());
	}
	
	@Override
	public RegistryInfo getInfo(){
		return serviceInfo;
	}
	
	@Override
	protected RegistryInfo createServiceInfo() {
		return new RegistryInfo();
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void start(){
		try{
			export();
			
			String localHost=(atomixConfig.getLocalMember().getHost()!=null ? atomixConfig.getLocalMember().getHost() : "localhost");
			String id=localHost+"/"+atomixConfig.getLocalMember().getPort();
			
			configureResources();
			
			globalLookupId=atomix.getLong(KEY_LOOKUP_ID).get();
			agents=atomix.getResource(KEY_AGENTS,Map.class).get();
			group=atomix.getGroup(KEY_INVOKER_GROUP).get();
			lookupResults=atomix.getResource(KEY_LOOKUP_RESULT,Map.class).get();

			registries.put(serviceInfo.getPid(),serviceInfo).get();
			
			lookupService.setAgents(agents);
			lookupService.setClients(clients);
			lookupService.setLookupResultId(globalLookupId);
			lookupService.setLookupResults(lookupResults);
			invokerServiceAdapter.setService(lookupService);
			
			group.join(id).get();
			group.election().onElection(term -> {
				if (term.leader().id().equals(id)){
					invoker.setInvokerService(invokerServiceAdapter);
				}
			});
			
			log.info("Registry ready...");
		}catch(Throwable th){
			th.printStackTrace();
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
			unexport();
		}catch(Throwable th){
			log.debug(th.getMessage(), th);
		}
		super.stop();
		log.info("Registry stopped.");
	}
}
