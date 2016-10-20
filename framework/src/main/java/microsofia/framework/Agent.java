package microsofia.framework;

public interface Agent extends Service{

	@Override
	default public Type getType(){
		return Type.AGENT;
	}
}
