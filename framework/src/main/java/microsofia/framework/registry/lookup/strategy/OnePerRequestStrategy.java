package microsofia.framework.registry.lookup.strategy;

import java.util.HashMap;
import java.util.List;

import javax.inject.Singleton;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;

@Singleton
public class OnePerRequestStrategy extends AbstractStrategy{

	public OnePerRequestStrategy(){
	}

	@Override
	public AgentInfo lookup(LookupRequest lookupRequest,List<AgentInfo> agentInfos) throws Exception{
		AgentInfo result=null;		
		List<LookupResult> filteredLookupResults=filterLookupResults(lookupRequest);
		
		//order by pid
		java.util.Map<Long,LookupResult> resultsByPid=new HashMap<>();
		filteredLookupResults.forEach(it->{
			resultsByPid.put(it.getAgentInfo().getPid(), it);
		});
		
		//look for a non used pid
		for (AgentInfo ai : agentInfos){//TODO: order them differently, keep history
			if (!resultsByPid.containsKey(ai.getPid())){
				result=ai;
				break;
			}
		}
		return result;
	}	
}