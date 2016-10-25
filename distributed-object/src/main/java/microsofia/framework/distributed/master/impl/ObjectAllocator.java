package microsofia.framework.distributed.master.impl;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IObjectAllocator;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobResult;
import microsofia.framework.distributed.master.RemoteObjectInfo;
import microsofia.framework.distributed.master.RemoteObjectInfo.Type;
import microsofia.framework.distributed.master.impl.store.JobResultStore;
import microsofia.framework.distributed.master.impl.store.JobStore;

@Singleton
@Server("fwk")
@Export
public class ObjectAllocator implements IObjectAllocator,JobStore.IJobStoreContainer{
	@Inject
	protected JobStore jobStore;
	@Inject
	protected JobResultStore jobResultStore;
	
	public ObjectAllocator(){
	}

	@Override
	public RemoteObjectInfo newRemoteObject() throws Exception{
		return null;
	}
	
	@Override
	public RemoteObjectInfo createRemoteObjectInfo(EntityManager entityManager) throws Exception {
		RemoteObjectInfo remoteObjectInfo=new RemoteObjectInfo();
		remoteObjectInfo.setCreationTime();
		remoteObjectInfo.setType(Type.STATE_LESS);
		entityManager.persist(remoteObjectInfo);
		return remoteObjectInfo;
	}
	
	@Override
	public long addJob(long roid,Job job) throws Exception {
		return jobStore.addJob(job, this);
	}

	@Override
	public List<Long> addJob(long roid,List<Job> jobs) throws Exception {
		return jobStore.addJob(jobs, this);
	}

	@Override
	public Job getJob(long id) throws Exception {
		return jobStore.getJob(id);
	}

	@Override
	public List<Job> getJob(List<Long> id) throws Exception {
		return jobStore.getJob(id);
	}

	@Override
	public JobResult getJobResult(long id) throws Exception {
		return jobResultStore.getJobResult(id);
	}

	@Override
	public List<JobResult> getJobResult(List<Long> id) throws Exception {
		return jobResultStore.getJobResult(id);
	}

	@Override
	public JobResult takeJobResult(long id) throws Exception {
		return jobResultStore.takeJobResult(id);
	}

	@Override
	public List<JobResult> takeJobResult(List<Long> ids) throws Exception {
		return jobResultStore.takeJobResult(ids);
	}
}
