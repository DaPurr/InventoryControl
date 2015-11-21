/**
 * 
 * @author Nemanja Milovanovic
 * 
 * A class that keeps track of time and which event needs to be scheduled when.
 *
 */

public class Scheduler {
	
	// class variables
	private EventList evl;
	private double time;
	
	/**
	 * Constructs the scheduler.
	 */
	public Scheduler() {
		time = 0;
		evl = new EventList();
	}
	
	/**
	 * Extracts the upcoming event from the event list and updates the time.
	 * 
	 * @return Upcoming event.
	 */
	public Event nextEvent() {
		Event e = evl.nextEvent();
		time = e.executionTime();
		return e;
	}
	
	/**
	 * Schedules a consumption event with a prespecified execution time for a certain material.
	 * 
	 * @param m_id				Material ID which is to be consumed.
	 * @param execution_time	Time at which the consumption will take place.
	 * @return					Returns the scheduled consumption event.
	 */
	public ConsumptionEvent addConsumptionEvent(Material m, int demand, double execution_time) {
		ConsumptionEvent e = new ConsumptionEvent(m, demand, execution_time);
		evl.addEvent(e);
		return e;
	}
	
	/**
	 * Schedules a reorder event to take place at a prespecified time for a certain material. The
	 * processing time is assumed to be negligible.
	 * 
	 * @param m_id				Material to be replenished.
	 * @param execution_time	Time at which the reorder will be processed.
	 * @return					Returns the scheduled reorder event.
	 */
	public ReorderEvent addReorderEvent(Material m, int quantity, double execution_time) {
		ReorderEvent e = new ReorderEvent(m, quantity, execution_time);
		evl.addEvent(e);
		return e;
	}
	
	/**
	 * Checks if there is an upcoming event in the future.
	 * 
	 * @return	True if there is an event which needs to be processed, false otherwise.
	 */
	public boolean hasEvent() {
		return evl.hasEvent();
	}
	
	/**
	 * Gives the current time in the system.
	 * 
	 * @return Returns the current time.
	 */
	public double time() {
		return time;
	}
}
