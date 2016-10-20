package microsofia.framework;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.AbstractModule;

import microsofia.framework.agent.AgentService;

public class TestAgent implements Agent {
	@Inject
	private AgentService agentService;
	
	public TestAgent(){
	}

	@Override
	public String getImplementation() {
		return "testagent";
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
		agentService.start();
	}

	@Override
	public void stop() throws Exception {
		agentService.stop();
	}
}
