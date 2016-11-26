package microsofia.framework.distributed.master.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.framework.client.lookup.IClientLookupService;
import microsofia.framework.distributed.master.ISlaveConfigurator;
import microsofia.framework.distributed.master.SlaveInfo;
import microsofia.framework.distributed.master.SlaveInfo.Status;
import microsofia.framework.distributed.master.dao.DataAccess;
import microsofia.framework.distributed.master.dao.ThrowableFunction;
import microsofia.framework.distributed.slave.ISlave;

@Singleton
@Server("fwk")
@Export
public class SlaveConfigurator implements ISlaveConfigurator{
	private static Log log=LogFactory.getLog(SlaveConfigurator.class);
	@Inject
	private Master master;
	@Inject
	private IClientLookupService clientLookupService;
	@Inject
	private DataAccess dataAccess;
	@Inject
	@Named("master")
	private ExecutorService executorService;
	private Map<Long,ISlave> slaves;

	public SlaveConfigurator(){
		slaves=new Hashtable<>();
	}
	
	public void start() throws Exception{
		List<SlaveInfo> slaveInfos=getSlaveInfo();

		List<Long> ids=slaveInfos.stream()
			.filter(it->{
				return it.getStatus().equals(Status.STARTED);
			})
			.map(SlaveInfo::getId)
			.collect(Collectors.toList());
		
		startSlave(ids);
	}
	
	public void stop(){
		List<ISlave> allSlaves=new ArrayList<ISlave>(slaves.values());
		slaves.clear();
		allSlaves.forEach(it->{
			try{
				it.stopWorker();
			}catch(Exception e){
				log.error(e,e);
			}
			try{
				clientLookupService.freeAgent(it);
			}catch(Exception e){
				log.error(e,e);
			}
		});
	}
	
	@Override
	public long addSlave(SlaveInfo slaveInfo) throws Exception{
		return dataAccess.write(it->{
			slaveInfo.setStatus(Status.STOPPED);
			it.persist(slaveInfo);			
			return slaveInfo.getId();
		});
	}

	@Override
	public void setSlavePoolSize(long slaveId,int poolSize) throws Exception{
		dataAccess.write(it->{
			SlaveInfo slaveInfo=it.getReference(SlaveInfo.class, new Long(slaveId));
			slaveInfo.setThreadPoolSize(poolSize);
			it.merge(slaveInfo);
			return null;
		});
	}
	
	@Override
	public void setSlaveNameAndGroup(long slaveId,String name,String group) throws Exception{
		dataAccess.write(it->{
			SlaveInfo slaveInfo=it.getReference(SlaveInfo.class, new Long(slaveId));
			slaveInfo.setName(name);
			slaveInfo.setGroup(group);
			it.merge(slaveInfo);
			return null;
		});
	}

	@Override
	public void removeSlave(long id) throws Exception{
		dataAccess.write(it->{
			it.remove(it.getReference(SlaveInfo.class, new Long(id)));
			return null;
		});
	}
	
	@Override
	public SlaveInfo getSlaveInfo(long id) throws Exception{
		return dataAccess.read(it->{
			return it.find(SlaveInfo.class,new Long(id));
		});
	}

	@Override
	public List<SlaveInfo> getSlaveInfo() throws Exception{
		return dataAccess.read(it->{
			CriteriaQuery<SlaveInfo> query=it.getCriteriaBuilder().createQuery(SlaveInfo.class);
			Root<SlaveInfo> root=query.from(SlaveInfo.class);
			query.select(root);
			
			return it.createQuery(query).getResultList();
		});
	}

	private void setSlaveStatus(Long id,Status status) throws Exception{
		dataAccess.write(it->{
			SlaveInfo slaveInfo=it.getReference(SlaveInfo.class, new Long(id));
			slaveInfo.setStatus(status);
			it.merge(slaveInfo);
			return null;
		});
	}
	
	private <E> void forAll(List<E> es, ThrowableFunction<E,Void> function) throws Exception{
		Exception lastTh=null;
		List<Future<Exception>> futures=new ArrayList<>();
		for (E e : es){
			Future<Exception> future=executorService.submit((Callable<Exception>)()->{
				try{
					function.apply(e);
					return null;
				}catch(Exception th){
					log.error(th,th);
					return th;
				}
			});

			futures.add(future);
		}
		for (Future<Exception> f : futures){
			try{
				Exception th2=f.get();
				if (th2!=null){
					lastTh=th2;
				}
			}catch(Exception e2){
				lastTh=e2;
				log.error(e2,e2);
			}
		}
		if (lastTh!=null){
			throw lastTh;//should throw one containing all of them
		}
	}
	
	@Override
	public void startSlave(long id) throws Exception{
		SlaveInfo slaveInfo=getSlaveInfo(id);
		ISlave slave=slaves.get(id);
		if (slave!=null){
			throw new IllegalStateException("Slave "+slaveInfo+" is already started with remote proxy "+slave);
		}

		slave=clientLookupService.searchAgent(ISlave.class, slaveInfo.getName(),slaveInfo.getGroup());
		slave.startWorker(master, slaveInfo);
		slaves.put(id, slave);
		setSlaveStatus(id,Status.STARTED);
	}
	
	@Override
	public void startSlave(List<Long> id) throws Exception{
		forAll(id,(Long it)->{
			startSlave(it);
			return null;
		});
	}

	@Override
	public void startAllSlave() throws Exception{
		forAll(getSlaveInfo(), (SlaveInfo it)->{
			startSlave(it.getId());
			return null;
		});
	}

	@Override
	public void stopSlave(long id) throws Exception{
		ISlave slave=slaves.remove(id);
		if (slave!=null){
			try{
				slave.stopWorker();
			}catch(Exception e){
				log.error(e,e);
			}
			try{
				clientLookupService.freeAgent(slave);
			}catch(Exception e){
				log.error(e,e);
			}
			setSlaveStatus(id,Status.STOPPED);
		}
	}
	
	@Override
	public void stopSlave(List<Long> id) throws Exception{
		forAll(id, (Long it)->{
			stopSlave(it);
			return null;
		});
	}

	@Override
	public void stopAllSlave() throws Exception{
		forAll(getSlaveInfo(), (SlaveInfo it)->{
			stopSlave(it.getId());
			return null;
		});
	}
	
	@Override
	public ISlave getSlave(long id){
		return slaves.get(id);
	}

	@Override
	public List<ISlave> getSlave(){
		return new ArrayList<ISlave>(slaves.values());
	}
}
