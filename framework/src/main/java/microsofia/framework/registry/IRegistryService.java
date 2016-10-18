package microsofia.framework.registry;

import java.util.List;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.IClient;
import microsofia.framework.service.IService;

//TODO define ILookupService so that we can introspect whats happening in there
@Server
public interface IRegistryService extends IService{
	
	@Override
	public RegistryInfo getInfo() throws Exception;

	public List<IAgentService> getAgents() throws Exception;
	
	public List<IClient> getClients() throws Exception;

	public List<IRegistryService> getRegistries() throws Exception;
}
