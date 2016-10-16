package microsofia.framework.registry.lookup;

import java.util.Collection;

import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.map.Map;
import microsofia.framework.service.ServiceAddress;

public class LookupService implements InvokerServiceAdapter.IStartable,InvokerServiceAdapter.IStoppable,ILookupService{
	protected Map<ServiceAddress, ServiceAddress> agents;
	
	public LookupService(){
	}
	
	public void setAgents(Map<ServiceAddress, ServiceAddress> agents){
		this.agents=agents;
	}
	
	@Override
	public void startInvocation() {
		System.out.println("Start allocating!!!");
	}
	
	@Override
	public void stopInvocation() {
		System.out.println("Stop allocating!!!");
	}
	
	//TODO review lookup criteria + save created lookup
	@Override
	public ServiceAddress searchAgent() throws Exception{
		System.out.println("Allocation arrived to requester");
		ServiceAddress result=null;
		System.out.println("before agents call");
		System.out.println("size=="+agents.size().get());
		Collection<ServiceAddress> ags=agents.values().get();
		System.out.println("after agents call "+ags);
		for (ServiceAddress sa : ags){
			result=sa;
			break;
		}
		return result;
	}
}
