package microsofia.framework.registry;

import java.util.List;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.IClient;
import microsofia.framework.service.IService;

@Server
public interface IRegistryService extends IService{

	public List<IAgentService> getAgents() throws Exception;
	
	public List<IClient> getClients() throws Exception;

	public List<IRegistryService> getRegistries() throws Exception;
}
