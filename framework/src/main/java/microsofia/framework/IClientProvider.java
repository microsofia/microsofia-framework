package microsofia.framework;

public interface IClientProvider extends IServiceProvider{

	@Override
	default public Type getType(){
		return Type.CLIENT;
	}
}
