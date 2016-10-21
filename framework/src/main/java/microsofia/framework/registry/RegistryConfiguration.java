package microsofia.framework.registry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
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

@Singleton
@XmlRootElement(name="registry")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistryConfiguration {
	@XmlAttribute
	private String host;
	@XmlAttribute
	private int port;
	@XmlElementWrapper(name="members")
	@XmlElement(name="member")
	private List<Member> member;
	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	private List<PropertyConfig> properties;
	
	public RegistryConfiguration(){
		properties=new ArrayList<>();
	}
	
	public String getHost(){
		return host;
	}
	
	public void setHost(String s){
		host=s;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int localPort) {
		this.port = localPort;
	}

	public List<Member> getMember() {
		return member;
	}

	public void setMember(List<Member> member) {
		this.member = member;
	}
	
	public List<PropertyConfig> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyConfig> properties) {
		this.properties = properties;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Member{
		@XmlAttribute
		private String host;
		@XmlAttribute
		private int port;
		
		public Member(){
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
			jaxbContext=JAXBContext.newInstance(RegistryConfiguration.class);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static RegistryConfiguration readFrom(Element[] element) throws Exception{
		Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
		if (element!=null){
			for (Element e : element){
				RegistryConfiguration conf=(RegistryConfiguration)unmarshaller.unmarshal(e);
				if (conf!=null){
					return conf;
				}
			}
		}
		return null;
	}
}
