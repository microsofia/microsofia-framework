package microsofia.framework.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.atomix.Atomix;
import io.atomix.variables.DistributedLong;
import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.IServer;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.container.module.endpoint.msofiarmi.MSofiaRMIServer;
import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.ClientInfo;
import microsofia.framework.client.IClientService;
import microsofia.framework.invoker.Invoker;
import microsofia.framework.map.Map;
import microsofia.framework.registry.IRegistryService;
import microsofia.framework.registry.RegistryInfo;

@Server("fwk")
public abstract class AbstractService<A extends Atomix,SI extends ServiceInfo> implements IService{
	private static Log log=LogFactory.getLog(AbstractService.class);
	public static final String KEY_REGISTRIES 		= "registries";
	public static final String KEY_AGENTS 	  		= "agents";
	public static final String KEY_CLIENTS 	  		= "clients";
	public static final String KEY_INVOKER 	  		= "invoker";
	public static final String KEY_INVOKER_GROUP	= "group";
	public static final String KEY_LOOKUP_RESULT	= "lookupResult";
	public static final String KEY_SERVICE_ID		= "serviceId";
	public static final String KEY_LOOKUP_ID		= "lookupId";
	public static final String KEY_LOOKUP_SERVICE	= "lookupService";
	@Inject
	@Server("fwk")
	protected IServer server;
	protected SI serviceInfo;
	@Inject
	@Named(KEY_SERVICE_ID)
	protected DistributedLong serviceId;
	@Inject
	@Named(KEY_REGISTRIES)
	protected Map<Long, RegistryInfo> registries;
	@Inject
	@Named(KEY_CLIENTS)
	protected Map<Long, ClientInfo> clients;
	@Inject
	protected ExecutorService executorService;
	@Inject
	@Named(KEY_INVOKER)
	protected Invoker invoker;
	
	public AbstractService(){
	}
	
	public abstract void start();
	
	public void stop(){
		executorService.shutdown();
	}
	
	protected abstract SI createServiceInfo();

	protected abstract A getAtomix();
	
	protected void configureService() throws Exception{
		invoker.setExecutorService(executorService);

		serviceInfo=createServiceInfo();
		serviceInfo.setObjectAddress(((MSofiaRMIServer)server).getLocalServer().getObjectAddress(this));
		serviceInfo.setInetAddress();
		serviceInfo.setNPid();
		serviceInfo.setStartDate();		
		serviceInfo.setPid(serviceId.incrementAndGet().get());
	}
	
	@Override
	public SI getInfo(){
		return serviceInfo;
	}
	
	@Override
	public void ping() {
	}
	
	@Override
	public java.util.Map<String,String> getSystemProperties(){
		Properties properties = System.getProperties();
		java.util.Map<String, String> map = new HashMap<String, String>();
		properties.entrySet().forEach(it->{
			map.put((String)it.getKey(), ""+it.getValue());
		});
        
        return map;
	}
	
	@Override
	public void setSystemProperty(String name,String value){
		System.setProperty(name, value);
	}
	
	@Override
	public java.util.Map<String,String> getEnvProperties(){
		return new HashMap<String, String>(System.getenv());
	}

	@Export
	public void export(){
		log.info("Service "+serviceInfo+" exported.");
	}
	
	@Unexport
	public void unexport(){
		log.info("Service "+serviceInfo+" unexported.");
	}

	protected <T> List<T> getProxies(Class<T> c,Collection<? extends ServiceInfo> sis) throws Exception{
		List<T> proxies=new ArrayList<>();
		for (ServiceInfo si : sis){
			proxies.add(getProxy(c, si));
		}
		return proxies;
	}
	
	public List<IRegistryService> getRegistries() throws Exception{
		return getProxies(IRegistryService.class, registries.values().get());
	}
	
	public List<IClientService> getClients() throws Exception{
		return getProxies(IClientService.class, clients.values().get());
	}
	
	public <T> T getProxy(Class<T> c,ServiceInfo si){
		return ((MSofiaRMIServer)server).getLocalServer().lookup(si.getObjectAddress().getServerAddress(), c);
	}

	public IAgentService getAgentService(ServiceInfo sa){
		return getProxy(IAgentService.class, sa);
	}

	public IClientService getClient(ServiceInfo sa){
		return getProxy(IClientService.class, sa);
	}

	public IRegistryService getRegistryService(ServiceInfo sa){
		return getProxy(IRegistryService.class, sa);
	}
}
