package microsofia.framework.distributed.slave;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.SlaveConfig;

@Server
public interface ISlave {

	public void startWorker(IMaster master,SlaveConfig slaveConfig) throws Exception;

	public SlaveConfig getSlaveConfig();
	
	public void stopWorker() throws Exception;
}
