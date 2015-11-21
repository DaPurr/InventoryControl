import org.apache.commons.math3.distribution.NormalDistribution;

public class NormalPolicyS implements PolicyCreator {

	public NormalPolicyS() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public ReorderPolicy createPolicyCSL(int[] demand, double leadTime, double target) {
		double mean = mean(demand);
		double std = std(demand);
		
		double mu_prime = mean*leadTime;
		double sigma_prime = std*Math.sqrt(leadTime);
		
		int baseStock = 1;
		NormalDistribution normdist = new NormalDistribution();
		double currentCSL = normdist.cumulativeProbability(normalize(baseStock-1, mu_prime, sigma_prime));
		while (currentCSL < target) {
			baseStock++;
			currentCSL = normdist.cumulativeProbability(normalize(baseStock-1, mu_prime, sigma_prime));
		}
		
		ReorderPolicy policy = new PolicySS(baseStock, baseStock);
		return policy;
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
	
	private double std(int[] demand) {
		double mean = mean(demand);
		double sum = 0.0;
		for (int i = 0; i < demand.length; i++) {
			sum += Math.pow(demand[i] - mean, 2);
		}
		return Math.sqrt(sum/(demand.length - 1));
	}
	
	private double normalize(double x, double mu, double sigma) {
		return (x-mu)/sigma;
	}

}