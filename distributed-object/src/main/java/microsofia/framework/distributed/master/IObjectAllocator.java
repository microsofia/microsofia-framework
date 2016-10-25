package microsofia.framework.distributed.master;

import java.util.List;

import microsofia.container.module.endpoint.Server;

@Server
public interface IObjectAllocator {
	
	public RemoteObjectInfo newRemoteObject() throws Exception;
	
	public long addJob(long roid,Job job) throws Exception;
	
	public List<Long> addJob(long roid,List<Job> jobs) throws Exception;

	public Job getJob(long id) throws Exception;
	
	public List<Job> getJob(List<Long> id) throws Exception;
	
	public JobResult getJobResult(long id) throws Exception;
	
	public List<JobResult> getJobResult(List<Long> id) throws Exception;
	
	public JobResult takeJobResult(long id) throws Exception;
	
	public List<JobResult> takeJobResult(List<Long> id) throws Exception;
}
