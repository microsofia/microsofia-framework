package microsofia.framework.distributed.master;

import microsofia.container.module.endpoint.Server;

@Server
public interface IMaster {

	public ISlaveConfigurator getSlaveConfigurator();
	
	public IObjectAllocator getObjectAllocator();
	
	public IJobQueue getJobQueue();
}
