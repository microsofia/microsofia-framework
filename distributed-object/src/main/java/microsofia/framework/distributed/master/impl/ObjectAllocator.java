package microsofia.framework.distributed.master.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.inject.Singleton;
import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.distributed.master.IObjectAllocator;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobResult;
import microsofia.framework.distributed.master.VirtualObjectInfo;
import microsofia.framework.distributed.master.VirtualObjectInfo.Type;
import microsofia.framework.distributed.master.dao.DataAccess;
import microsofia.framework.distributed.slave.ISlave;

@Singleton
@Server("fwk")
@Export
public class ObjectAllocator implements IObjectAllocator{
	private static Log log=LogFactory.getLog(ObjectAllocator.class);
	@Inject
	private DataAccess dataAccess;
	@Inject
	private JobQueue jobQueue;
	@Inject
	private SlaveConfigurator slaveConfigurator;
	
	public ObjectAllocator(){
	}
	
	@SuppressWarnings("unchecked")
	public void start() throws Exception{
		dataAccess.read(em->{
			List<Job> jobs=em.createQuery("SELECT j FROM "+Job.class.getName()+"  j WHERE j.status = :s")
			 		 .setParameter("s", Job.Status.CREATED)
			 		 .getResultList();
			jobs.forEach(jobQueue::jobAdded);
			
			return null;
		});
	}
	
	private VirtualObjectInfo createVirtualObject(EntityManager entityManager,Type type,int setupMethod, byte[] setupArguments, int tearDownMethod, byte[] tearDownArguments) throws Exception{
		VirtualObjectInfo virtualObjectInfo=new VirtualObjectInfo();
		virtualObjectInfo.setCreationTime();
		virtualObjectInfo.setType(type);
		virtualObjectInfo.setStatusCreated();
		virtualObjectInfo.setSetupMethod(setupMethod);
		virtualObjectInfo.setSetupArguments(setupArguments);
		virtualObjectInfo.setTearnDownMethod(tearDownMethod);
		virtualObjectInfo.setTearnDownArguments(tearDownArguments);
		entityManager.persist(virtualObjectInfo);
		return virtualObjectInfo;
	}
	
