package microsofia.framework.distributed.slave.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.framework.distributed.master.IJobQueue;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.JobResult;
import microsofia.framework.distributed.master.SlaveConfig;
import microsofia.framework.distributed.slave.ISlave;

@Singleton
@Server("fwk")
public abstract class Slave implements Runnable, ISlave{
	private static Log log=LogFactory.getLog(Slave.class);
	protected SlaveConfig slaveConfig;
	protected IMaster master;
	protected IJobQueue jobQueue;
	protected ExecutorService executorService;
	protected AtomicBoolean started;

	public Slave(){
		started=new AtomicBoolean();
	}
	
	@Export
	public abstract void start() throws Exception;

	@Override
	public void startWorker(IMaster master, SlaveConfig slaveConfig) throws Exception {
		this.master=master;
		this.jobQueue=master.getJobQueue();
		this.slaveConfig=slaveConfig;
		
		startWorker();
	}
	
	protected void startWorker() throws Exception{
		started.set(true);
		executorService=Executors.newFixedThreadPool(slaveConfig.getThreadPoolSize());	
		for (int i=0;i<slaveConfig.getThreadPoolSize();i++){
			executorService.submit(this);
		}
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
	public abstract void stop() throws Exception;
	
	@Override
	public SlaveConfig getSlaveConfig() {
		return slaveConfig;
	}
	
	public void run(){
		while (started.get()){
			try{
				Job job=jobQueue.takeJob(slaveConfig.getId());
				if (job!=null){
					jobQueue.jobFinished(job.getId(), null, JobResult.write("Hello world!"));
					//TODO: run and set results

				}else{
					synchronized(this){
						try{
							wait(3000);//TODO param
						}catch(Exception e2){
						}
					}
				}
			}catch(Exception e){
				log.error(e,e);
				synchronized(this){
					try{
						wait(5000);//TODO param
					}catch(Exception e2){
					}
				}
			}
		}
	}
}
