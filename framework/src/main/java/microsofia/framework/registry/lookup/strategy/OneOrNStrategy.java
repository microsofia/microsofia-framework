package microsofia.framework.registry.lookup.strategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Singleton;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.registry.lookup.LookupRequest;
import microsofia.framework.registry.lookup.LookupResult;

@Singleton
public class OneOrNStrategy extends AbstractStrategy{

	public OneOrNStrategy(){
	}

	@Override
	public AgentInfo lookup(LookupRequest lookupRequest,List<AgentInfo> agentInfos) throws Exception{
		AgentInfo result=null;		
		List<LookupResult> filteredLookupResults=filterLookupResults(lookupRequest);
		
		java.util.Map<Long,LookupResultsByService> resultsByPid=new HashMap<>();
		filteredLookupResults.forEach(it->{
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
		return result;
	}	
}