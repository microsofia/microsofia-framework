package microsofia.example.echo.agent;

import javax.inject.Inject;
import javax.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.example.echo.client.IEcho;
import microsofia.example.echo.client.IHelloWorld;

@Singleton
@Server("fwk")
public class Echo implements IEcho{
	@Inject
	@Singleton
	private HelloWorld helloWorld;

	public Echo() {
	}
	
	@Override
	public IHelloWorld getHelloWorld() {
		return helloWorld;
	}

	@Override
	public String echo(String message) {
		return message;
	}

	@Export
	public void start(){
		helloWorld.start();
	}
	
	@Unexport
	public void stop(){
		helloWorld.stop();
	}
}
