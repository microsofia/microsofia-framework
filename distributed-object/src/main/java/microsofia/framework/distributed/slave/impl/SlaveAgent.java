package microsofia.framework.distributed.slave.impl;


import javax.inject.Inject;
import javax.inject.Singleton;

import microsofia.framework.Agent;
import microsofia.framework.agent.AgentService;
import microsofia.framework.distributed.slave.impl.Slave;

@Singleton
public abstract class SlaveAgent implements Agent {
	@Inject
	private AgentService agentService;
	
	public SlaveAgent(){
	}

	protected abstract Slave getSlave();
	
	@Override
	public void start() throws Exception {
		getSlave().start();
		agentService.start();
	}

	@Override
	public void stop() throws Exception {
		getSlave().stop();
		agentService.stop();
	}
}
