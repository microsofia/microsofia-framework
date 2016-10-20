package microsofia.framework;

public interface Client extends Service{

	@Override
	default public Type getType(){
		return Type.CLIENT;
	}
}
