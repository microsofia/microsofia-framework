package microsofia.framework;

public interface Client extends Service{

	public Class<?> getServiceClass();
	
	public String getImplementation();
	
	@Override
	default public Type getType(){
		return Type.CLIENT;
	}
}
