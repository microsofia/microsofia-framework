package microsofia.framework.distributed.sample;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.framework.distributed.slave.ISlave;
import microsofia.framework.distributed.slave.impl.Slave;

@Singleton
@Server("fwk")
public class SlaveSample extends Slave implements ISlave{

	public SlaveSample(){
	}

	@Export
	@Override
	public void start() throws Exception {
	}

	@Unexport
	@Override
	public void stop() throws Exception {
	}
}
