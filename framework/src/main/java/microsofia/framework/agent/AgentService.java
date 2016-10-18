package microsofia.framework.agent;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.client.AbstractClient;
import microsofia.framework.map.Map;

//TODO load the implementation of the agent
@Server("fwk")
public class AgentService extends AbstractClient<AgentInfo> implements IAgentService{
	private static Log log=LogFactory.getLog(AgentService.class);
	@Inject
	protected AgentConfiguration agentConfiguration;
	protected Map<Long,AgentInfo> agents;
	
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
	
	@SuppressWarnings("unchecked")
	protected void internalStart() throws Exception{
		agents=atomix.getResource(KEY_AGENTS, Map.class).get();

		serviceInfo.setServiceName(agentConfiguration.getServiceName());
		serviceInfo.setLookupConfiguration(agentConfiguration.getLookupConfiguration());
		/*TODO check agent config:
				->multiplicity coherent if 'one' used
				->if there is a previous one with the same object@ then remove it from map ???
		 */
		agents.put(serviceInfo.getPid(),serviceInfo).get();
		
		log.info("Agent ready...");
	}
	
	@Override
	public void internalStop(){
		agents.remove(serviceInfo.getPid());
		log.info("Agent stopped.");
	}	
}
