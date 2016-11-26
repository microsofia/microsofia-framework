package microsofia.framework.distributed.sample;

import java.util.concurrent.Future;

public interface IProxySample {

	public String helloWorld(String s) throws Exception;

	public Future<String> asyncHelloWorld(String s) throws Exception;
}
