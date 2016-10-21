package microsofia.framework.agent;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.service.IService;

@Server
public interface IAgentService extends IService{
	
	public Object getAgent() throws Exception;
	
	public <A> A getAgent(Class<A> ca) throws Exception;
	
	@Override
	public AgentInfo getInfo() throws Exception;
}
