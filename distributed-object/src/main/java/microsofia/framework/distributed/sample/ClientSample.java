package microsofia.framework.distributed.sample;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;

import microsofia.framework.Client;
import microsofia.framework.client.ClientService;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.IRemoteObject;
import microsofia.framework.distributed.master.Job;
import microsofia.framework.distributed.master.SlaveConfig;

@Singleton
public class ClientSample implements Client {
	@Inject
	private ClientService clientService;
	
	public ClientSample(){
	}
	
	@Override
	public String getImplementation() {
		return "do.sample.client";
	}

	@Override
	public List<AbstractModule> getGuiceModules() {
		return null;
	}

	@Override
	public List<Class<?>> getInjectedClasses() {
		return null;
	}

	@Override
	public void start() throws Exception {
		try{
			Thread.sleep(5000);
			clientService.start();
			System.out.println("Registries:"+clientService.getRegistries());
			System.out.println("Agents:"+clientService.getRegistries().get(0).getAgents());
			clientService.getRegistries().get(0).getAgents().forEach(it->{
				try{
					System.out.println(it.getInfo());
				}catch(Exception e){
					e.printStackTrace();
				}
			});
			IMaster master=clientService.getClientLookupService().searchAgent(IMaster.class, "dosample");
			System.out.println("master=="+master);
	
			SlaveConfig slaveConfig=new SlaveConfig();
			slaveConfig.setThreadPoolSize(1);
			slaveConfig.setQueue("doslave");
			long id=master.getSlaveConfigurator().addSlave(slaveConfig);
			System.out.println("Slave added id="+id);
			
			System.out.println("Slaves: "+master.getSlaveConfigurator().getSlaveConfig());
			
			master.getSlaveConfigurator().startSlave(id);
			
			System.out.println("After slave start: "+master.getSlaveConfigurator().getSlaveConfig());
			
			IRemoteObject stateLessRemoteObject=master.getObjectAllocator().getStateLessRemoteObject();
			Job job=new Job();
			long ljobid=stateLessRemoteObject.addJob(job);
			
			System.out.println("Job inserted: "+stateLessRemoteObject.getJob(ljobid));
			
			slaveConfig.setId(id);
			slaveConfig.setThreadPoolSize(5);
			master.getSlaveConfigurator().updateSlave(slaveConfig);
			System.out.println("After slave update: "+master.getSlaveConfigurator().getSlaveConfig());
			master.getSlaveConfigurator().removeSlave(id);
			System.out.println("Slave removal: "+master.getSlaveConfigurator().getSlaveConfig());

		}catch(Throwable th){
			th.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		clientService.stop();
	}

	@Override
	public Class<?> getServiceClass() {
		return null;
	}
}
