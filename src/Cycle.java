
public class Cycle {
	private boolean stockout;
	
	public Cycle() {
		stockout = false;
	}
	
	public boolean getStatus() {
		return stockout;
	}
	
	public void stockedOut() {
		stockout = true;
	}
}
