/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Represents the (R,Q) policy.
 *
 */
public class PolicyRQ extends ReorderPolicy {

	private int quantity;
	
	public PolicyRQ(int minStock, int quantity) {
		super(minStock, minStock + quantity);
		this.quantity = quantity;
	}
	
	@Override
	public boolean doReorder() {
		return inventoryPostion() <= reorderPoint();
	}

	@Override
	public int reorder() {
		return quantity;
	}

}