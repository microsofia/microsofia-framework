package microsofia.framework.client;

import microsofia.container.module.endpoint.Id;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.service.IService;

@Id
@Server
public interface IClientService extends IService{

	public Object getClient() throws Exception;
	
	@Override
	public ClientInfo getInfo() throws Exception;
}
