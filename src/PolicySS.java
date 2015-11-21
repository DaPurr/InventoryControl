/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Represents the reordering policy for a certain material.
 *
 */
public class PolicySS extends ReorderPolicy {
	
	public PolicySS(int minStock, int maxStock) {
		super(minStock, maxStock);
	}
	
	public PolicySS(int currentStock, int minStock, int maxStock) {
		super(currentStock, minStock, maxStock);
	}
	
	@Override
	public boolean doReorder() {
		return inventoryPostion() <= reorderPoint();
	}

	@Override
	public int reorder() {
		if (doReorder())
			return maxStock() - inventoryPostion();
		throw new IllegalStateException("Inventory position not below minimum allowed stock.");
	}

}
