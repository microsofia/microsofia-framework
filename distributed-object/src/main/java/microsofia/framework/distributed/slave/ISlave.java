package microsofia.framework.distributed.slave;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.SlaveInfo;

@Server
public interface ISlave {

	public void startWorker(IMaster master,SlaveInfo slaveInfo) throws Exception;

	public SlaveInfo getSlaveInfo() throws Exception;

	public void tearDown(long roid,int tearnDownMethod,byte[] tearnDownArguments) throws Exception;
	
	public void stopWorker() throws Exception;
}
