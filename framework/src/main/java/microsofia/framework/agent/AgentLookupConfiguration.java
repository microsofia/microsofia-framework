package microsofia.framework.agent;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="lookup")
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentLookupConfiguration implements Externalizable{
	public enum Multiplicity{one, one_per_request, one_or_n};
	@XmlElement(name="multiplicity")
	private Multiplicity multiplicity;
	@XmlElement(name="weigth")
	private int weigth;

	public AgentLookupConfiguration(){
		multiplicity=Multiplicity.one;
		weigth=1;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}

	public int getWeigth() {
		return weigth;
	}

	public void setWeigth(int weigth) {
		this.weigth = weigth;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(multiplicity);
		out.writeInt(weigth);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		multiplicity=(Multiplicity)in.readObject();
		weigth=in.readInt();
	}
	
	@Override
	public String toString(){
		return "[Multiplicity:"+multiplicity+"][Weigth:"+weigth+"]";
	}
}