package microsofia.framework.registry.lookup;

import microsofia.framework.service.ServiceAddress;

public interface ILookupService {

	public ServiceAddress searchAgent() throws Exception;
}
