import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 
 * This class carefully stores and distributes all the different events that come in to our system.
 * 
 * @author Nemanja Milovanovic
 *
 */
public class EventList {
	private PriorityQueue<Event> events;
	
	/**
	 * Constructs a new EventList using a priority queue implementation. The events are sorted in ascending 
	 * order according to their execution time into the system.
	 */
	public EventList() {
		events = new PriorityQueue<>(5, new EventsComparator());
	}
	
	/**
	 * Adds an event to the event list.
	 * 
	 * @param e Event to be added.
	 */
	public void addEvent(Event e) {
		events.add(e);
	}
	
	/**
	 * Removes the event with the smallest execution time from the event list and returns it.
	 * 
	 * @return Upcoming event, based on execution time.
	 */
	public Event nextEvent() {
		return events.poll();
	}
	
	public Event peek() {
		return events.peek();
	}
	
	public boolean hasEvent() {
		return !events.isEmpty();
	}
	
	private class EventsComparator implements Comparator<Event> {

		public EventsComparator() {
			
		}
		
		@Override
		public int compare(Event e1, Event e2) {
			return (int) Math.signum(e1.executionTime() - e2.executionTime());
		}
	}
}