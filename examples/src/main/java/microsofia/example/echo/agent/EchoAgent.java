package microsofia.example.echo.agent;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import microsofia.framework.Agent;
import microsofia.framework.agent.AgentService;

@Singleton
public class EchoAgent implements Agent {
	@Inject
	private AgentService agentService;
	@Inject
	private Echo echo;
	
	public EchoAgent(){
	}

	@Override
	public Class<?> getServiceClass(){
		return Echo.class;
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
		return Arrays.asList(Echo.class,HelloWorld.class);
	}

	@Override
	public void start() throws Exception {
		echo.start();
		agentService.start();
	}

	@Override
	public void stop() throws Exception {
		echo.stop();
		agentService.stop();
	}
}
