package microsofia.framework.distributed.sample;

import com.google.inject.Singleton;
import microsofia.framework.distributed.slave.impl.SlaveAgent;

@Singleton
public class SlaveAgentSample extends SlaveAgent{

	@Override
	public String getImplementation() {
		return "do.slave.sample";
	}

	@Override
	public Class<?> getSlaveInstanceClass(){
		return Sample.class;
	}
	
}
