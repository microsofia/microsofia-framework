package microsofia.framework.distributed.master.impl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IJobQueue;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobItem;
import microsofia.framework.distributed.master.JobResult;
import microsofia.framework.distributed.master.VirtualObjectInfo;
import microsofia.framework.distributed.master.SlaveInfo;
import microsofia.framework.distributed.master.dao.DataAccess;

@Singleton
@Server("fwk")
@Export
public class JobQueue implements IJobQueue{
	@Inject 
	protected JobComparator jobComparator;
	@Inject
	protected DataAccess dataAccess;
	private Map<Long,Job> jobs;
	
	public JobQueue(){
		jobs=new Hashtable<>();
	}

	//TODO:later if slave didnt call takeJob since x time, then init virtualobjects, so that other slaves take them
	@Override
	public JobItem takeJob(long slaveId) throws Exception {
		final JobItem jobItem;
		synchronized(this){
			jobComparator.setTime();
			
			TreeSet<Job> treeSet=new TreeSet<>(jobComparator);
			jobs.values().forEach(it->{
				if (it.getVirtualObjectInfo().getSlaveInfo()==null || 
					it.getVirtualObjectInfo().getSlaveInfo().getId()==slaveId){
					treeSet.add(it);
				}
			});
			
			if (treeSet.size()>0){
				Job job=treeSet.first();
				jobs.remove(job.getId());
				
				jobItem=new JobItem();
				jobItem.setJob(job);
			}else{
				jobItem=null;
			}
		}
		
		if (jobItem!=null){
			Job job=dataAccess.write(em->{
				Job tmpJob=em.find(Job.class, new Long(jobItem.getJob().getId()));

				VirtualObjectInfo virtualObjectInfo=tmpJob.getVirtualObjectInfo();
				if (virtualObjectInfo.isTypeStateFull() && virtualObjectInfo.getSlaveInfo()==null){
					jobItem.setSetup(virtualObjectInfo);
				}
				virtualObjectInfo.setSlaveInfo(em.find(SlaveInfo.class, new Long(slaveId)));
	
				tmpJob.setStatusRunning();
				
				JobResult jobResult=new JobResult(tmpJob);
				tmpJob.setJobResult(jobResult);

				em.persist(tmpJob);
				return tmpJob;
			});
			jobItem.setJob(job);
		}
		return jobItem;
	}

	private Job jobFinished(EntityManager em, Long jobId){
		Job job=em.find(Job.class, new Long(jobId));
		job.setStatusFinished();

		JobResult jobResult=job.getJobResult();
		jobResult.setEndTime();
		jobResult.setStatusFinished();
		
		if (job.getVirtualObjectInfo().isTypeStateLess()){
			job.getVirtualObjectInfo().setEndTime();
			job.getVirtualObjectInfo().setStatusFinished();
		}
		return job;
	}
	
	@Override
	public void jobFailed(long jobId,byte[] error) throws Exception{
		dataAccess.write(em->{
			Job job=jobFinished(em, jobId);
			job.getJobResult().setError(error);
			em.merge(job);
			
			return null;
		});
	}

	@Override
	public void jobSucceeded(long jobId,byte[] result) throws Exception{
		dataAccess.write(em->{
			Job job=jobFinished(em, jobId);
			job.getJobResult().setResult(result);
			em.merge(job);
			
			return null;
		});
	}

	public synchronized void jobAdded(Job job) {
		jobs.put(job.getId(),job);
	}
	
	public synchronized void jobStopped(long id){
		jobs.remove(id);
	}

	public synchronized void jobStopped(List<Long> ids){
		ids.forEach(jobs::remove);
	}
}
