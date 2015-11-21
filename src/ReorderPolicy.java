
public abstract class ReorderPolicy {
	
	// class variables
	private int minStock;
	private int maxStock;
	private int inventory_level;
	private int inventory_position;
	
	/**
	 * Creates a policy where we have the current stock set to maxStock.
	 * 
	 * @param minStock	Reorder point of stock.
	 * @param maxStock	Maximum allowed stock.
	 */
	public ReorderPolicy(int minStock, int maxStock) {
		this.minStock = minStock;
		this.maxStock = maxStock;
		this.inventory_level = maxStock;
		this.inventory_position = maxStock;
	}
	
	/**
	 * Creates a policy where we have the current stock set to a user-specified quantity.
	 * 
	 * @param currentStock	Inventory level of the stock.
	 * @param minStock		Reorder point of stock.
	 * @param maxStock		Maximum allowed stock.
	 */
	public ReorderPolicy(int currentStock, int minStock, int maxStock) {
		this.minStock = minStock;
		this.maxStock = maxStock;
		this.inventory_level = currentStock;
		this.inventory_position = currentStock;
	}
	
	/**
	 * Returns the current inventory level.
	 * 
	 * @return	Inventory level.
	 */
	public int inventoryLevel() {
		return inventory_level;
	}
	
	/**
	 * Returns the current inventory position.
	 * 
	 * @return	Inventory position.
	 */
	public int inventoryPostion() {
		return inventory_position;
	}
	
	/**
	 * Checks if we need to do a reorder based on the order policy.
	 * 
	 * @return	True if we need to reorder, false otherwise.
	 */
	public abstract boolean doReorder();
	
	/**
	 * Checks the amount of materials we need to reorder.
	 * 
	 * @return	Reorder quantity.
	 */
	public abstract int reorder();
	
	/**
	 * Checks if we have any backorders.
	 * 
	 * @return	True if we have backorders, false otherwise.
	 */
	public boolean isShort() {
		return inventoryLevel() < 0;
	}
	
	/**
	 * Reports the amount of backorders.
	 * 
	 * @return	Amount of backorders.
	 */
	public int amountShort() {
		if (isShort())
			return -inventoryLevel();
		return 0;
	}
	
	/**
	 * Returns the reorder point of the policy.
	 * 
	 * @return	Reorder point.
	 */
	public int reorderPoint() {
		return minStock;
	}
	
	/**
	 * Returns the maximum allowed stock level for this policy.
	 * 
	 * @return	Maximum allowed stock level.
	 */
	public int maxStock() {
		return maxStock;
	}
	
	/**
	 * Consumes a certain amount of stock.
	 * 
	 * @param 	amount	Amount of stock to be consumed.
	 * @return			Current stock level.		
	 */
	public int consume(int amount) {		
		inventory_level -= amount;
		inventory_position = Math.max(inventory_position-amount, 0);
		return inventory_level;
	}
	
	/**
	 * Replenishes the inventory level by a certain amount.
	 * 
	 * @param	amount	Replenishment amount.
	 * @return			Updated inventory level
	 */
	public int replenishInventoryLevel(int amount) {
		if (amount < 0)
			throw new IllegalStateException("Cannot replenish negative values.");
		inventory_level += amount;
		return inventory_level;
	}
	
	/**
	 * Replenishes the inventory position by a certain amount.
	 * 
	 * @param	amount	Replenishment amount.
	 * @return			Updated inventory position
	 */
	public int replenishInventoryPosition(int amount) {
		if (amount < 0)
			throw new IllegalStateException("Cannot replenish negative values.");
		inventory_position += amount;
		return inventory_position;
	}
}
