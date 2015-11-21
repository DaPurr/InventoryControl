/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Represents a consumption event of a certain material. The consumption can be any strictly positive integer.
 *
 */
public class ConsumptionEvent extends Event {

	private Material m;
	private int demand;
	private double execution_time;
	
	/**
	 * Constructor for a consumption event. Each consumption event must take place sometime, and have a certain 
	 * strictly positive integer demand and must be linked to a material.
	 * 
	 * @param m_id				Material linked to this event.
	 * @param demand			Strictly positive demand.
	 * @param execution_time	Time at which the consumption takes place.
	 */
	public ConsumptionEvent(Material m, int demand, double execution_time) {
		this.m = m;
		this.demand = demand;
		this.execution_time = execution_time;
	}
	
	@Override
	public String type() {
		return "Consumption";
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
	 * Gives the demand for this consumption event. 
	 * 
	 * @return Returns demand.
	 */
	public int demand() {
		return demand;
	}
	
	@Override
	public String toString() {
		String s = super.toString();
		s += ",\tdemand=" + demand();
		return s;
	}

}