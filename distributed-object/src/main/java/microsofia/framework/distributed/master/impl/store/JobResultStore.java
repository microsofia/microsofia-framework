package microsofia.framework.distributed.master.impl.store;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import microsofia.container.module.db.jpa.JPA;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobResult;

public class JobResultStore {
	@Inject
	@JPA("master")
	private EntityManagerFactory entityManagerFactory;
	@Inject
	private IJobResultListener jobResultListener;
	
	public JobResultStore(){
	}
	
	public static interface IJobResultListener{
		
		public void jobResultAdded(JobResult jobResult);
		
		public void jobResultRemoved(JobResult jobResult);
	}
	
	public void jobStarted(EntityManager entityManager,Job job) throws Exception{
		JobResult jobResult=new JobResult(job);
		jobResult.setStartTime();
		job.setJobResult(jobResult);

		entityManager.persist(jobResult);
	}
	
	public void jobFinished(EntityManager entityManager, long jobId,byte[] error, byte[] result) throws Exception{
		JobResult jobResult=entityManager.find(JobResult.class, new Long(jobId));
		jobResult.setEndTime();
		jobResult.setError(error);
		jobResult.setResult(result);
		jobResult.setStatusFinished();
		entityManager.merge(jobResult);
	}
	
	public JobResult getJobResult(long id) throws Exception {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			return entityManager.find(JobResult.class,new Long(id));
		}finally{
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<JobResult> getJobResult(List<Long> id) throws Exception {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{						
			return entityManager.createQuery("SELECT jr FROM "+JobResult.class.getName()+"  jr WHERE jr.id IN :ids")
						        .setParameter("ids", id)
						        .getResultList();
		}finally{
			entityManager.close();
		}
	}

	public JobResult takeJobResult(long id) throws Exception {//TODO wait time out?
		JobResult jobResult=null;
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				jobResult=entityManager.find(JobResult.class, new Long(id));
				if (jobResult!=null){
					jobResult.setStatusArchived();
					entityManager.persist(jobResult);
				}
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
		if (jobResult!=null && jobResultListener!=null){
			jobResultListener.jobResultRemoved(jobResult);
		}
		return jobResult;
	}

	public List<JobResult> takeJobResult(List<Long> ids) throws Exception {//TODO wait time out?
		List<JobResult> jobResults=new ArrayList<JobResult>(ids.size());
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				for (Long id : ids){
					JobResult tmp=entityManager.find(JobResult.class, new Long(id));
					if (tmp!=null){
						tmp.setStatusArchived();
						entityManager.persist(tmp);
					}
					jobResults.add(tmp);
				}
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
		if (jobResultListener!=null){
			jobResults.forEach(it->{
				if (it!=null){
					jobResultListener.jobResultRemoved(it);
				}
			});
		}
		return jobResults;
	}
}