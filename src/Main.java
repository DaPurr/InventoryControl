import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		Simulator simCurrent = new Simulator("DataCorrectedWithClassFreqLD.csv");
		
		// simulate using current policies
		System.out.println(simCurrent.simulate());
		Map<String, Double> currentCSLCombined = simCurrent.getRealizedCSLCombined();
		Map<String, Double> currentFRCombined = simCurrent.getRealizedFRCombined();
		Set<Material> currentMaterials = simCurrent.getMaterials();
		
		// simulate using Porras
//		PorrasPolicyS porraspcS = new PorrasPolicyS();
//		Set<Material> porrasMaterials = porraspcS.createPolicyCSL(currentMaterials, currentCSLCombined);
//		Simulator simPorras = new Simulator(porrasMaterials);
//		System.out.println(simPorras.simulate());
//		// harmonize Porras
//		Simulator harmonizedPorrasSim = harmonizeService(porraspcS, porrasMaterials, currentCSLCombined, currentFRCombined);
//		System.out.println(harmonizedPorrasSim.simulate());
		
		// simulate using Normal policies
//		NormalPolicyS normpcS = new NormalPolicyS();
//		Set<Material> normalMaterials = normpcS.createPolicyCSL(currentMaterials, currentCSLCombined);
//		Simulator simNormal = new Simulator(normalMaterials);
//		System.out.println(simNormal.simulate());
//		// harmonize normal
//		Simulator harmonizedNormalSim = harmonizeService(normpcS, normalMaterials, currentCSLCombined, currentFRCombined);
//		System.out.println(harmonizedNormalSim.simulate());
		
		// only do Poisson for some
//		PoissonPolicyS poisspcS = new PoissonPolicyS();
//		Set<Material> poissonMaterials = poisspcS.createPolicyCSL(currentMaterials, currentCSLCombined);
//		Simulator simPoisson= new Simulator(poissonMaterials);
//		System.out.println(simPoisson.simulate());
//		// harmonize Poisson
//		Simulator harmonizedPoissonSim = harmonizeService(normpcS, normalMaterials, currentCSLCombined, currentFRCombined);
//		System.out.println(harmonizedNormalSim.simulate());
		
		// export service measures
		try {
			simCurrent.exportServiceMeasures("harm_porras_test");
//			simNormal.exportServiceMeasures("normal");
//			simPoisson.exportServiceMeasures("poisson");
//			harmonizedNormalSim.exportServiceMeasures("harm_normal");
//			harmonizedPorrasSim.exportServiceMeasures("harm_porras");
//			harmonizedPoissonSim.exportServiceMeasures("harm_poisson");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Simulator harmonizeService(PolicyCreator pc, Set<Material> materials, 
			Map<String, Double> targetCSL, Map<String, Double> targetFR) {
		// initialize the environment
		Simulator sim = new Simulator(materials);
		sim.simulate();
		Map<String, Double> realizedCSL = sim.getRealizedCSLCombined();
		Map<String, Double> realizedFR = sim.getRealizedFRCombined();
		Map<String, Double> newTargetCSL = new HashMap<>(targetCSL);
		
		// stopping criteria
		// step size criterion
		double epsilon = 1e-5;
		// max iterations
		int n_max = 300;
		// neighborhood criterion
		double neighborhood = 0.02;
		
		// implement a binary search type method
		Map<String, Boolean> stopping_criteria = new HashMap<>();
		Map<String, Double> lower_bounds = new HashMap<>();
		Map<String, Double> upper_bounds = new HashMap<>();
		int iteration = 1;
		for (String group : targetCSL.keySet()) {
			stopping_criteria.put(group, false);
			lower_bounds.put(group, 0.0);
			upper_bounds.put(group, 1.0);
		}
		
		// begin the method
		boolean cont = true;
		while (iteration <= n_max && cont) {
			cont = false;
			for (String group : stopping_criteria.keySet()) {
				if (stopping_criteria.get(group))
					continue;

				cont = true;
				double tCSL = targetCSL.get(group);
				double tFR = targetFR.get(group);
				double rCSL = realizedCSL.get(group);
				double rFR = realizedFR.get(group);
				if (Math.abs(tCSL-rCSL) <= neighborhood && Math.abs(tFR-rFR) <= neighborhood) {
					stopping_criteria.put(group, true);
					continue;
				}

				// calculate new target CSL
				double mean_target = (tCSL + tFR)/2;
				double mean_realized = (rCSL + rFR)/2;
				if (mean_realized < mean_target) {
					lower_bounds.put(group, newTargetCSL.get(group));
				} else {
					upper_bounds.put(group, newTargetCSL.get(group));
				}
				double lb = lower_bounds.get(group);
				double ub = upper_bounds.get(group);
				newTargetCSL.put(group, (lb+ub)/2);
				
				// update stopping criteria
				if (ub-lb < epsilon)
					stopping_criteria.put(group, true);
			}
			
			// update rCSL and rFR using simulation
			Set<Material> newMaterials = pc.createPolicyCSL(materials, newTargetCSL);
			sim = new Simulator(newMaterials);
			sim.simulate();
			realizedCSL = sim.getRealizedCSLCombined();
			realizedFR = sim.getRealizedFRCombined();
			System.out.println(iteration);
			iteration++;
		}
		
		return sim;
	}

}