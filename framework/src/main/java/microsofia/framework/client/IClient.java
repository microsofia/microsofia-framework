package microsofia.framework.client;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.service.IService;

@Server
public interface IClient extends IService{

	@Override
	public ClientInfo getInfo() throws Exception;
}
