/**
 * 
 * @author Nemanja Milovanovic
 * 
 * This abstract class is a template for all events, like reorders and material consumptions.
 *
 */
public abstract class Event {
	/**
	 * Returns the type of this particular event.
	 * 
	 * @return Type of event.
	 */
	public abstract String type();
	
	/**
	 * Returns the time this event arrived in the system.
	 * 
	 * @return Arrival time.
	 */
	public abstract double executionTime();
	
	/**
	 * All events should be tied to a certain material. This function returns the unique ID this event is tied to.
	 * 
	 * @return Material ID.
	 */
	public abstract Material associatedMaterial();
	
	@Override
	public String toString() {
		return type() + ":\tmaterial=" + associatedMaterial() + ",\texecution=" + executionTime();
	}
}