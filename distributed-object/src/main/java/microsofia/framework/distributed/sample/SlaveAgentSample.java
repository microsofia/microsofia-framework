package microsofia.framework.distributed.sample;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import microsofia.framework.distributed.slave.impl.Slave;
import microsofia.framework.distributed.slave.impl.SlaveAgent;

@Singleton
public class SlaveAgentSample extends SlaveAgent{
	@Inject
	private SlaveSample slaveSample;

	@Override
	public Class<?> getServiceClass() {
		return SlaveSample.class;
	}

	@Override
	public String getImplementation() {
		return "do.slave.sample";
	}

	@Override
	public List<AbstractModule> getGuiceModules() {
		return null;
	}

	@Override
	public List<Class<?>> getInjectedClasses() {
		return Arrays.asList(SlaveSample.class);
	}

	@Override
	protected Slave getSlave() {
		return slaveSample;
	}
}
