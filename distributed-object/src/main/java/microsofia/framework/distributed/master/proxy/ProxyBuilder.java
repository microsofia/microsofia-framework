package microsofia.framework.distributed.master.proxy;

import java.lang.reflect.Proxy;
import javax.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.container.Container;
import microsofia.framework.client.ClientService;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.IObjectAllocator;

public class ProxyBuilder {
	private static Log log=LogFactory.getLog(ProxyBuilder.class); 
	@Inject
	private ClientService clientService;
	@Inject
	private Container container;
	private IMaster master;
	private IObjectAllocator objectAllocator;
	private int priority;
	private int weigth;
	private long pollingPeriod;
	
	public ProxyBuilder(){
		priority=100;
		weigth=1;
		pollingPeriod=3000;
	}
	
	public IMaster getMaster(){
		return master;
	}
	
	public ClientService getClientService() {
		return clientService;
	}

	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getWeigth() {
		return weigth;
	}

	public void setWeigth(int weigth) {
		this.weigth = weigth;
	}
	
	public long getPollingPeriod(){
		return pollingPeriod;
	}
	
	public void setPollingPeriod(long l){
		pollingPeriod=l;
	}

	public void connect(String name,String group) throws Exception{
		master=clientService.getClientLookupService().searchAgent(IMaster.class, name,group);
		objectAllocator=master.getObjectAllocator();
	}
	
	public void disconnect(){
		try{
			clientService.getClientLookupService().freeAgent(master);
		}catch(Exception e){
			log.debug(e,e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> c){
		InvocationJobHandler invocationJobHandler=new InvocationJobHandler(this,c,objectAllocator);
		container.injectMembers(invocationJobHandler);
		return (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{c}, invocationJobHandler);
	}
}
