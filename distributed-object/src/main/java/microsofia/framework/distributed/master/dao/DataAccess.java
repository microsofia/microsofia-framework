package microsofia.framework.distributed.master.dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import microsofia.container.module.db.jpa.Entities;
import microsofia.container.module.db.jpa.JPA;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobResult;
import microsofia.framework.distributed.master.VirtualObjectInfo;
import microsofia.framework.distributed.master.SlaveInfo;

@Singleton
public class DataAccess {
	@Inject
	@Entities({Job.class,JobResult.class,VirtualObjectInfo.class,SlaveInfo.class})
	@JPA("master")
	private EntityManagerFactory entityManagerFactory;
	
	public DataAccess(){
	}
	
	public <R> R write(ThrowableFunction<EntityManager,R> th) throws Exception{
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				R r=th.apply(entityManager);
				entityManager.getTransaction().commit();
				return r;
			}catch(Exception th2){
				entityManager.getTransaction().rollback();
				throw th2;
			}
		}finally{
			entityManager.close();
		}
	}

	public <R> R read(ThrowableFunction<EntityManager,R> th) throws Exception{
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			return th.apply(entityManager);
		}finally{
			entityManager.close();
		}
	}
}