	@Override
	public VirtualObjectInfo createVirtualObject(int setupMethod, byte[] setupArguments, int tearDownMethod, byte[] tearDownArguments) throws Exception{
		return dataAccess.write(em->{
			return createVirtualObject(em,Type.STATE_FULL,setupMethod,setupArguments,tearDownMethod,tearDownArguments);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeVirtualObject(long roid) throws Exception{
		dataAccess.write(em->{
			VirtualObjectInfo virtualObjectInfo=em.find(VirtualObjectInfo.class, new Long(roid));
			if (virtualObjectInfo.isTypeStateLess()){
				throw new IllegalArgumentException("Cannot remove stateless virtual object: id="+roid+", instance="+virtualObjectInfo);
			}
			
			List<Job> jobs=em.createQuery("SELECT j FROM "+Job.class.getName()+"  j WHERE j.virtualObjectInfo = :roid and j.status not in :s")
					 		 .setParameter("roid", roid)
					 		 .setParameter("s", Arrays.asList(Job.Status.FINISHED,Job.Status.STOPPED))
					 		 .getResultList();
			if (jobs.size()>0){
				throw new IllegalStateException("Cannot remove virtual object:"+virtualObjectInfo+" as at least one job is still not finished or stopped: "+jobs.get(0));
			}
			
			if (virtualObjectInfo.isTypeStateFull() && virtualObjectInfo.getTearnDownMethod()>0){
				try{
					ISlave slave=slaveConfigurator.getSlave(virtualObjectInfo.getSlaveInfo().getId());
					slave.tearDown(virtualObjectInfo.getId(),virtualObjectInfo.getTearnDownMethod(),virtualObjectInfo.getTearnDownArguments());
				}catch(Exception e){
					log.error(e,e);
					//TODO:later. Maybe slave is stopped. we should check when slave is started again
				}
			}
			
			virtualObjectInfo.setStatusFinished();
			virtualObjectInfo.setEndTime();
			
			return null;
		});
	}
	
	private void jobCreated(EntityManager em,VirtualObjectInfo virtualObjectInfo,Job job){
		job.setVirtualObjectInfo(virtualObjectInfo);
		job.setStatusCreated();
		job.setCreationTime();
		em.persist(job);
	}

	private void checkVirtualObjectStatus(VirtualObjectInfo virtualObjectInfo) throws Exception{
		if (virtualObjectInfo.getStatus()!=VirtualObjectInfo.Status.CREATED){
			throw new IllegalStateException("Using a killed virtual object: "+virtualObjectInfo);
		}
	}
	
	@Override
	public long addJob(long roid,Job job) throws Exception {
		dataAccess.write(em->{
			VirtualObjectInfo virtualObjectInfo;
			if (roid<=0){
				virtualObjectInfo=createVirtualObject(em,Type.STATE_LESS,0,null,0,null);
			}else{
				virtualObjectInfo=em.find(VirtualObjectInfo.class,new Long(roid));
				if (virtualObjectInfo==null){
					throw new IllegalArgumentException("Could not find state full virtual object with id:"+roid);
				}
				checkVirtualObjectStatus(virtualObjectInfo);
			}

			jobCreated(em, virtualObjectInfo,job);

			return null;
		});
		
		jobQueue.jobAdded(job);
		return job.getId();
	}

	@Override
	public List<Long> addJob(long roid,List<Job> jobs) throws Exception {
		dataAccess.write(em->{
			VirtualObjectInfo virtualObjectInfo=null;
			if (roid>0){
				virtualObjectInfo=em.find(VirtualObjectInfo.class,new Long(roid));
				if (virtualObjectInfo==null){
					throw new IllegalArgumentException("Could not find state full virtual object with id:"+roid);
				}
				checkVirtualObjectStatus(virtualObjectInfo);
			}
			for (Job job : jobs){
				if (roid<=0){
					virtualObjectInfo=createVirtualObject(em,Type.STATE_LESS,0,null,0,null);
				}

				jobCreated(em,virtualObjectInfo,job);
			}
			return null;
		});
		
		jobs.forEach(jobQueue::jobAdded);
		return jobs.stream().map(Job::getId).collect(Collectors.toList());
	}
	
	@Override
	public void updateJobPriority(List<Long> ids, int priority) throws Exception{
		List<Job> jobs=dataAccess.write(em->{
			List<Job> tmp=new ArrayList<>();
			for (Long id : ids){
				Job job=em.find(Job.class, id);
				if (job!=null){
					job.setPriority(priority);
					tmp.add(job);
				}
			}
			return tmp;
		});
		
		jobs.forEach(jobQueue::jobAdded);
	}

	@Override
	public void stopJob(long id) throws Exception{
		jobQueue.jobStopped(id);
		
		//TODO:later we might have issue if parallel read/write to a job, use memory lock
		dataAccess.write(em->{
			Job job=em.find(Job.class, new Long(id));
			if (job.isStatusCreated()){
				job.setStatusStopped();
			}
			return null;
		});
	}
	
	@Override
	public void stopJob(List<Long> ids) throws Exception{
		jobQueue.jobStopped(ids);
		
		dataAccess.write(em->{
			for (Long id : ids){
				Job job=em.find(Job.class, new Long(id));
				if (job.isStatusCreated()){
					job.setStatusStopped();
				}
			}
			return null;
		});
	}

	@Override
	public Job getJob(long id) throws Exception {
		return dataAccess.read(em->{
			return em.find(Job.class,new Long(id));
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Job> getJob(List<Long> id) throws Exception {
		return dataAccess.read(em->{
			return em.createQuery("SELECT j FROM "+Job.class.getName()+"  j WHERE j.id IN :ids")
					 .setParameter("ids", id)
					 .getResultList();
		});		
	}

	@Override
	public JobResult getJobResult(long id) throws Exception {
		return dataAccess.read(em->{
			return em.find(JobResult.class,new Long(id));
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<JobResult> getJobResult(List<Long> id) throws Exception {
		return dataAccess.read(em->{
			return em.createQuery("SELECT jr FROM "+JobResult.class.getName()+"  jr WHERE jr.id IN :ids")
			         .setParameter("ids", id)
			         .getResultList();
		});
	}

	@Override
	public JobResult takeJobResult(long id) throws Exception {
		return dataAccess.write(em->{
			JobResult jobResult=em.find(JobResult.class, new Long(id));
			if (jobResult!=null && jobResult.isStatusFinished()){
				jobResult.setStatusArchived();
				em.persist(jobResult);
				return jobResult;
			}
			return null;
		});
	}

	@Override
	public List<JobResult> takeJobResult(List<Long> ids) throws Exception {//TODO:later cleaning of archived?
		return dataAccess.write(em->{
			List<JobResult> jobResults=new ArrayList<JobResult>(ids.size());
			for (Long id : ids){
				JobResult tmp=em.find(JobResult.class, new Long(id));
				if (tmp!=null && tmp.isStatusFinished()){
					tmp.setStatusArchived();
					em.persist(tmp);
					jobResults.add(tmp);
				}else{
					jobResults.add(null);
				}				
			}
			return jobResults;
		});
	}
}
