package microsofia.framework.service;

import microsofia.container.module.endpoint.Server;
import java.util.*;

//TODO ILog4jService
@Server
public interface IService {
	
	public ServiceInfo getInfo() throws Exception;;
	
	public Map<String,String> getSystemProperties();
	
	public void setSystemProperty(String name,String value);
	
	public Map<String,String> getEnvProperties();
	
	public void ping();
}
