package microsofia.example.echo.client;

import microsofia.container.module.endpoint.Server;

@Server
public interface IHelloWorld {

	public String getMessage();
}
