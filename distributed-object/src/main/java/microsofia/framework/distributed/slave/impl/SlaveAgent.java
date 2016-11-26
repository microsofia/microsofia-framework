package microsofia.framework.distributed.slave.impl;


import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;

import microsofia.framework.Agent;
import microsofia.framework.agent.AgentService;
import microsofia.framework.distributed.master.impl.ClassMetadata;
import microsofia.framework.distributed.slave.SlaveInstance;
import microsofia.framework.distributed.slave.impl.Slave;

@Singleton
public abstract class SlaveAgent implements Agent {
	@Inject
	private AgentService agentService;
	@Inject
	private Slave slave;
	
	public SlaveAgent(){
	}

	public abstract Class<?> getSlaveInstanceClass();
	
	@Override
	public List<Class<?>> getInjectedClasses() {
		return Arrays.asList(Slave.class,getSlaveInstanceClass());
	}
	
	@Override
	public List<AbstractModule> getGuiceModules() {
		return Arrays.asList(new AbstractModule() {
			
			@Provides
			@Named("ClassMetadata")
			public ClassMetadata getClassMetadata(@SlaveInstance Object instance){
				return new ClassMetadata(instance.getClass());
			}
			
			@Override
			protected void configure() {
				bind(Key.get(Object.class, SlaveInstance.class)).to(SlaveAgent.this.getSlaveInstanceClass()).asEagerSingleton();
			}
		});
	}
	
	@Override
	public Class<?> getServiceClass() {
		return Slave.class;
	}

	@Override
	public void start() throws Exception {
		slave.start();
		agentService.start();
	}

	@Override
	public void stop() throws Exception {
		slave.stop();
		agentService.stop();
	}
}
