package microsofia.framework.distributed.slave.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.inject.Singleton;
import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.container.module.property.Property;
import microsofia.framework.distributed.master.IJobQueue;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobItem;
import microsofia.framework.distributed.master.SlaveInfo;
import microsofia.framework.distributed.master.VirtualObjectInfo;
import microsofia.framework.distributed.master.impl.ClassMetadata;
import microsofia.framework.distributed.slave.ISlave;
import microsofia.framework.distributed.slave.SlaveInstance;

@Singleton
@Server("fwk")
public class Slave implements Runnable, ISlave{
	private static Log log=LogFactory.getLog(Slave.class);
	@com.google.inject.Inject(optional=true)
	@Property("jobPollingPeriod")
	@Nullable
	protected Long pollPeriod;
	@com.google.inject.Inject(optional=true)
	@Property("errorWaitingTime")
	@Nullable
	protected Long errorWaitingTime;
	@Inject
	@SlaveInstance
	protected Object instance;
	@Inject 
	@Named("ClassMetadata")
	protected ClassMetadata classMetadata;
	protected SlaveInfo slaveInfo;
	protected IMaster master;
	protected IJobQueue jobQueue;
	protected ExecutorService executorService;
	protected AtomicBoolean started;

	public Slave(){
		started=new AtomicBoolean();
	}
	
	private void invoke(String m) throws Exception{
		Method method=null;
		try{
			method=instance.getClass().getMethod(m);
		}catch(Exception e){
			log.debug(e,e);
		}
		if (method!=null){
			method.invoke(instance);
		}
	}
	
	@Export
	public void start() throws Exception{
		if (pollPeriod==null){
			pollPeriod=new Long(1000);
		}
		if (errorWaitingTime==null){
			errorWaitingTime=new Long(60000);
		}
		invoke("start");
	}
	
	public Object invoke(long roid,int method,Object[] arguments) throws Exception{
		VirtualObjectInfo.setCurrentId(roid);
		return classMetadata.getMethod(method).invoke(instance, arguments);
	}

	@Override
	public void startWorker(IMaster master, SlaveInfo slaveInfo) throws Exception {
		this.master=master;
		this.jobQueue=master.getJobQueue();
		this.slaveInfo=slaveInfo;
		
		startWorker();
	}
	
	protected void startWorker() throws Exception{
		started.set(true);
		executorService=Executors.newFixedThreadPool(slaveInfo.getThreadPoolSize());	
		for (int i=0;i<slaveInfo.getThreadPoolSize();i++){
			executorService.submit(this);
		}
	}
	
	@Override
	public void tearDown(long roid,int tearnDownMethod,byte[] tearnDownArguments) throws Exception{
		invoke(roid,tearnDownMethod,(Object[])Job.read(tearnDownArguments));
	}

	@Override
	public void stopWorker() throws Exception{
		started.set(false);
		synchronized(this){
			try{
				notifyAll();
			}catch(Exception e){
			}
		}
		executorService.shutdown();
	}
	
	@Unexport
	public void stop() throws Exception{
		invoke("stop");
	}
	
	@Override
	public SlaveInfo getSlaveInfo() {
		return slaveInfo;
	}
	
	public void run(){
		while (started.get()){
			try{
				JobItem jobItem=jobQueue.takeJob(slaveInfo.getId());
				if (jobItem!=null && started.get()){
					Throwable error=null;
					if (jobItem.getSetupMethod()>0){
						try{
							invoke(jobItem.getJob().getVirtualObjectInfo().getId(),jobItem.getSetupMethod(),jobItem.getSetupArgumentsAsObjects());
						}catch(Throwable th){
							log.error("Error while calling setup for job "+jobItem.getJob()+"\r\n Error:"+th,th);
							error=th;
						}
						if (error!=null){
							jobQueue.jobFailed(jobItem.getJob().getId(), (error!=null ? Job.write(error):null));
						}
					}
					if (error==null){
						Object result=null;
						try{
							result=invoke(jobItem.getJob().getVirtualObjectInfo().getId(),jobItem.getJob().getMethod(),jobItem.getJob().getArgumentsAsObjects());
						}catch(Throwable th){
							th.printStackTrace();
							log.error("Error while processing job "+jobItem.getJob()+"\r\n Error:"+th,th);
							error=th;
						}
						if (error!=null){
							jobQueue.jobFailed(jobItem.getJob().getId(), (error!=null ? Job.write(error):null));
						}else{
							jobQueue.jobSucceeded(jobItem.getJob().getId(), (result!=null ? Job.write(result) :null));
						}
					}

				}else{
					if (started.get()){
						synchronized(this){
							try{
								wait(pollPeriod);
							}catch(Exception e2){
							}
						}
					}
				}
			}catch(Throwable e){
				e.printStackTrace();
				log.error(e,e);
				synchronized(this){
					try{
						wait(errorWaitingTime);
					}catch(Exception e2){
					}
				}
			}
		}
	}
}
