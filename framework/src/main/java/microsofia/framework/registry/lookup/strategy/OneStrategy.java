package microsofia.framework.registry.lookup.strategy;

import java.util.List;

import javax.inject.Singleton;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.agent.AgentLookupConfiguration;
import microsofia.framework.registry.lookup.LookupRequest;

@Singleton
public class OneStrategy extends AbstractStrategy{

	public OneStrategy(){
	}

	@Override
	public AgentInfo lookup(LookupRequest lookupRequest,List<AgentInfo> agentInfos) throws Exception{
		AgentInfo result=null;		

		if (agentInfos.size()>0){
			AgentLookupConfiguration lookupConfigurationType=agentInfos.get(0).getLookupConfiguration();
			
			if (lookupConfigurationType.getMultiplicity().equals(AgentLookupConfiguration.Multiplicity.one)){
				//multiplicity is 1
				result=agentInfos.get(0);//there shouldnt be more than one active, it is validated at agent startup
			}
		}
		return result;
	}	
}
