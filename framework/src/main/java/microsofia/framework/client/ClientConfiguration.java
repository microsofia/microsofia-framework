package microsofia.framework.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

import microsofia.container.application.PropertyConfig;

@XmlRootElement(name="client")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClientConfiguration{
	@XmlElementWrapper(name="registries")
	@XmlElement(name="registry")
	protected List<Registry> registry;
	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	private List<PropertyConfig> properties;
	
	public ClientConfiguration(){
		properties=new ArrayList<>();
	}

	public List<Registry> getRegistry() {
		return registry;
	}

	public void setRegistry(List<Registry> registry) {
		this.registry = registry;
	}
	
	public List<PropertyConfig> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyConfig> properties) {
		this.properties = properties;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Registry{
		@XmlAttribute
		private String host;
		@XmlAttribute
		private int port;
		
		public Registry(){
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}
	
	private static JAXBContext jaxbContext=null;
	static{
		try{
			jaxbContext=JAXBContext.newInstance(ClientConfiguration.class);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static ClientConfiguration createClientConfiguration(Element[] element) throws Exception{
		Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
		if (element!=null){
			for (Element e : element){
				ClientConfiguration conf=(ClientConfiguration)unmarshaller.unmarshal(e);
				if (conf!=null){
					return conf;
				}
			}
		}
		return null;
	}
}