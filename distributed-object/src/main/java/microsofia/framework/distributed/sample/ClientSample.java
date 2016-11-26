package microsofia.framework.distributed.sample;

import java.util.List;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.inject.AbstractModule;
import microsofia.framework.Client;
import microsofia.framework.client.ClientService;
import microsofia.framework.distributed.master.IMaster;
import microsofia.framework.distributed.master.SlaveInfo;
import microsofia.framework.distributed.master.proxy.ProxyBuilder;

@Singleton
public class ClientSample implements Client {
	@Inject
	private ClientService clientService;
	@Inject
	private ProxyBuilder proxyBuilder;
	
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
		proxyBuilder.connect("dosample_name","dosample_group");
		IMaster master=proxyBuilder.getMaster();
		System.out.println("master=="+master);

		SlaveInfo slaveConfig=new SlaveInfo();
		slaveConfig.setThreadPoolSize(1);
		slaveConfig.setName("doslave_name");
		slaveConfig.setGroup("doslave_group");
		long id=master.getSlaveConfigurator().addSlave(slaveConfig);
		System.out.println("Slave added id="+id);
		
		System.out.println("Slaves: "+master.getSlaveConfigurator().getSlaveInfo());
		
		master.getSlaveConfigurator().startSlave(id);
		
		System.out.println("After slave start: "+master.getSlaveConfigurator().getSlaveInfo());
		try{
			IProxySample sample=proxyBuilder.getProxy(IProxySample.class);
			String answer=sample.helloWorld("hello world!");
			System.out.println("result=="+answer);
			
			Future<String> future=sample.asyncHelloWorld("Async Hello world!");
			System.out.println("before calling future and blocking");
			System.out.println("async result=="+future.get());
			
			master.getSlaveConfigurator().setSlavePoolSize(id, 5);
			System.out.println("After slave update: "+master.getSlaveConfigurator().getSlaveInfo());
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
