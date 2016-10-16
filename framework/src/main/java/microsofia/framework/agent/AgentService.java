package microsofia.framework.agent;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.client.AbstractClient;

//TODO load the implementation of the agent
@Server("fwk")
public class AgentService extends AbstractClient implements IAgentService{
	private static Log log=LogFactory.getLog(AgentService.class);
	@Inject
	protected AgentConfiguration agentConfiguration;	
	
	public AgentService(){
	}
	
	@Override
	public AgentConfiguration getClientConfiguration(){
		return agentConfiguration;
	}
	
	@Override
	protected String getServiceAddressMap() {
		return "agents";
	}
	
	@Override
	public void start(){
		super.start();
		log.info("Agent ready...");
	}
	
	@Override
	public void stop(){
		super.stop();
		log.info("Agent stopped.");
	}
}
