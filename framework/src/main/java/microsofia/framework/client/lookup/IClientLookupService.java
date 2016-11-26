package microsofia.framework.client.lookup;

public interface IClientLookupService {

	public Object searchAgent(String name,String group) throws Exception;
	
	public <A> A searchAgent(Class<A> ca,String name,String group) throws Exception;
	
	public <A> A searchAgent(Class<A> ca,String name,String group,int weigth) throws Exception;
	
	public Object searchAgent(String name,String group,int weigth) throws Exception;
	
	public void freeAgent(Object proxy) throws Exception;
}
