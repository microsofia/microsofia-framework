package microsofia.framework.service;

import microsofia.container.module.endpoint.Export;
import microsofia.container.module.endpoint.Server;

@Server("fwk")
public class Service implements IService{

	public Service(){
	}

	@Override
	public void ping() {
	}

	@Export
	public void export(){
		System.out.println("Service exported.");
	}
}
