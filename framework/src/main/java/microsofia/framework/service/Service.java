package microsofia.framework.service;

import javax.inject.Inject;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.IServer;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.msofiarmi.MSofiaRMIServer;

@Server("fwk")
public class Service implements IService{
	@Inject
	@Server("fwk")
	protected IServer server;
	protected ServiceAddress serviceAddress;
	
	public Service(){
	}

	protected void initAddress(){//TODO use @PostConstruct?
		serviceAddress=new ServiceAddress();
		serviceAddress.setObjectAddress(((MSofiaRMIServer)server).getLocalServer().getObjectAddress(this));
	}
	
	@Override
	public ServiceAddress getServiceAddress(){
		return serviceAddress;
	}
	
	@Override
	public void ping() {
	}

	@Export
	public void export(){
		System.out.println("Service exported.");
	}
}
