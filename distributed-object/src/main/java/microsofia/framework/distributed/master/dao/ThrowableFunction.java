package microsofia.framework.distributed.master.dao;

@FunctionalInterface
public interface ThrowableFunction<E,R> {
	public R apply(E e) throws Exception;
}