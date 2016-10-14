package microsofia.framework.registry;

import microsofia.framework.service.ServiceAddress;

public interface IAllocationService {

	public ServiceAddress allocate() throws Exception;
}
