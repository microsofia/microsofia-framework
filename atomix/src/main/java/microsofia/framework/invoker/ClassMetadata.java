package microsofia.framework.invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClassMetadata {
	//the method by hashcode
	private Map<Integer,Method> methods;
	//the hashcode by method
	private Map<Method, Integer> hashCodes;
	
	public ClassMetadata(Class<?> c){
		methods=new HashMap<>();
		hashCodes=new HashMap<>();
		for (Method m : c.getMethods()){
			int h=getHashCode(m);
			methods.put(h, m);
			hashCodes.put(m, h);
		}
	}

	//computes the hashcode which is simply the hashcode of the string
	//representing its signature
	private int getHashCode(Method m){
		String s=m.getName();
		if (m.getParameterTypes()!=null){
			for (Class<?> c : m.getParameterTypes()){
				s+=c.getName();
			}
		}
		return s.hashCode();
	}

	//returns the hashcode of a method
	public int hashCode(Method m){
		return hashCodes.get(m);
	}
	
	//returns a method for a given hashcode
	public Method getMethod(int i){
		return methods.get(i);
	}
}
