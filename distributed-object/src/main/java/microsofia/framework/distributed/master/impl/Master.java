package microsofia.framework.distributed.master.impl;

import javax.inject.Inject;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.framework.distributed.master.IJobQueue;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.IObjectAllocator;
import microsofia.framework.distributed.master.ISlaveConfigurator;

@Singleton
@Server("fwk")
public class Master implements IMaster{
	@Inject
	private SlaveConfigurator slaveConfigurator;
	@Inject
	private ObjectAllocator objectAllocator;
	@Inject
	private JobQueue jobQueue;

	public Master(){
	}
	
	@Export
	public void start(){
		slaveConfigurator.start();
	}
	
	@Unexport
	public void stop(){
		slaveConfigurator.stop();
	}
	
	@Override
	public ISlaveConfigurator getSlaveConfigurator() {
		return slaveConfigurator;
	}

	@Override
	public IObjectAllocator getObjectAllocator() {
		return objectAllocator;
	}

	@Override
	public IJobQueue getJobQueue() {
		return jobQueue;
	}
}
