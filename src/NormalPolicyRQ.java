import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.NormalDistribution;

public class NormalPolicyRQ implements PolicyCreator {

	public NormalPolicyRQ() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Set<Material> createPolicyCSL(Set<Material> materials, Map<String, Double> mapTargetCSL) {
		Set<Material> normalMaterials = new TreeSet<>();
		PolicyCreator pc = new NormalPolicyRQ();
		
		for (Material m : materials) {
			// estimate policy
			ReorderPolicy policy = pc.createPolicyCSL(m, mapTargetCSL.get(m.getCombinedClass()));
			Material material = new Material(m, policy);
			normalMaterials.add(material);
		}
		
		return normalMaterials;
	}

	@Override
	public ReorderPolicy createPolicyCSL(Material m, double target) {
		int[] demand = m.getDemand();
		double leadTime = m.getLeadTime();
		if (target == 1.0)
			target -= 1e-4;
		
		double mean = mean(demand);
		double std = std(demand);
		
		double mu_prime = mean*leadTime;
		double sigma_prime = std*Math.sqrt(leadTime);
		
		// determine reorder point
		NormalDistribution normdist = new NormalDistribution();
		int reorder_point = (int)Math.ceil(mu_prime + sigma_prime*normdist.inverseCumulativeProbability(target));
		if (reorder_point < 1)
			reorder_point = 1;
		
		// determine EOQ
		double h = (0.25/12)*m.getPrice();
		int A = 36;
		int quantity = EOQ(mean, A, h);
		
		ReorderPolicy policy = new PolicyRQ(reorder_point, quantity);
		return policy;
	}
	
	private double mean(int[] demand) {
		double sum = 0.0;
		for (int i = 0; i < demand.length; i++) {
			sum += demand[i];
		}
		return sum/demand.length;
	}
	
	private double std(int[] demand) {
		double mean = mean(demand);
		double sum = 0.0;
		for (int i = 0; i < demand.length; i++) {
			sum += Math.pow(demand[i] - mean, 2);
		}
		return Math.sqrt(sum/(demand.length - 1));
	}

	@Override
	public ReorderPolicy createPolicyFR(Material m, double target) {
		// TODO Auto-generated method stub
		return null;
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
