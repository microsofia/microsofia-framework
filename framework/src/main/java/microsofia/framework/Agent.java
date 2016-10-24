package microsofia.framework;

public interface Agent extends Client{

	@Override
	default public Type getType(){
		return Type.AGENT;
	}
}
