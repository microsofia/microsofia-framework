package microsofia.framework.distributed.master;

import java.util.List;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.slave.ISlave;

@Server
public interface ISlaveConfigurator {

	public long addSlave(SlaveInfo slaveInfo) throws Exception;
	
	public void setSlavePoolSize(long slaveId,int poolSize) throws Exception;
	
	public void setSlaveNameAndGroup(long slaveId,String name,String group) throws Exception;
	
	public void removeSlave(long id) throws Exception;
	
	public SlaveInfo getSlaveInfo(long id) throws Exception;

	public List<SlaveInfo> getSlaveInfo() throws Exception;
	
	public void startSlave(long id) throws Exception;
	
	public void startSlave(List<Long> id) throws Exception;

	public void startAllSlave() throws Exception;

	public void stopSlave(long id) throws Exception;
	
	public void stopSlave(List<Long> id) throws Exception;

	public void stopAllSlave() throws Exception;

	public ISlave getSlave(long id) throws Exception;

	public List<ISlave> getSlave() throws Exception;
}
