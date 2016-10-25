package microsofia.framework.registry.lookup.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.AgentLookupConfiguration;
import microsofia.framework.registry.lookup.LookupRequest;

@Singleton
public class CompositeStrategy extends AbstractStrategy{
	private Map<AgentLookupConfiguration.Multiplicity, AbstractStrategy> strategies;
	@Inject
	private OneStrategy oneStrategy;
	@Inject
	private OnePerRequestStrategy onePerRequestStrategy;
	@Inject
	private OneOrNStrategy oneOrNStrategy;

	public CompositeStrategy(){
		strategies=new HashMap<>();
	}
	
	public void start() throws Exception{
		strategies.put(AgentLookupConfiguration.Multiplicity.one, oneStrategy);
		strategies.put(AgentLookupConfiguration.Multiplicity.one_per_request, onePerRequestStrategy);
		strategies.put(AgentLookupConfiguration.Multiplicity.one_or_n, oneOrNStrategy);
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