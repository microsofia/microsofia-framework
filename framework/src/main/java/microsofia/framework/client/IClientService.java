package microsofia.framework.client;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.service.IService;

@Server
public interface IClientService extends IService{

	@Override
	public ClientInfo getInfo() throws Exception;
}
