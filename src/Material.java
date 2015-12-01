/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Class representation of a spare part. Per part we register the associated ID, price, etc..
 *
 */
public class Material {

	// class variables describing the material
	String id;
	double price;
	int stock;
	double lead_time;
	int crit_H;
	int crit_M;
	int crit_L;
	int[] demand;
	ReorderPolicy policy;
	String demandClass;
	String priceClass;
	
	/**
	 * Creates a material instance with the parameters given. The default reorder policy is (s,S),
	 * where s = min_stock-1 and S = max_stock.
	 * 
	 * @param id			ID for the material.
	 * @param price			Price per unit.
	 * @param current_stock	Current stock for the material.
	 * @param min_stock		Minimum allowed stock.
	 * @param max_stock		Maximum allowed stock.
	 * @param lead_time		Material lead-time per order.
	 * @param crit_H		Number of components in critical class H that need the material.
	 * @param crit_M		Number of components in critical class M that need the material.
	 * @param crit_L		Number of components in critical class L that need the material.
	 * @param demand		Historical demand.
	 */
	public Material(String id, double price, int min_stock, int max_stock, double lead_time, 
			int crit_H, int crit_M, int crit_L, int[] demand, String demandClass, String priceClass) {
		this.id = id;
		this.price = price;
		this.lead_time = lead_time;
		this.crit_H = crit_H;
		this.crit_M = crit_M;
		this.crit_L = crit_L;
		this.demand = demand;
		this.demandClass = demandClass;
		this.priceClass = priceClass;
		
		policy = new PolicySS(min_stock-1, max_stock);
	}
	
	public Material(String id, double price, ReorderPolicy policy, double lead_time, int crit_H, 
			int crit_M, int crit_L, int[] demand, String demandClass, String priceClass) {
		this.id = id;
		this.price = price;
		this.policy = policy;
		this.lead_time = lead_time;
		this.crit_H = crit_H;
		this.crit_M = crit_M;
		this.crit_L = crit_L;
		this.demand = demand;
		this.demandClass = demandClass;
		this.priceClass = priceClass;
	}
	
	public Material(Material m, ReorderPolicy policy) {
		this.id = m.getId();
		this.price = m.getPrice();
		this.lead_time = m.getLeadTime();
		this.crit_H = m.getCritH();
		this.crit_M = m.getCritM();
		this.crit_L = m.getCritL();
		this.demand = m.getDemand();
		this.demandClass = m.getDemandClass();
		this.priceClass = m.getPriceClass();
		this.policy = policy;
	}
	
	public int[] getDemand() {
		return demand;
	}
	
	public String getDemandClass() {
		return demandClass;
	}
	
	public String getPriceClass() {
		return priceClass;
	}
	
	public String getCombinedClass() {
		return priceClass + demandClass + criticality();
	}
	
	public boolean doReorder() {
		return policy.doReorder();
	}
	
	public int reorder() {
		return policy.reorder();
	}
	
	public int demand(int day) {
		return demand[day];
	}
	
	public int consume(int amount) {
		return policy.consume(amount);
	}
	
	public int replenishInventoryLevel(int amount) {
		return policy.replenishInventoryLevel(amount);
	}
	
	public int replenishInventoryPosition(int amount) {
		return policy.replenishInventoryPosition(amount);
	}

	public String getId() {
		return id;
	}

	public double getPrice() {
		return price;
	}

	public int getInventoryLevel() {
		return policy.inventoryLevel();
	}
	
	public int getInventoryPosition() {
		return policy.inventoryPostion();
	}

	public int getReorderPoint() {
		return policy.reorderPoint();
	}

	public int getMaxStock() {
		return policy.maxStock();
	}

	public double getLeadTime() {
		return lead_time;
	}
	
	public int getCritH() {
		return crit_H;
	}

	public int getCritM() {
		return crit_M;
	}

	public int getCritL() {
		return crit_L;
	}
	
	@Override
	public String toString() {
		String s = "[";
		s += "id: " + id + ", ";
		s += "price: " + price;
		s += "]";
		
		return s;
	}
	
	public boolean isShort() {
		return policy.isShort();
	}
	
	public int amountShort() {
		return policy.amountShort();
	}
	
	/**
	 * This method returns the material criticality. We have defined it as
	 * 		- H: #(highly critical components) > 0
	 * 		- M: #(medium critical components) > 0
	 * 		- L: #(low critical components) > 0
	 * 		- Z: otherwise
	 * where we have encoded H, M, L, Z, as 1, 2, 3, 4, respectively.
	 * 
	 * @return	Criticality class.
	 */
	public String criticality() {
		if (crit_H > 0)
			return "1";
		if (crit_M > 0)
			return "2";
		if (crit_L > 0)
			return "3";
		return "4";
	}
	
	public int totalDemand() {
		int sum = 0;
		for (int i = 0; i < demand.length; i++)
			sum += demand[i];
		return sum;
	}
	
	public int totalPositiveDemand() {
		int sum = 0;
		for (int i = 0; i < demand.length; i++) {
			if (demand[i] <= 0)
				continue;
			sum += demand[i];
		}
		return sum;
	}
	
}
