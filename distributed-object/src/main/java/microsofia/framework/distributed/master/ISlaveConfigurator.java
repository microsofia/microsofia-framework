package microsofia.framework.distributed.master;

import java.util.List;

import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.slave.ISlave;

@Server
public interface ISlaveConfigurator {

	public long addSlave(SlaveConfig slaveConfig) throws Exception;
	
	public void updateSlave(SlaveConfig slaveConfig) throws Exception;
	
	public void removeSlave(long id) throws Exception;
	
	public SlaveConfig getSlaveConfig(long id) throws Exception;

	public List<SlaveConfig> getSlaveConfig() throws Exception;
	
	public void startSlave(long id) throws Exception;
	
	public void startSlave(List<Long> id) throws Exception;

	public void startAllSlave() throws Exception;

	public void stopSlave(long id) throws Exception;
	
	public void stopSlave(List<Long> id) throws Exception;

	public void stopAllSlave() throws Exception;

	public ISlave getSlave(long id) throws Exception;

	public List<ISlave> getSlave() throws Exception;
}
