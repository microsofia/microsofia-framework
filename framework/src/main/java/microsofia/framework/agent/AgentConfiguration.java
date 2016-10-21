package microsofia.framework.agent;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

import microsofia.framework.client.ClientConfiguration;

@Singleton
@XmlRootElement(name="agent")
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentConfiguration extends ClientConfiguration{
	@XmlElement(name="queue")
	private String queue;
	@XmlElement(name="lookup")
	private AgentLookupConfiguration lookupConfiguration;
	
	public AgentConfiguration(){
	}
	
	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public AgentLookupConfiguration getLookupConfiguration() {
		return lookupConfiguration;
	}

	public void setLookupConfiguration(AgentLookupConfiguration lookupConfiguration) {
		this.lookupConfiguration = lookupConfiguration;
	}
	
	private static JAXBContext jaxbContext=null;
	static{
		try{
			jaxbContext=JAXBContext.newInstance(AgentConfiguration.class);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static AgentConfiguration createAgentConfiguration(Element[] element) throws Exception{
		Unmarshaller unmarshaller=jaxbContext.createUnmarshaller();
		if (element!=null){
			for (Element e : element){
				AgentConfiguration conf=(AgentConfiguration)unmarshaller.unmarshal(e);
				if (conf!=null){
					return conf;
				}
			}
		}
		return null;
	}
}
