package microsofia.framework.distributed.master.impl;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IJobQueue;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.IObjectAllocator;
import microsofia.framework.distributed.master.ISlaveConfigurator;

@Singleton
@Server("fwk")
@Export
public class Master implements IMaster{
	@Inject
	private SlaveConfigurator slaveConfigurator;
	@Inject
	private ObjectAllocator objectAllocator;
	@Inject
	private JobQueue jobQueue;
	@Inject
	@Named("master")
	private ExecutorService executorService;

	public Master(){
	}
	
	public void start() throws Exception{
		objectAllocator.start();
		slaveConfigurator.start();
	}
	
	public void stop(){
		slaveConfigurator.stop();
		executorService.shutdown();
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
