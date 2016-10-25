package microsofia.framework.distributed.master.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.inject.Singleton;

import microsofia.container.module.db.jpa.JPA;
import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.framework.client.lookup.IClientLookupService;
import microsofia.framework.distributed.master.ISlaveConfigurator;
import microsofia.framework.distributed.master.SlaveConfig;
import microsofia.framework.distributed.master.SlaveConfig.Status;
import microsofia.framework.distributed.slave.ISlave;

@Singleton
@Server("fwk")
public class SlaveConfigurator implements ISlaveConfigurator{
	@Inject
	private Master master;
	@Inject
	private IClientLookupService clientLookupService;
	@Inject
	@JPA("master")
	private EntityManagerFactory entityManagerFactory;
	private Map<Long,ISlave> slaves;

	public SlaveConfigurator(){
		slaves=new Hashtable<>();
	}
	
	@Export
	public void start(){
	}
	
	@Unexport
	public void stop(){

	}
	
	@Override
	public long addSlave(SlaveConfig slaveConfig) {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				slaveConfig.setStatus(Status.STOPPED);
				
				entityManager.persist(slaveConfig);
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
		return slaveConfig.getId();
	}

	@Override
	public void updateSlave(SlaveConfig slaveConfig) {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				entityManager.merge(slaveConfig);
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
	}

	@Override
	public void removeSlave(long id) {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				entityManager.remove(entityManager.getReference(SlaveConfig.class, new Long(id)));
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
	}
	
	@Override
	public SlaveConfig getSlaveConfig(long id){
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			return entityManager.find(SlaveConfig.class,new Long(id));
		}finally{
			entityManager.close();
		}
	}

	@Override
	public List<SlaveConfig> getSlaveConfig(){
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			CriteriaQuery<SlaveConfig> query=entityManager.getCriteriaBuilder().createQuery(SlaveConfig.class);
			Root<SlaveConfig> root=query.from(SlaveConfig.class);
			query.select(root);
			
			return entityManager.createQuery(query).getResultList();
		}finally{
			entityManager.close();
		}
	}

	private void setSlaveStatus(Long id,Status status) {
		EntityManager entityManager=entityManagerFactory.createEntityManager();
		try{
			entityManager.getTransaction().begin();
			try{
				SlaveConfig slaveConfig=entityManager.getReference(SlaveConfig.class, new Long(id));
				slaveConfig.setStatus(status);
				entityManager.merge(slaveConfig);
				entityManager.getTransaction().commit();
			}catch(Throwable th){
				entityManager.getTransaction().rollback();
				throw th;
			}
		}finally{
			entityManager.close();
		}
	}
	
	@Override
	public void startSlave(long id) throws Exception{
		SlaveConfig slaveConfig=getSlaveConfig(id);
		ISlave slave=slaves.get(id);
		if (slave!=null){
			throw new IllegalStateException("Slave "+slaveConfig+" is already started with remote proxy "+slave);
		}

		slave=clientLookupService.searchAgent(ISlave.class, slaveConfig.getQueue());
		slave.startWorker(master, slaveConfig);
		slaves.put(id, slave);
		setSlaveStatus(id,Status.STARTED);
	}
	
	@Override
	public void startSlave(List<Long> id) throws Exception{
		for (Long i : id){
			startSlave(i);//TODO use executorservice
		}
	}

	@Override
	public void startAllSlave() throws Exception{
		for (SlaveConfig sc : getSlaveConfig()){
			startSlave(sc.getId());//TODO use executorservice
		}
	}

	@Override
	public void stopSlave(long id) throws Exception{
		ISlave slave=slaves.remove(id);
		if (slave!=null){
			slave.stopWorker();
			clientLookupService.freeAgent(slave);
			setSlaveStatus(id,Status.STOPPED);
		}
	}
	
	@Override
	public void stopSlave(List<Long> id) throws Exception{
		for (Long i : id){
			stopSlave(i);//TODO use executorservice
		}
	}

	@Override
	public void stopAllSlave() throws Exception{
		for (SlaveConfig sc : getSlaveConfig()){
			stopSlave(sc.getId());//TODO use executorservice
		}
	}
	
	@Override
	public ISlave getSlave(long id){
		return slaves.get(id);
	}

	public List<ISlave> getSlave(){
		return new ArrayList<ISlave>(slaves.values());
	}
}
