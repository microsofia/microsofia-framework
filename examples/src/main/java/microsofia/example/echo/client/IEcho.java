package microsofia.example.echo.client;

import microsofia.container.module.endpoint.Server;

@Server
public interface IEcho {

	public IHelloWorld getHelloWorld();
	
	public String echo(String message);
}
