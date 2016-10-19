package microsofia.framework.registry.lookup;

import java.util.List;

import microsofia.container.module.endpoint.Server;

@Server
public interface ILookupService {

	public List<LookupResult> getLookupResults() throws Exception;
	
	public LookupResult searchAgent(LookupRequest request) throws Exception;

	public void freeAgent(LookupResult result) throws Exception;
}
