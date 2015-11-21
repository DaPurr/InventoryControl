import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Measures performance of the implemented policy.
 *
 */
public class Performance {
	
	// general
	Set<Material> materials;
	
	// CSL
	Map<Material, Integer> countCycles;
	Map<Material, Integer> countStockouts;
	
	// fill rate
	Map<Material, Integer> totalDemand;
	Map<Material, Integer> deniedDemand;
	
	// costs
	double totalHoldingCosts = 0.0;
	double totalFixedCosts = 0.0;
	double totalMarginalCosts = 0.0;
	
	Map<Material, Double> holdingCosts;
	Map<Material, Double> fixedCosts;
	Map<Material, Double> marginalCosts;
	
	Map<String, Double> holdingCostsCombined;
	Map<String, Double> holdingCostsPrice;
	Map<String, Double> holdingCostsDemand;
	Map<String, Double> holdingCostsCrit;
	
	Map<String, Double> fixedCostsCombined;
	Map<String, Double> fixedCostsPrice;
	Map<String, Double> fixedCostsDemand;
	Map<String, Double> fixedCostsCrit;
	
	Map<String, Double> marginalCostsCombined;
	Map<String, Double> marginalCostsPrice;
	Map<String, Double> marginalCostsDemand;
	Map<String, Double> marginalCostsCrit;
	
	public Performance(Set<Material> materials) {
		// general
		this.materials = materials;
		
		// CSL
		countCycles = new HashMap<>();
		countStockouts = new HashMap<>();
		
		// FR
		totalDemand = new HashMap<>();
		deniedDemand = new HashMap<>();
		
		// costs
		holdingCosts = new HashMap<>();
		fixedCosts = new HashMap<>();
		marginalCosts = new HashMap<>();
		
		holdingCostsCombined = new TreeMap<>();
		holdingCostsPrice = new TreeMap<>();
		holdingCostsDemand = new TreeMap<>();
		holdingCostsCrit = new TreeMap<>();
		
		fixedCostsCombined = new TreeMap<>();
		fixedCostsPrice = new TreeMap<>();
		fixedCostsDemand = new TreeMap<>();
		fixedCostsCrit = new TreeMap<>();
		
		marginalCostsCombined = new TreeMap<>();
		marginalCostsPrice = new TreeMap<>();
		marginalCostsDemand = new TreeMap<>();
		marginalCostsCrit = new TreeMap<>();
		
		for (Material m : materials) {
			countCycles.put(m, 1);
			countStockouts.put(m, 0);
			
			totalDemand.put(m, 0);
			deniedDemand.put(m, 0);
			
			holdingCosts.put(m, 0.0);
			fixedCosts.put(m, 0.0);
			marginalCosts.put(m, 0.0);
			
			holdingCostsCombined.put(m.getCombinedClass(), 0.0);
			holdingCostsPrice.put(m.getPriceClass(), 0.0);
			holdingCostsDemand.put(m.getDemandClass(), 0.0);
			holdingCostsCrit.put(m.criticality(), 0.0);
			
			fixedCostsCombined.put(m.getCombinedClass(), 0.0);
			fixedCostsPrice.put(m.getPriceClass(), 0.0);
			fixedCostsDemand.put(m.getDemandClass(), 0.0);
			fixedCostsCrit.put(m.criticality(), 0.0);
			
			marginalCostsCombined.put(m.getCombinedClass(), 0.0);
			marginalCostsPrice.put(m.getPriceClass(), 0.0);
			marginalCostsDemand.put(m.getDemandClass(), 0.0);
			marginalCostsCrit.put(m.criticality(), 0.0);
		}
	}
	
	public void startCycle(Material m) {
		countCycles.put(m, countCycles.get(m) + 1);
	}
	
	public void endCycle(Material m, Cycle c) {
//		isCycle.remove(m);
//		stockout.remove(m);
	}
	
	public void stockout(Material m) {
		countStockouts.put(m, countStockouts.get(m) + 1);
	}
	
	public void deniedDemand(Material m, int quantity) {
		deniedDemand.put(m, deniedDemand.get(m) + quantity);
	}
	
	public void addDemand(Material m, int quantity) {
		totalDemand.put(m, totalDemand.get(m) + quantity);
	}
	
	public double CSL(Material m) {
		double csl = 1 - (double)countStockouts.get(m)/countCycles.get(m);
		return csl;
	}
	
	public double fillRate(Material m) {
		return 1 - (double)deniedDemand.get(m)/totalDemand.get(m);
	}
	
