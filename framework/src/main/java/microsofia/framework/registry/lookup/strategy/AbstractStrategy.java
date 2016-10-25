package microsofia.framework.registry.lookup.strategy;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import microsofia.framework.agent.AgentFilters;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;
import microsofia.framework.registry.lookup.LookupResultFilters;
import microsofia.framework.service.AbstractService;

public abstract class AbstractStrategy {
	@Inject
	@Named(AbstractService.KEY_AGENTS)
	protected Map<Long, AgentInfo> agents;
	@Inject
	@Named(AbstractService.KEY_LOOKUP_RESULT)
	protected Map<Long, LookupResult> lookupResults;

	protected AbstractStrategy(){
	}
	
	public void setAgents(Map<Long, AgentInfo> agents){
		this.agents=agents;
	}
	
	public void setLookupResults(Map<Long, LookupResult> lookupResults){
		this.lookupResults=lookupResults;
	}
	
	//filtering on queue
	public List<AgentInfo> filterAgents(LookupRequest lookupRequest) throws Exception{
		return agents.values(AgentFilters.byQueue(lookupRequest.getQueue())).get();
	}
	
	public List<LookupResult> filterLookupResults(LookupRequest lookupRequest) throws Exception{
		return lookupResults.values(LookupResultFilters.byQueue(lookupRequest.getQueue())).get();
	}
	
	public abstract AgentInfo lookup(LookupRequest lookupRequest,List<AgentInfo> agentInfos) throws Exception;
}
