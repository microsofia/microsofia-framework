package microsofia.framework.distributed.master;

import microsofia.container.module.endpoint.Server;

@Server
public interface IJobQueue {

	public Job takeJob(long slaveId) throws Exception;
	
	public void jobFinished(long jobId,byte[] error, byte[] result) throws Exception;
}
