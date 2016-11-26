package microsofia.framework.distributed.master;

import microsofia.container.module.endpoint.Server;

@Server
public interface IMaster {

	public ISlaveConfigurator getSlaveConfigurator() throws Exception;
	
	public IObjectAllocator getObjectAllocator() throws Exception;
	
	public IJobQueue getJobQueue() throws Exception;
}
