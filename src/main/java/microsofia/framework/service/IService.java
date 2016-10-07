package microsofia.framework.service;

import microsofia.container.module.endpoint.Server;

@Server
public interface IService {
	
	public ServiceAddress getServiceAddress();
	
	public void ping();
}
