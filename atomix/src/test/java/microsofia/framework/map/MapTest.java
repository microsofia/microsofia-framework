package microsofia.framework.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.atomix.Atomix;
import io.atomix.AtomixClient;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.storage.Storage;

public class MapTest {
	protected AtomixReplica atomixReplica1;
	protected Map<Long,String> map1;
	protected Map<ServiceAddress,ServiceAddress> map12;
	protected AtomixReplica atomixReplica2;
	protected Map<Long,String> map2;
	protected Map<ServiceAddress,ServiceAddress> map22;
	protected AtomixReplica atomixReplica3;
	protected Map<Long,String> map3;
	protected AtomixClient atomixClient;
	protected Map<Long,String> map4;
	protected Map<ServiceAddress,ServiceAddress> map32;
	
	@Before
	public void setup() throws Exception{
		List<Address> adr=new ArrayList<>();
		for (int i=9970;i<9973;i++){
			adr.add(new Address("localhost", i));
		}
		
		@SuppressWarnings("unchecked")
		AtomixReplica.Builder builder1=AtomixReplica.builder(new Address("localhost",9970))
												   .withStorage(new Storage("logs/"+9970))
												   .withResourceTypes((Class)Map.class);
		atomixReplica1=builder1.build();
		atomixReplica1.serializer().register(ServiceAddress.class,1986);
		CompletableFuture<AtomixReplica> f1=atomixReplica1.bootstrap(adr);
		
		@SuppressWarnings("unchecked")
		AtomixReplica.Builder builder2=AtomixReplica.builder(new Address("localhost",9971))
				   .withStorage(new Storage("logs/"+9971))
				   .withResourceTypes((Class)Map.class);
		atomixReplica2=builder2.build();
		atomixReplica2.serializer().register(ServiceAddress.class,1986);
		CompletableFuture<AtomixReplica> f2=atomixReplica2.bootstrap(adr);

		@SuppressWarnings("unchecked")
		AtomixReplica.Builder builder3=AtomixReplica.builder(new Address("localhost",9972))
				.withStorage(new Storage("logs/"+9972))
				.withResourceTypes((Class)Map.class);
		atomixReplica3=builder3.build();
		atomixReplica3.serializer().register(ServiceAddress.class,1986);
		CompletableFuture<AtomixReplica> f3=atomixReplica3.bootstrap(adr);
		
		@SuppressWarnings("unchecked")
		AtomixClient.Builder builder4=AtomixClient.builder().withResourceTypes((Class)Map.class);
		atomixClient=builder4.build();
		atomixClient.serializer().register(ServiceAddress.class,1986);
		CompletableFuture<Atomix> f4=atomixClient.connect(adr);
		
		f1.get();
		f2.get();
		f3.get();
		f4.get();
		
		map1=atomixReplica1.getResource("map",Map.class).get();
		map2=atomixReplica1.getResource("map",Map.class).get();
		map3=atomixReplica1.getResource("map",Map.class).get();
		map4=atomixClient.getResource("map",Map.class).get();
		
		map12=atomixReplica1.getResource("mapsa",Map.class).get();
		map22=atomixReplica1.getResource("mapsa",Map.class).get();
		map32=atomixReplica1.getResource("mapsa",Map.class).get();
	}

	@Test
	public void testSimplePut() throws Exception{
		map1.put(new Long(1978), "charbel").get();
		map2.put(new Long(1979), "charbel+1").get();
		map3.put(new Long(1980), "charbel+2").get();
		
		Assert.assertEquals(map1.get(new Long(1978)).get(),"charbel");
		Assert.assertEquals(map1.get(new Long(1979)).get(),"charbel+1");
		Assert.assertEquals(map1.get(new Long(1980)).get(),"charbel+2");
		Assert.assertEquals(map1.size().get().intValue(),3);
		
		Set<String> values=new HashSet<>();
		values.add("charbel");
		values.add("charbel+1");
		values.add("charbel+2");
		for (Object o : map1.values().get()){
			Assert.assertTrue("Map doesnt contain "+o,values.contains(o));
		}		
	}
	
	@Test
	public void testDisconnection() throws Exception{
		map4.put(new Long(2014), "sofia").get();
		Assert.assertEquals(map1.get(new Long(2014)).get(),"sofia");
		
		atomixClient.close().get();
		Thread.sleep(5000);
		Assert.assertNull("Value was not removed from context after client disconnection.",map1.get(new Long(2014)).get());
	}
	
	@Test
	public void testComplexPut() throws Exception{
		Set<ServiceAddress> allSA=new HashSet<>();
		
		for (int i=0;i<10;i++){
			ServiceAddress sa1=generateServiceAddress();
			allSA.add(sa1);
			map12.put(sa1, sa1).get();
		}
		
		for (ServiceAddress sa : map12.values().get()){
			Assert.assertTrue("Map doesnt contain "+sa,allSA.contains(sa));
		}		
	}
	
	@After
	public void tearDown() throws Exception{
		atomixClient.close().get();
		CompletableFuture<Void> f1=atomixReplica1.leave().thenRun(atomixReplica1::shutdown);
		CompletableFuture<Void> f2=atomixReplica2.leave().thenRun(atomixReplica2::shutdown);
		CompletableFuture<Void> f3=atomixReplica3.leave().thenRun(atomixReplica3::shutdown);

		f1.get();
		f2.get();
		f3.get();
	}

	public static ServiceAddress generateServiceAddress(){
		ServiceAddress sa=new ServiceAddress();
		sa.host="localhost";
		sa.port=9996;
		sa.id=""+System.currentTimeMillis();
		sa.interfaces=new Class[]{Serializable.class, List.class,Collection.class};
		return sa;
	}
	
	public static class ServiceAddress implements CatalystSerializable{
		private static final long serialVersionUID = 0L;
		public String id;
		public String host;
		public int port;
		public Class<?>[] interfaces;

		public ServiceAddress(){
		}

		public void writeObject(BufferOutput buffer, Serializer serializer) {		
	    	buffer.writeString(id);
			buffer.writeString(host);
			buffer.writeInt(port);
			buffer.writeInt(interfaces.length);
			for (Class<?> c : interfaces){
				buffer.writeString(c.getName());
			}
	    }

	    @Override
	    public void readObject(BufferInput buffer, Serializer serializer) {
	    	id=buffer.readString();
	    	
	    	host=buffer.readString();
	    	port=buffer.readInt();

	    	List<Class<?>> interfaces=new ArrayList<>();

	    	int l=buffer.readInt();
	    	for (int i=0;i<l;i++){
	    		try {
					interfaces.add(Class.forName(buffer.readString()));
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	this.interfaces=interfaces.toArray(new Class<?>[0]);
	    }
	    
	    @Override
	    public int hashCode(){
	    	return id.hashCode();
	    }
	    
	    @Override
	    public boolean equals(Object o){
	    	if (!(o instanceof ServiceAddress)){
	    		return false;
	    	}
	    	return ((ServiceAddress)o).id.equals(id);
	    }
	    
	    @Override
	    public String toString(){
	    	return "Service[Id:"+id+"]";
	    }
	}
}
