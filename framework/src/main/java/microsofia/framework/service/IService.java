package microsofia.framework.service;

import microsofia.container.module.endpoint.Server;

@Server
public interface IService {
	
	public ServiceInfo getServiceInfo();
	
	public void ping();
}
