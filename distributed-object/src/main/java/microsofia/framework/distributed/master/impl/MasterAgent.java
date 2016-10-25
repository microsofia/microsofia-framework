package microsofia.framework.distributed.master.impl;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import microsofia.framework.Agent;
import microsofia.framework.agent.AgentService;
import microsofia.framework.distributed.slave.impl.Slave;

@Singleton
public class MasterAgent implements Agent {
	@Inject
	private AgentService agentService;
	@Inject
	private Master master;
	
	public MasterAgent(){
	}

	@Override
	public Class<?> getServiceClass(){
		return Master.class;
	}
	
	@Override
	public String getImplementation() {
		return "do.master";
	}

	@Override
	public List<AbstractModule> getGuiceModules() {
		return null;
	}

	@Override
	public List<Class<?>> getInjectedClasses() {
		return Arrays.asList(Master.class,JobQueue.class,ObjectAllocator.class,SlaveConfigurator.class,Slave.class);
	}

	@Override
	public void start() throws Exception {
		master.start();
		agentService.start();
	}

	@Override
	public void stop() throws Exception {
		master.stop();
		agentService.stop();
	}
}
