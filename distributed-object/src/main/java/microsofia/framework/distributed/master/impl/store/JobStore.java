package microsofia.framework.distributed.master.impl.store;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import microsofia.container.module.db.jpa.Entities;
import microsofia.container.module.db.jpa.JPA;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobResult;
import microsofia.framework.distributed.master.RemoteObjectInfo;
import microsofia.framework.distributed.master.SlaveConfig;

public class JobStore {
	@Inject
	@JPA("master")
	@Entities({SlaveConfig.class,Job.class,JobResult.class,RemoteObjectInfo.class})
	private EntityManagerFactory entityManagerFactory;
	@Inject
	private IJobListener jobListener;
	@Inject
	private JobResultStore jobResultStore;
	
	public JobStore(){
	}
	
	public static interface IJobStoreContainer{
		public RemoteObjectInfo createRemoteObjectInfo(EntityManager entityManager) throws Exception;
	}
	
	public static interface IJobListener{
		public void fireJobAdded(Job job);
		
		public void fireJobRemoved(Job job);
	}

	public long addJob(Job job,IJobStoreContainer jobStoreContainer) throws Exception {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				RemoteObjectInfo remoteObjectInfo=jobStoreContainer.createRemoteObjectInfo(entityManager);
				job.setRemoteObjectInfo(remoteObjectInfo);
				job.setStatus(Job.Status.CREATED);
				job.setCreationTime();
		
				entityManager.persist(job);
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
		jobListener.fireJobAdded(job);
		return job.getId();
	}

	public List<Long> addJob(List<Job> jobs,IJobStoreContainer jobStoreContainer) throws Exception {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				for (Job job : jobs){
					RemoteObjectInfo remoteObjectInfo=jobStoreContainer.createRemoteObjectInfo(entityManager);
					job.setRemoteObjectInfo(remoteObjectInfo);
					job.setStatus(Job.Status.CREATED);
					job.setCreationTime();
					
					entityManager.persist(job);
				}
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
		jobs.forEach(jobListener::fireJobAdded);
		return jobs.stream().map(Job::getId).collect(Collectors.toList());
	}
	
	public void jobStarted(long jobId,long slaveId) throws Exception{//TODO not great!!!
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				Job job=entityManager.find(Job.class, new Long(jobId));
				//TODO fill job.getRemoteObjectInfo())

				job.setStatusRunning();
				jobResultStore.jobStarted(entityManager,job);

				entityManager.persist(job);
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
	}
	
	public void jobFinished( long jobId,byte[] error, byte[] result) throws Exception{//TODO not great!!!
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				Job job=entityManager.find(Job.class, new Long(jobId));
				job.setStatusFinished();
				entityManager.merge(job);
				
				jobResultStore.jobFinished(entityManager, jobId, error, result);

				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
	}

	public Job getJob(long id) throws Exception {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			return entityManager.find(Job.class,new Long(id));
		}finally{
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Job> getJob(List<Long> id) throws Exception {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{						
			return entityManager.createQuery("SELECT j FROM "+Job.class.getName()+"  j WHERE j.id IN :ids")
						        .setParameter("ids", id)
						        .getResultList();
		}finally{
			entityManager.close();
		}
	}
}
