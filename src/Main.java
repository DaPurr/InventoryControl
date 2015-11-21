import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		Simulator sim = new Simulator("DataCorrectedWithClassFreqLD.csv");
		
		// perform simulation
		System.out.println(sim.simulate());
		
		// export service measures
		try {
			sim.exportServiceMeasures();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}