package microsofia.framework.registry.lookup;

import java.util.List;

import microsofia.container.module.endpoint.Id;
import microsofia.container.module.endpoint.Server;

@Id
@Server
public interface ILookupService {

	public List<LookupResult> getLookupResults() throws Exception;
	
	public LookupResult searchAgent(LookupRequest request) throws Exception;

	public void freeAgent(Long id) throws Exception;

	public void freeAgent(List<Long> ids) throws Exception;
}
