package microsofia.framework.client.lookup;

public interface IClientLookupService {

	public Object searchAgent(String queue) throws Exception;
	
	public <A> A searchAgent(Class<A> ca,String queue) throws Exception;
	
	public <A> A searchAgent(Class<A> ca,String queue,int weigth) throws Exception;
	
	public Object searchAgent(String queue,int weigth) throws Exception;
	
	public void freeAgent(Object proxy) throws Exception;
}
