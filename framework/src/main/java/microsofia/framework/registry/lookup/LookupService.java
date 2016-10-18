package microsofia.framework.registry.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import io.atomix.variables.DistributedLong;
import microsofia.framework.agent.AgentFilters;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.AgentLookupConfiguration;
import microsofia.framework.client.ClientInfo;
import microsofia.framework.invoker.InvokerServiceAdapter;
import microsofia.framework.map.Map;

public class LookupService implements InvokerServiceAdapter.IStartable,InvokerServiceAdapter.IStoppable,ILookupService{
	protected AtomicBoolean canInvoke;
	protected DistributedLong globalLookupId;
	protected Map<Long, AgentInfo> agents;
	protected Map<Long, ClientInfo> clients;
	protected Map<Long, LookupResult> lookupResults;

	public LookupService(){
		canInvoke=new AtomicBoolean(false);
	}
	
	public void setLookupResultId(DistributedLong globalLookupId){
		this.globalLookupId=globalLookupId;
	}
	
	public void setAgents(Map<Long, AgentInfo> agents) throws InterruptedException, ExecutionException{
		this.agents=agents;
		agents.setMapListener(this::agentRemoved).get();
	}
	
	public void setClients(Map<Long, ClientInfo> clients) throws InterruptedException, ExecutionException{
		this.clients=clients;
		clients.setMapListener(this::clientRemoved).get();
	}
	
	public void setLookupResults(Map<Long, LookupResult> lookupResults){
		this.lookupResults=lookupResults;
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
			new Thread(){
				public void run(){
					try{
						List<LookupResult> toBeRemoved=lookupResults.values(LookupResultFilters.byAgentPid(agent.getPid())).get();
						for (LookupResult r : toBeRemoved){
							freeAgent(r);
						}
					}catch(Exception e){
						e.printStackTrace();//TODO
					}
				}
			}.start();
		}
	}
	
	public void clientRemoved(Long pid, ClientInfo client){//TODO in another thread?
		if (canInvoke.get()){
			new Thread(){
				public void run(){
					try{
						List<LookupResult> toBeRemoved=lookupResults.values(LookupResultFilters.byClientPid(client.getPid())).get();
						for (LookupResult r : toBeRemoved){
							freeAgent(r);
						}
					}catch(Exception e){
						e.printStackTrace();//TODO
					}
				}
			}.start();
		}
	}
	
	@Override
	public LookupResult searchAgent(LookupRequest request) throws Exception{//TODO sync on serviceName
		LookupResult lookupResult=new LookupResult();
		lookupResult.setClientInfo(request.getClientInfo());
		if (canInvoke.get()){
			AgentInfo result=null;
			
			//filtering on servicename
			List<AgentInfo> agentInfos=agents.values(AgentFilters.byServiceName(request.getServiceName())).get();
	
			if (agentInfos.size()>0){
				AgentLookupConfiguration lookupConfigurationType=agentInfos.get(0).getLookupConfiguration();
				
				if (lookupConfigurationType.getMultiplicity().equals(AgentLookupConfiguration.Multiplicity.one)){
					//multiplicity is 1
					result=agentInfos.get(0);//TODO what to do if there are others?
					
				}else{
					//multiplicity is one_per_request or one_or_n
					List<LookupResult> results=lookupResults.values(LookupResultFilters.byServiceName(request.getServiceName())).get();
	
					if (lookupConfigurationType.getMultiplicity().equals(AgentLookupConfiguration.Multiplicity.one_per_request)){
						//order lookupresult by pid
						java.util.Map<Long,LookupResult> resultsByPid=new HashMap<>();
						results.forEach(it->{
							resultsByPid.put(it.getAgentInfo().getPid(), it);
						});
						
						//look for a non used pid
						for (AgentInfo ai : agentInfos){//order them differntly, maybe by how much they processed TODO
							if (!resultsByPid.containsKey(ai.getPid())){
								result=ai;
								break;
							}
						}
					
					}else{
						//one_or_n: order lookupresult by pid. There can be several lookupresult in one pid
						java.util.Map<Long,LookupResultsByService> resultsByPid=new HashMap<>();
						results.forEach(it->{
							LookupResultsByService tmp=resultsByPid.get(it.getAgentInfo().getPid());
							if (tmp==null){
								tmp=new LookupResultsByService(it.getAgentInfo());
								resultsByPid.put(it.getAgentInfo().getPid(),tmp);
							}
							tmp.addResult(it);
						});
						
						List<LookupResultsByService> orderedServices=Arrays.asList(resultsByPid.values().toArray(new LookupResultsByService[0]));
						Collections.sort(orderedServices);
						
						result=orderedServices.get(0).getAgentInfo();
					}
				}
				
				lookupResult.setId(globalLookupId.incrementAndGet().get());
				lookupResult.setAgentInfo(result);
		
				lookupResults.put(lookupResult.getId(), lookupResult).get();
			
			}else{
				//no agents found, what to do? TODO
			}
		}
		return lookupResult;
	}
	
	@Override
	public void freeAgent(LookupResult lookupResult) throws Exception{
		if (canInvoke.get()){
			lookupResults.remove(lookupResult.getId()).get();
		}
	}
	
	public static class LookupResultsByService implements Comparable<LookupResultsByService>{
		private AgentInfo agentInfo;
		private List<LookupResult> results;
		
		public LookupResultsByService(AgentInfo agentInfo){
			this.setAgentInfo(agentInfo);
			results=new ArrayList<>();
		}

		public AgentInfo getAgentInfo() {
			return agentInfo;
		}

		public void setAgentInfo(AgentInfo agentInfo) {
			this.agentInfo = agentInfo;
		}

		public void addResult(LookupResult result){
			results.add(result);
		}

		@Override
		public int compareTo(LookupResultsByService o) {
			if (getAgentInfo().getPid()==o.getAgentInfo().getPid()){
				return 0;
			}
			int nb1=results.size()/getAgentInfo().getLookupConfiguration().getWeigth();
			int nb2=o.results.size()/o.getAgentInfo().getLookupConfiguration().getWeigth();
			if (nb1<nb2){
				return -1;
				
			}else if (nb1>nb2){
				return 1;
				
			}else{
				if (results.size()<o.results.size()){
					return -1;

				}else{
					return 1;
				}
			}
		}
	}
}