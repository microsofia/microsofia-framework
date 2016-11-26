package microsofia.framework.distributed.sample;

public class Sample implements ISample{
	
	public Sample(){
	}

	@Override
	public String helloWorld(String s) throws Exception{
		return s;
	}
	
	@Override
	public String asyncHelloWorld(String s) throws Exception{
		Thread.sleep(10000);
		return s;
	}
}
