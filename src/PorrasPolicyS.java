import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PorrasPolicyS implements PolicyCreator {
	
	public PorrasPolicyS() {
		
	}

	@Override
	public Set<Material> createPolicyCSL(Set<Material> materials, Map<String, Double> mapTargetCSL) {
		Set<Material> porrasMaterials = new TreeSet<>();
		PolicyCreator pc = new PorrasPolicyS();

		for (Material m : materials) {
			// estimate policy
			ReorderPolicy policy = pc.createPolicyCSL(m, mapTargetCSL.get(m.getCombinedClass()));
			Material material = new Material(m, policy);
			porrasMaterials.add(material);
		}
		return porrasMaterials;
	}


	@Override
	public ReorderPolicy createPolicyCSL(Material m, double target) {
		// create frequency table and pmf
		TreeMap<Integer, Integer> frequencyTable = new TreeMap<>();
		TreeMap<Integer, Double> probabilityTable = new TreeMap<>();
		
		int[] demand = m.getDemand();
		double leadTime = m.getLeadTime();
		if (target == 1.0)
			target -= 1e-4;
		
		int roundLeadTime = (int) Math.ceil(leadTime);
		List<Integer> leadTimeDemands = new ArrayList<>();
		for (int i = 0; i <= demand.length - roundLeadTime; i++) {
			int sum = 0;
			for (int j = i; j < i + roundLeadTime; j++) {
				sum += demand[j];
			}
			leadTimeDemands.add(sum); 
		}

		for(int i=0; i < leadTimeDemands.size(); i++) {
			if (frequencyTable.get(leadTimeDemands.get(i)) != null){
				int elementCount = frequencyTable.get(leadTimeDemands.get(i));
				frequencyTable.put(leadTimeDemands.get(i), elementCount+1);

			} else {
				frequencyTable.put(leadTimeDemands.get(i),1);
			}
		}
		int sum=0;
		for (int i : frequencyTable.keySet()){
			sum += frequencyTable.get(i);
		}

		for(int i : frequencyTable.keySet()) {
			double probability = (double)frequencyTable.get(i) / sum;
			probabilityTable.put(i, probability);
		}

		int baseStock = 1;
		double currentCSL = porrasCDF(baseStock, probabilityTable);
		while (lessThan(currentCSL, target)) {
			baseStock++;
			currentCSL = porrasCDF(baseStock, probabilityTable);
		}

		ReorderPolicy policy = new PolicySS(baseStock, baseStock);
		return policy;
	}

	@Override
	public ReorderPolicy createPolicyFR(Material m,	double target) {
		// TODO Auto-generated method stub
		return null;
	}

	private double porrasCDF(int k, TreeMap<Integer, Double> probabilityTable) {
		double totalProb = 0.0;
		for (int i : probabilityTable.keySet()) {
			if (k < i)
				return totalProb;
			totalProb += probabilityTable.get(i);
		}
		return totalProb;
	}
	
	private boolean lessThan(double a, double b) {
		final double epsilon = 1e-5;
		return (a + epsilon) < b;
	}
}