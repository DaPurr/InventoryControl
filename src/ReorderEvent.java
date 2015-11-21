/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Represents a reorder of a certain amount for a certain material. The order quantity must be strictly positive and integer.
 *
 */
public class ReorderEvent extends Event {

	// class variables
	public Material m;
	public int quantity;
	private double execution_time;
	
	public ReorderEvent(Material m, int quantity, double execution_time) {
		this.m = m;
		this.quantity = quantity;
		this.execution_time = execution_time;
	}
	
	@Override
	public String type() {
		return "Order arrival";
	}

	@Override
	public double executionTime() {
		return execution_time;
	}

	@Override
	public Material associatedMaterial() {
		return m;
	}
	
	/**
	 * Gives the order quantity for this reorder.
	 * @return Order quantity.
	 */
	public int quantity() {
		return quantity;
	}
	
	@Override
	public String toString() {
		String s = super.toString();
		s += ",\tquantity=" + quantity();
		return s;
	}
}