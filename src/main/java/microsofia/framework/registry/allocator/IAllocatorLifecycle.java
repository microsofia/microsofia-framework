package microsofia.framework.registry.allocator;

public interface IAllocatorLifecycle {

	public void startAllocating();
	
	public AllocationResponse allocate(AllocationRequest request);
	
	public void stopAllocating();
}
