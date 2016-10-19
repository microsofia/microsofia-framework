package microsofia.framework.registry.lookup.strategy;

import java.util.ArrayList;
import java.util.List;

import microsofia.framework.agent.AgentInfo;
import microsofia.framework.registry.lookup.LookupResult;

public class LookupResultsByService implements Comparable<LookupResultsByService>{
	private AgentInfo agentInfo;
	private int resultWeigth;
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
		resultWeigth+=result.getLookupRequest().getWeight();
		results.add(result);
	}

	@Override
	public int compareTo(LookupResultsByService o) {
		if (getAgentInfo().getPid()==o.getAgentInfo().getPid()){
			return 0;
		}
		int nb1=resultWeigth/getAgentInfo().getLookupConfiguration().getWeigth();
		int nb2=o.resultWeigth/o.getAgentInfo().getLookupConfiguration().getWeigth();
		if (nb1<nb2){
			return -1;
			
		}else if (nb1>nb2){
			return 1;
			
		}else{
			if (resultWeigth<o.resultWeigth){
				return -1;

			}else{
				if (getAgentInfo().getLookupConfiguration().getWeigth()>o.getAgentInfo().getLookupConfiguration().getWeigth()){
					return -1;
				}
				return 1;
			}
		}
	}
}