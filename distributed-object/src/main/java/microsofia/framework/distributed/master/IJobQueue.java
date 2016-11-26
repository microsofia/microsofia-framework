package microsofia.framework.distributed.master;

import microsofia.container.module.endpoint.Server;

@Server
public interface IJobQueue {

	public JobItem takeJob(long slaveId) throws Exception;
	
	public void jobFailed(long jobId,byte[] error) throws Exception;

	public void jobSucceeded(long jobId,byte[] result) throws Exception;
}
