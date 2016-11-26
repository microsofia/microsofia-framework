package microsofia.example.echo.client;

import javax.inject.Inject;
import javax.inject.Singleton;

import microsofia.framework.client.ClientService;

@Singleton
public class EchoTest {
	@Inject
	private ClientService clientService;
	private IEcho echo;
	private IHelloWorld helloWorld;
	
	public EchoTest(){
	}

	public void test() throws Exception{
		testEcho();
		testHelloWorld();
	}
	
	public void testEcho() throws Exception{
		echo=clientService.getClientLookupService().searchAgent(IEcho.class, "echo_name","echo_group", 1);
		helloWorld=echo.getHelloWorld();
		System.out.println(echo.echo("Hello world!"));
	}
	
	public void testHelloWorld() throws Exception{
		System.out.println(helloWorld.getMessage());
	}
}
