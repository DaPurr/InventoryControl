import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.PoissonDistribution;

public class PoissonPolicyRQ implements PolicyCreator {

	@Override
	public ReorderPolicy createPolicyCSL(Material m, double target) {
		int[] demand = m.getDemand();
		double leadTime = m.getLeadTime();
		if (target == 1.0)
			target -= 1e-4;
		
		double mean = mean(demand);
		
		double mu_prime = mean*leadTime;
		
		int reorderPoint = 1;
		PoissonDistribution poissdist = new PoissonDistribution(mu_prime);
		double currentCSL = poissdist.cumulativeProbability(reorderPoint);
		while (currentCSL < target) {
			reorderPoint++;
			currentCSL = poissdist.cumulativeProbability(reorderPoint);
		}

		// determine EOQ
		double h = 0.25/12;
		int A = 36;
		int quantity = EOQ(mean, A, h);

		ReorderPolicy policy = new PolicyRQ(reorderPoint, quantity);
		return policy;
	}

	@Override
	public Set<Material> createPolicyCSL(Set<Material> materials, Map<String, Double> mapTargetCSL) {
		Set<Material> poissonMaterials = new TreeSet<>();
		PolicyCreator pc = new PoissonPolicyRQ();
		
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
	
	private double totalCosts(int Q, double d, double A, double h) {
		return A*(d/Q) + h*(Q/2);
	}
	
	private int EOQ(double d, double A, double h) {
		double quantity = Math.sqrt( (2*A*d)/h );
		double C_low = totalCosts((int)Math.floor(quantity), d, A, h);
		double C_upp = totalCosts((int)Math.ceil(quantity), d, A, h);
		if (quantity <= 1)
			return 1;
		else if (C_low < C_upp)
			return (int)Math.ceil(quantity);
		else
			return (int)Math.ceil(quantity);
	}

}
