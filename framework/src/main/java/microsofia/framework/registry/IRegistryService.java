package microsofia.framework.registry;

import java.util.List;

import microsofia.container.module.endpoint.Id;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.IClientService;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.service.IService;

@Id
@Server
public interface IRegistryService extends IService{
	
	@Override
	public RegistryInfo getInfo() throws Exception;

	public List<IAgentService> getAgents() throws Exception;
	
	public List<IClientService> getClients() throws Exception;

	public List<IRegistryService> getRegistries() throws Exception;

	public ILookupService getLookupService() throws Exception;
}
