package microsofia.framework;

public interface IAgentProvider extends IServiceProvider{

	@Override
	default public Type getType(){
		return Type.AGENT;
	}
}