	public Map<Material, Double> fillRateAll() {
		Map<Material, Double> fr = new HashMap<>();
		for (Material m : materials) {
			fr.put(m, fillRate(m));
		}
		return fr;
	}
	
	public Map<Material, Double> CSLAll() {
		Map<Material, Double> csl = new HashMap<>();
		for (Material m : materials) {
			csl.put(m, CSL(m));
		}
		return csl;
	}
	
	public void addHoldingCosts(Material m, double costs) {
		totalHoldingCosts += costs;
		holdingCosts.put(m, holdingCosts.get(m) + costs);
		String groupCombined = m.getCombinedClass();
		String groupPrice = m.getPriceClass();
		String groupDemand = m.getDemandClass();
		String groupCrit = m.criticality();
		
		holdingCostsCombined.put(groupCombined, holdingCostsCombined.get(groupCombined) + costs);		
		holdingCostsPrice.put(groupPrice, holdingCostsPrice.get(groupPrice) + costs);
		holdingCostsDemand.put(groupDemand, holdingCostsDemand.get(groupDemand) + costs);		
		holdingCostsCrit.put(groupCrit, holdingCostsCrit.get(groupCrit) + costs);
	}
	
	public void addFixedCosts(Material m, double costs) {
		totalFixedCosts += costs;
		fixedCosts.put(m, fixedCosts.get(m) + costs);
		String groupCombined = m.getCombinedClass();
		String groupPrice = m.getPriceClass();
		String groupDemand = m.getDemandClass();
		String groupCrit = m.criticality();
		
		fixedCostsCombined.put(groupCombined, fixedCostsCombined.get(groupCombined) + costs);
		fixedCostsPrice.put(groupPrice, fixedCostsPrice.get(groupPrice) + costs);
		fixedCostsDemand.put(groupDemand, fixedCostsDemand.get(groupDemand) + costs);
		fixedCostsCrit.put(groupCrit, fixedCostsCrit.get(groupCrit) + costs);
	}
	
	public void addMarginalCosts(Material m, double costs) {
		totalMarginalCosts += costs;
		marginalCosts.put(m, marginalCosts.get(m) + costs);
		String groupCombined = m.getCombinedClass();
		String groupPrice = m.getPriceClass();
		String groupDemand = m.getDemandClass();
		String groupCrit = m.criticality();
		
		marginalCostsCombined.put(groupCombined, marginalCostsCombined.get(groupCombined) + costs);
		marginalCostsPrice.put(groupPrice, marginalCostsPrice.get(groupPrice) + costs);
		marginalCostsDemand.put(groupDemand, marginalCostsDemand.get(groupDemand) + costs);
		marginalCostsCrit.put(groupCrit, marginalCostsCrit.get(groupCrit) + costs);
	}
	
	public double getTotalCosts() {
		return totalFixedCosts + totalHoldingCosts;
	}
	
	public double getTotalHoldingCosts() {
		return totalHoldingCosts;
	}
	
	public double getTotalFixedCosts() {
		return totalFixedCosts;
	}
	
	public double getTotalMarginalCosts() {
		return totalMarginalCosts;
	}
	
	public double getCombinedGroupHoldingCosts(String group) {
		return holdingCostsCombined.get(group);
	}
	
	public double getPriceGroupHoldingCosts(String group) {
		return holdingCostsPrice.get(group);
	}
	
	public double getDemandGroupHoldingCosts(String group) {
		return holdingCostsDemand.get(group);
	}
	
	public double getCritGroupHoldingCosts(String group) {
		return holdingCostsCrit.get(group);
	}
	
	public double getCombinedGroupFixedCosts(String group) {
		return fixedCostsCombined.get(group);
	}
	
	public double getPriceGroupFixedCosts(String group) {
		return fixedCostsPrice.get(group);
	}
	
	public double getDemandGroupFixedCosts(String group) {
		return fixedCostsDemand.get(group);
	}
	
	public double getCritGroupFixedCosts(String group) {
		return fixedCostsCrit.get(group);
	}
	
	public double getCombinedGroupMarginalCosts(String group) {
		return marginalCostsCombined.get(group);
	}
	
	public double getPriceGroupMarginalCosts(String group) {
		return marginalCostsPrice.get(group);
	}
	
	public double getDemandGroupMarginalCosts(String group) {
		return marginalCostsDemand.get(group);
	}
	
	public double getCritGroupMarginalCosts(String group) {
		return marginalCostsCrit.get(group);
	}
	
}
