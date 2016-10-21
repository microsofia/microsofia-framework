package microsofia.example.echo.agent;

import javax.inject.Singleton;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;
import microsofia.container.module.endpoint.Unexport;
import microsofia.example.echo.client.IHelloWorld;

@Singleton
@Server("fwk")
public class HelloWorld implements IHelloWorld{
	
	public HelloWorld(){
	}

	@Override
	public String getMessage() {
		return "Hello World!";
	}
	
	@Export
	public void start(){
	}
	
	@Unexport
	public void stop(){
	}
}
