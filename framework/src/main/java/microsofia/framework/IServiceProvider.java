package microsofia.framework;

import java.util.List;

import com.google.inject.AbstractModule;

public interface IServiceProvider {
	public enum Type{CLIENT, AGENT, REGISTRY};

	public Type getType();
	
	public String getImplementation();
	
	public List<AbstractModule> getGuiceModules();
	
	public List<Class<?>> getInjectedClasses();
	
	public void start() throws Exception;
	
	public void stop() throws Exception;
}