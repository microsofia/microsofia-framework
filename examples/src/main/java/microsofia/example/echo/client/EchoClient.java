package microsofia.example.echo.client;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;

import microsofia.framework.Client;
import microsofia.framework.client.ClientService;

@Singleton
public class EchoClient implements Client {
	@Inject
	private ClientService clientService;
	@Inject
	private EchoTest echoTest;
	
	public EchoClient(){
	}
	
	@Override
	public String getImplementation() {
		return "echo";
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
		clientService.start();
		echoTest.test();
	}

	@Override
	public void stop() throws Exception {
		clientService.stop();
	}

	@Override
	public Class<?> getServiceClass() {
		return EchoTest.class;
	}
}
