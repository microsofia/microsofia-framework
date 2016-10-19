package microsofia.framework.registry.lookup.strategy;

import java.util.List;

import microsofia.framework.agent.AgentFilters;
import microsofia.framework.agent.AgentInfo;
import microsofia.framework.map.Map;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;
import microsofia.framework.registry.lookup.LookupResultFilters;

public abstract class AbstractStrategy {
	protected Map<Long, AgentInfo> agents;
	protected Map<Long, LookupResult> lookupResults;

	protected AbstractStrategy(){
	}
	
	public void setAgents(Map<Long, AgentInfo> agents){
		this.agents=agents;
	}
	
	public void setLookupResults(Map<Long, LookupResult> lookupResults){
		this.lookupResults=lookupResults;
	}
	
	//filtering on servicename
	public List<AgentInfo> filterAgents(LookupRequest lookupRequest) throws Exception{
		return agents.values(AgentFilters.byServiceName(lookupRequest.getServiceName())).get();
	}
	
	public List<LookupResult> filterLookupResults(LookupRequest lookupRequest) throws Exception{
		return lookupResults.values(LookupResultFilters.byServiceName(lookupRequest.getServiceName())).get();
	}
	
	public abstract AgentInfo lookup(LookupRequest lookupRequest,List<AgentInfo> agentInfos) throws Exception;
}
