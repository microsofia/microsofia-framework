package microsofia.framework.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.IServer;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.container.module.endpoint.msofiarmi.MSofiaRMIServer;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.IClient;
import microsofia.framework.map.Map;
import microsofia.framework.registry.IRegistryService;

@Server("fwk")
public abstract class Service implements IService{
	private static Log log=LogFactory.getLog(Service.class);
	@Inject
	@Server("fwk")
	protected IServer server;
	protected ServiceInfo serviceInfo;
	protected ServiceAddress serviceAddress;
	
	public Service(){
	}
	
	public abstract void start();
	
	public abstract void stop();
	
	protected void init() throws Exception{
		serviceAddress=new ServiceAddress();
		serviceAddress.setObjectAddress(((MSofiaRMIServer)server).getLocalServer().getObjectAddress(this));
		
		serviceInfo=new ServiceInfo();
		serviceInfo.setInetAddress();
		serviceInfo.setPid();
		serviceInfo.setStartDate();
	}
	
	@Override
	public ServiceInfo getServiceInfo(){
		return serviceInfo;
	}
	
	@Override
	public void ping() {
	}

	@Export
	public void export(){
		log.info("Service "+serviceInfo+" exported.");
	}
	
	@Unexport
	public void unexport(){
		log.info("Service "+serviceInfo+" unexported.");
	}

	protected <T> List<T> getProxies(Class<T> c,Map<ServiceAddress,ServiceAddress> map) throws Exception{
		List<T> proxies=new ArrayList<>();
		for (ServiceAddress sa : map.values().get()){
			proxies.add(getProxy(c, sa));
		}
		return proxies;
	}
	
	public <T> T getProxy(Class<T> c,ServiceAddress sa){
		return ((MSofiaRMIServer)server).getLocalServer().lookup(sa.getObjectAddress().getServerAddress(), c);
	}

	public IAgentService getAgentService(ServiceAddress sa){
		return getProxy(IAgentService.class, sa);
	}

	public IClient getClient(ServiceAddress sa){
		return getProxy(IClient.class, sa);
	}

	public IRegistryService getRegistryService(ServiceAddress sa){
		return getProxy(IRegistryService.class, sa);
	}
}
