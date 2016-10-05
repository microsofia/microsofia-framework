package microsofia.framework.registry;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="registry")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistryConfiguration {
	@XmlAttribute
	private int port;
	@XmlElementWrapper(name="cluster")
	@XmlElement(name="registry")
	private List<Address> address;
	
	public RegistryConfiguration(){
	}

	public int getPort() {
		return port;
	}

	public void setPort(int localPort) {
		this.port = localPort;
	}

	public List<Address> getAddress() {
		return address;
	}

	public void setAddress(List<Address> address) {
		this.address = address;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Address{
		@XmlAttribute
		private String host;
		@XmlAttribute
		private int port;
		
		public Address(){
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
}
