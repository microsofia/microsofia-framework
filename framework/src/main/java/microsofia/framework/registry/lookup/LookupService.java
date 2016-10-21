package microsofia.framework.registry.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.atomix.variables.DistributedLong;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.client.ClientInfo;
import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.strategy.CompositeStrategy;

@Singleton
public class LookupService implements InvokerServiceAdapter.IStartable,InvokerServiceAdapter.IStoppable,ILookupService{
	private static Log log=LogFactory.getLog(LookupService.class);
	protected AtomicBoolean canInvoke;
	@Inject
	protected LockFactory lockFactory;
	@Inject
	protected CompositeStrategy compositeStrategy;
	@Inject
	protected ExecutorService executorService;
	protected DistributedLong globalLookupId;
	protected Map<Long, AgentInfo> agents;
	protected Map<Long, ClientInfo> clients;
	protected Map<Long, LookupResult> lookupResults;

	public LookupService(){
		canInvoke=new AtomicBoolean(false);
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setLookupResultId(DistributedLong globalLookupId){
		this.globalLookupId=globalLookupId;
	}
	
	public void setAgents(Map<Long, AgentInfo> agents) throws InterruptedException, ExecutionException{
		this.agents=agents;
		compositeStrategy.setAgents(agents);
		agents.setMapListener(this::agentRemoved).get();
	}
	
	public void setClients(Map<Long, ClientInfo> clients) throws InterruptedException, ExecutionException{
		this.clients=clients;
		clients.setMapListener(this::clientRemoved).get();
	}
	
	public void setLookupResults(Map<Long, LookupResult> lookupResults){
		this.lookupResults=lookupResults;
		compositeStrategy.setLookupResults(lookupResults);
	}
		
	@Override
	public void startInvocation() {
		canInvoke.set(true);
	}

	@Override
	public void stopInvocation() {
		canInvoke.set(false);
	}
	
	public void agentRemoved(Long pid, AgentInfo agent){
		if (canInvoke.get()){
			executorService.submit(()->{
				try{
					List<LookupResult> toBeRemoved=lookupResults.values(LookupResultFilters.byAgentPid(agent.getPid())).get();
					for (LookupResult r : toBeRemoved){
						lookupResults.remove(r.getId());
					}
				}catch(Exception e){
					log.debug(e.getMessage(),e);
				}
			});
		}
	}
	
	public void clientRemoved(Long pid, ClientInfo client){
		if (canInvoke.get()){
			executorService.submit(()->{
				try{
					List<LookupResult> toBeRemoved=lookupResults.values(LookupResultFilters.byClientPid(client.getPid())).get();
					for (LookupResult r : toBeRemoved){
						lookupResults.remove(r.getId());
					}
				}catch(Exception e){
					log.debug(e.getMessage(),e);
				}
			});
		}
	}

	@Override
	public List<LookupResult> getLookupResults() throws Exception{
		return new ArrayList<LookupResult>(lookupResults.values().get());
	}
	
	@Override
	public LookupResult searchAgent(LookupRequest request) throws Exception{
		LookupResult lookupResult=new LookupResult();
		lookupResult.setLookupRequest(request);
		
		if (canInvoke.get()){
			Object lock=lockFactory.getLock(request.getQueue());
			try{
				synchronized(lock){
					AgentInfo result=compositeStrategy.lookup(request, null);
					
					if (result!=null){
						lookupResult.setId(globalLookupId.incrementAndGet().get());
						lookupResult.setAgentInfo(result);
						lookupResults.put(lookupResult.getId(), lookupResult).get();

					}else{
						//no agents found, what to do? 
						//TODO: introduce AgentCreator stuff ... with waiting mode or not
					}
				}
			}finally{
				lockFactory.freeLock(lock);
			}
		}
		return lookupResult;
	}
	
	@Override
	public void freeAgent(Long id) throws Exception{
		if (canInvoke.get()){
			lookupResults.remove(id).get();
		}
	}
	
	@Override
	public void freeAgent(List<Long> ids) throws Exception{
		if (canInvoke.get()){
			List<CompletableFuture<LookupResult>> fs=new ArrayList<>();
			ids.forEach(it->{
				fs.add(lookupResults.remove(it));
			});
			fs.forEach(it->{
				try{
					it.get();
				}catch(Exception e){
					log.error(e,e);
				}
			});
		}
	}
}