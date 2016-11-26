package microsofia.framework.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.client.AbstractClientService;
import microsofia.framework.map.Map;

@Singleton
@Server("fwk")
public class AgentService extends AbstractClientService<AgentInfo> implements IAgentService{
	private static Log log=LogFactory.getLog(AgentService.class);
	@Inject
	protected AgentConfiguration agentConfiguration;
	@Inject
	@Named(KEY_AGENTS)
	protected Map<Long,AgentInfo> agents;
	protected Object agent;
	
	public AgentService(){
	}
	
	@Override
	public AgentConfiguration getClientConfiguration(){
		return agentConfiguration;
	}
	
	public AgentLookupConfiguration getLookupConfiguration(){
		return agentConfiguration.getLookupConfiguration();
	}

	@Override
	public AgentInfo getInfo(){
		return serviceInfo;
	}
	
	@Override
	protected AgentInfo createServiceInfo() {
		return new AgentInfo();
	}
	
	public void setAgent(Object agent){
		this.agent=agent;
	}
	
	@Override
	public Object getAgent(){
		return agent;
	}
	
	public <A> A getAgent(Class<A> ca){
		return ca.cast(agent);
	}
	
	protected void internalStart() throws Exception{
		serviceInfo.setName(agentConfiguration.getName());
		serviceInfo.setGroup(agentConfiguration.getGroup());
		serviceInfo.setLookupConfiguration(agentConfiguration.getLookupConfiguration());
		
		List<AgentInfo> others=agents.values(AgentFilters.byNameAndGroup(agentConfiguration.getName(),agentConfiguration.getGroup())).get();
		if (others.size()>0){
			//for multiplicity one, we make sure that there is only one agentservice running
			if (agentConfiguration.getLookupConfiguration().getMultiplicity().equals(AgentLookupConfiguration.Multiplicity.one)){
				if (others.size()>0){
					throw new IllegalStateException("Cannot start the agent with multipliciy 'one' while other agents having the same queue are running: "+others);
				}
			}

			//for agent having the same host/port we remove it. Is it necessary?
			List<CompletableFuture<AgentInfo>> futures=new ArrayList<>();
			others.forEach(it->{
				if (it.getObjectAddress().equals(serviceInfo.getObjectAddress())){
					futures.add(agents.remove(it.getPid()));
				}
			});
			
			futures.forEach(it->{
				try{
					it.get();
				}catch(Exception e){
					log.debug(e,e);
				}
			});
		}
		
		agents.put(serviceInfo.getPid(),serviceInfo).get();
		
		log.info("Agent"+serviceInfo+" ready...");
		System.out.println("Agent"+serviceInfo+" ready...");
	}
	
	@Override
	public void internalStop(){
		agents.remove(serviceInfo.getPid());
		log.info("Agent stopped.");
	}	
}
