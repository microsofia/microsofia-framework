package microsofia.framework;

public interface Agent extends Service{

	public Class<?> getServiceClass();
	
	@Override
	default public Type getType(){
		return Type.AGENT;
	}
}
