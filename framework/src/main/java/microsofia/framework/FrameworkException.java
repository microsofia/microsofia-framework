package microsofia.framework;

public class FrameworkException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public FrameworkException(String m){
		super(m);
	}

	public FrameworkException(String m,Throwable th){
		super(m,th);
	}
}