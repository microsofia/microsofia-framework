package microsofia.framework.registry.lookup.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.AgentLookupConfiguration;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;

@Singleton
public class CompositeStrategy extends AbstractStrategy{
	private Map<AgentLookupConfiguration.Multiplicity, AbstractStrategy> strategies;

	public CompositeStrategy(){
		strategies=new HashMap<>();
		strategies.put(AgentLookupConfiguration.Multiplicity.one, new OneStrategy());
		strategies.put(AgentLookupConfiguration.Multiplicity.one_per_request, new OnePerRequestStrategy());
		strategies.put(AgentLookupConfiguration.Multiplicity.one_or_n, new OneOrNStrategy());
	}
	
	@Override
	public void setAgents(microsofia.framework.map.Map<Long, AgentInfo> agents){
		super.setAgents(agents);
		strategies.values().forEach(it->it.setAgents(agents));
	}
	
	@Override
	public void setLookupResults(microsofia.framework.map.Map<Long, LookupResult> lookupResults){
		super.setLookupResults(lookupResults);
		strategies.values().forEach(it->it.setLookupResults(lookupResults));
	}

	@Override
	public AgentInfo lookup(LookupRequest lookupRequest,List<AgentInfo> agentInfos) throws Exception{
		AgentInfo result=null;
		agentInfos=filterAgents(lookupRequest);

		if (agentInfos.size()>0){
			AgentLookupConfiguration lookupConfiguration=agentInfos.get(0).getLookupConfiguration();
			AbstractStrategy strategy=strategies.get(lookupConfiguration.getMultiplicity());
			result=strategy.lookup(lookupRequest, agentInfos);

		}
		return result;
	}	
}