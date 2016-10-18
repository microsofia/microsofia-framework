package microsofia.framework.registry.lookup;

public interface ILookupService {

	public LookupResult searchAgent(LookupRequest request) throws Exception;

	public void freeAgent(LookupResult result) throws Exception;
}
