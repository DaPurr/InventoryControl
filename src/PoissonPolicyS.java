import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class PoissonPolicyS implements PolicyCreator {
	
	@Override
	public ReorderPolicy createPolicyCSL(int[] demand, double leadTime, double target) {
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
		Set<Material> poissonMaterials = new HashSet<>();
		PolicyCreator pc = new PoissonPolicyS();
		
		for (Material m : materials) {
			// estimate policy
			ReorderPolicy policy = pc.createPolicyCSL(m.getDemand(), m.getLeadTime(), mapTargetCSL.get(m.getCombinedClass()));
			Material material = new Material(m, policy);
			poissonMaterials.add(material);
		}
		
		return poissonMaterials;
	}

	@Override
	public ReorderPolicy createPolicyFR(int[] demand, double leadTime, double target) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private double mean(int[] demand) {
		double sum = 0.0;
		for (int i = 0; i < demand.length; i++) {
			sum += demand[i];
		}
		return sum/demand.length;
	}

}
