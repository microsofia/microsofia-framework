package microsofia.framework.agent;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.service.IService;

@Server
public interface IAgentService extends IService{
	
	@Override
	public AgentInfo getInfo() throws Exception;
}
