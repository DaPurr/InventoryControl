import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class PoissonPolicyS implements PolicyCreator {
	
	@Override
	public ReorderPolicy createPolicyCSL(Material m, double target) {
		int[] demand = m.getDemand();
		double leadTime = m.getLeadTime();
		if (target == 1.0)
			target -= 1e-4;
		
		double mean = mean(demand);
		
		double mu_prime = mean*leadTime;
		
		int baseStock = 1;
		PoissonDistribution poissdist = new PoissonDistribution(mu_prime);
		double currentCSL = poissdist.cumulativeProbability(baseStock);
		while (currentCSL < target) {
			baseStock++;
			currentCSL = poissdist.cumulativeProbability(baseStock);
		}
		
		ReorderPolicy policy = new PolicySS(baseStock, baseStock);
		return policy;
	}
	
	@Override
	public Set<Material> createPolicyCSL(Set<Material> materials, Map<String, Double> mapTargetCSL) {
		Set<Material> poissonMaterials = new TreeSet<>();
		PolicyCreator pc = new PoissonPolicyS();
		
		for (Material m : materials) {
			// estimate policy
			ReorderPolicy policy = pc.createPolicyCSL(m, mapTargetCSL.get(m.getCombinedClass()));
			Material material = new Material(m, policy);
			poissonMaterials.add(material);
		}
		
		return poissonMaterials;
	}

	@Override
	public ReorderPolicy createPolicyFR(Material m, double target) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private double mean(int[] demand) {
		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < demand.length; i++) {
			if (demand[i] >= 0) {
				sum += demand[i];
				count++;
			}
		}
		return sum/count;
	}

}
