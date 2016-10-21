package microsofia.framework.client.lookup;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import microsofia.framework.agent.IAgentService;
import microsofia.framework.client.AbstractClientService;
import microsofia.framework.registry.lookup.ILookupService;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;

@Singleton
public class ClientLookupService implements IClientLookupService{
	private static Log log=LogFactory.getLog(ClientLookupService.class);
	private ILookupService lookupService;
	private AbstractClientService<?> abstractClientService;
	private Map<Object,LookupResult> results;
	
	public ClientLookupService(){
		results=Collections.synchronizedMap(new IdentityHashMap<>());
	}
	
	public ILookupService getLookupService() {
		return lookupService;
	}

	public void setLookupService(ILookupService lookupService) {
		this.lookupService = lookupService;
	}

	public AbstractClientService<?> getAbstractClientService() {
		return abstractClientService;
	}

	public void setAbstractClientService(AbstractClientService<?> abstractClientService) {
		this.abstractClientService = abstractClientService;
	}

	@Override
	public Object searchAgent(String queue) throws Exception{
		return searchAgent(queue,1);
	}
	
	@Override
	public <A> A searchAgent(Class<A> ca,String queue) throws Exception{
		return searchAgent(ca,queue,1);
	}
	
	@Override
	public <A> A searchAgent(Class<A> ca,String queue,int weigth) throws Exception{
		Object proxy=searchAgent(queue,weigth);
		if (proxy!=null){
			return ca.cast(proxy);
		}
		return null;
	}
	
	public Object searchAgent(String queue,int weigth) throws Exception{
		LookupRequest request=new LookupRequest();
		request.setClientInfo(getAbstractClientService().getInfo());
		request.setQueue(queue);
		request.setWeight(weigth);
		LookupResult result=getAbstractClientService().getLookupService().searchAgent(request);
		if (result.getAgentInfo()!=null){
			IAgentService agentService=getAbstractClientService().getAgentService(result.getAgentInfo());
			Object proxy=agentService.getAgent();
			results.put(proxy,result);
			return proxy;
		}
		return null;
	}
	
	@Override
	public void freeAgent(Object proxy) throws Exception{
		LookupResult result=results.remove(proxy);
		if (result!=null){
			getLookupService().freeAgent(result.getId());
		}
	}
	
	public void close(){
		List<Long> ids=results.values().stream().map(LookupResult::getId).collect(Collectors.toList());
		try{
			getLookupService().freeAgent(ids);
		}catch(Exception e){
			log.error(e,e);
		}
	}
}
