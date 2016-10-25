package microsofia.framework.distributed.master.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IJobQueue;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.impl.store.JobResultStore;
import microsofia.framework.distributed.master.impl.store.JobStore;
import microsofia.framework.distributed.master.impl.store.JobStore.IJobListener;

@Singleton
@Server("fwk")
@Export
public class JobQueue implements IJobQueue, IJobListener{
	@Inject
	protected JobResultStore jobResultStore;
	@Inject
	protected JobStore jobStore;
	private Set<Job> jobs;
	
	public JobQueue(){
		jobs=new HashSet<>();
	}

	@Override
	public Job takeJob(long slaveId) throws Exception {
		Job job;
		synchronized(this){
			TreeSet<Job> treeSet=new TreeSet<>(jobComparator);
			treeSet.addAll(jobs);
			job=treeSet.first();
			jobs.remove(job);
		}
		jobStore.jobStarted(job.getId(),slaveId);
		return job;
	}

	@Override
	public void jobFinished(long jobId,byte[] error, byte[] result) throws Exception{
		jobStore.jobFinished(jobId,error, result);
	}

	@Override
	public synchronized void fireJobAdded(Job job) {
		jobs.add(job);
	}

	@Override
	public void fireJobRemoved(Job job) {
	}

	static JobComparator jobComparator = new JobComparator();
	
	private static class JobComparator implements Comparator<Job>{
		
		JobComparator(){
		}

		@Override
		public int compare(Job j1, Job j2) {//TODO take into account that it is on that slave
			if (j1.getPriority()<j2.getPriority()){
				return -1;

			}else if (j1.getPriority()>j2.getPriority()){
				return 1;
				
			}else{
				if (j1.getCreationTime()<j2.getCreationTime()){
					return -1;
							
				}else if (j1.getCreationTime()>j2.getCreationTime()){
					return 1;
				
				}else{
					if (j1.getId()==j2.getId()){
						return 0;
					}
					return 1;
				}
			}
		}		
	}
}
