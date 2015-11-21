import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		Simulator simCurrent = new Simulator("DataCorrectedWithClassFreqLD.csv");
		
		// simulate using current policies
		System.out.println(simCurrent.simulate());
		Map<String, Double> currentCSLCombined = simCurrent.getRealizedCSLCombined();
		Set<Material> currentMaterials = simCurrent.getMaterials();
		
		// simulate using Normal policies
		NormalPolicyS normpcS = new NormalPolicyS();
		Set<Material> normalMaterials = normpcS.createPolicyCSL(currentMaterials, currentCSLCombined);
		Simulator simNormal = new Simulator(normalMaterials);
		System.out.println(simNormal.simulate());
		
		// only do Poisson for some
//		PoissonPolicyS poisspcS = new PoissonPolicyS();
//		Set<Material> poissonMaterials = poisspcS.createPolicyCSL(currentMaterials, currentCSLCombined);
//		Simulator simPoisson= new Simulator(poissonMaterials);
//		System.out.println(simPoisson.simulate());
		
		// compare
		
		
		// export service measures
		try {
			simCurrent.exportServiceMeasures("current");
			simNormal.exportServiceMeasures("normal");
//			simPoisson.exportServiceMeasures("poisson");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}