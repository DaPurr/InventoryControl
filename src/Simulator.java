import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * @author Nemanja Milovanovic
 * 
 * Class Simulator simulates an inventory control system with fixed lead-time and reorder policies different for each material.
 * The simulation uses historic demand to evaluate reorder policies.
 *
 */
public class Simulator {
	
	// class variables
	private Set<Material> materials;
	private Scheduler scheduler;
	private int horizon;
	private Performance perf;
	
	// costs
	private final double holding_costs = 0.25/12;
	private final double fixed_costs = 36.0;
	private final double stockout_costs_H = 30*24*1000.0;
	private final double stockout_costs_M = 30*24*200.0;
	private final double stockout_costs_L = 30*24*50.0;
	private final double stockout_costs_Z = 30*24*0.0;
	
	private double total_stockout_H = 0.0;
	private double total_stockout_M = 0.0;
	private double total_stockout_L = 0.0;
	private double total_stockout_Z = 0.0;
	
	// groups
	private Set<String> demandGroups;
	private Set<String> priceGroups;
	private Set<String> critGroups;
	private Set<String> combinedGroups;
	
	// system state - auxiliary variable for determining holding and backorder costs
	private Map<Material, Double> system_state;
	private Set<Material> currentStockouts = new HashSet<>();
	
	// service measures
	// fill rate
	private Map<String, Double> fillRateDemandGroup;
	private Map<String, Double> fillRatePriceGroup;
	private Map<String, Double> fillRateCritGroup;
	private Map<String, Double> fillRateCombinedGroup;
	
	// CSL
	// class-based counters
	private Map<String, Double> CSLDemandGroup;
	private Map<String, Double> CSLPriceGroup;
	private Map<String, Double> CSLCritGroup;
	private Map<String, Double> CSLCombinedGroup;
	
	Map<String, Integer> groupTotalDemandDemand = new TreeMap<>();
	Map<String, Integer> groupTotalDemandPrice = new TreeMap<>();
	Map<String, Integer> groupTotalDemandCrit = new TreeMap<>();
	Map<String, Integer> groupTotalDemandCombined = new TreeMap<>();
	
	Map<String, Integer> groupSizeDemand = new TreeMap<>();
	Map<String, Integer> groupSizePrice = new TreeMap<>();
	Map<String, Integer> groupSizeCrit = new TreeMap<>();
	Map<String, Integer> groupSizeCombined = new TreeMap<>();
	
	/**
	 * Creates a Simulator class based of the data in file_name.
	 * @param file_name	Location of the file with all material information.
	 */
	public Simulator(String file_name) {
		// initialize all class variables
		scheduler = new Scheduler();
		system_state = new HashMap<>();
		
		CSLDemandGroup = new TreeMap<>();
		CSLPriceGroup = new TreeMap<>();
		CSLCritGroup = new TreeMap<>();
		CSLCombinedGroup = new TreeMap<>();
		fillRateDemandGroup = new TreeMap<>();
		fillRatePriceGroup = new TreeMap<>();
		fillRateCritGroup = new TreeMap<>();
		fillRateCombinedGroup = new TreeMap<>();
		demandGroups = new TreeSet<>();
		priceGroups = new TreeSet<>();
		critGroups = new TreeSet<>();
		combinedGroups = new TreeSet<>();
		
		try {
			materials = importMaterials(file_name);
			perf = new Performance(materials);
			for (Material m : materials) {
				system_state.put(m, 0.0);
				priceGroups.add(m.getPriceClass());
				demandGroups.add(m.getDemandClass());
				critGroups.add(m.criticality());
				combinedGroups.add(m.getCombinedClass());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Simulator(Set<Material> materials) {
		// initialize all class variables
//				this.file_name = file_name;
		this.materials = materials;
		scheduler = new Scheduler();
		system_state = new HashMap<>();

		CSLDemandGroup = new TreeMap<>();
		CSLPriceGroup = new TreeMap<>();
		CSLCritGroup = new TreeMap<>();
		CSLCombinedGroup = new TreeMap<>();
		fillRateDemandGroup = new TreeMap<>();
		fillRatePriceGroup = new TreeMap<>();
		fillRateCritGroup = new TreeMap<>();
		fillRateCombinedGroup = new TreeMap<>();
		demandGroups = new TreeSet<>();
		priceGroups = new TreeSet<>();
		critGroups = new TreeSet<>();
		combinedGroups = new TreeSet<>();
		
		perf = new Performance(materials);
		boolean first = true;
		for (Material m : materials) {
			if (first) {
				horizon = m.getDemand().length;
				first = false;
			}
			system_state.put(m, 0.0);
			priceGroups.add(m.getPriceClass());
			demandGroups.add(m.getDemandClass());
			critGroups.add(m.criticality());
			combinedGroups.add(m.getCombinedClass());
		}
	}
	
	/**
	 * Does the actual Discrete-Event Simulation (DES). The DES is deterministic and we only need to run it once, so it's very fast.
	 * 
	 * @return	Output of the simulation summary.
	 */
	public String simulate() {
		String s = "";
		
		// start the iteration up
		startup();
				
		// handle incoming events
		while (scheduler.hasEvent() && scheduler.time() < horizon ) {
			Event next_event = scheduler.nextEvent();
			
			// TODO: ONLY FOR DEBUGGING PURPOSES
			Material m = next_event.associatedMaterial();
			int deniedDemand = perf.deniedDemand.get(m);
			int totalDemand = perf.totalDemand.get(m);
			int nrStockouts = perf.countStockouts.get(m);
			int nrCycles = perf.countCycles.get(m);
			double FR = perf.fillRate(m);
			double CSL = perf.CSL(m);
			int IL = m.getInventoryLevel();
			int IP = m.getInventoryPosition();
			
			// handle a consumption event
			if (next_event instanceof ConsumptionEvent) {
				ConsumptionEvent consumption_event = (ConsumptionEvent) next_event;
				callbackConsumptionEvent(consumption_event);
			} 
					
			// handle a reorder event
			else if (next_event instanceof ReorderEvent) {
				ReorderEvent reorder_event = (ReorderEvent) next_event;
				callbackReorderEvent(reorder_event);
			}
			
			// TODO: DEBUGGING PURPOSES ONLY
			deniedDemand = perf.deniedDemand.get(m);
			totalDemand = perf.totalDemand.get(m);
			nrStockouts = perf.countStockouts.get(m);
			nrCycles = perf.countCycles.get(m);
			FR = perf.fillRate(m);
			CSL = perf.CSL(m);
			IL = m.getInventoryLevel();
			IP = m.getInventoryPosition();
			
			// update holding costs
//			Material m = next_event.associatedMaterial();
			double prev_time_m = system_state.get(m);
			double hold_costs = holding_costs*Math.max(0, m.getInventoryLevel())*
					(scheduler.time() - prev_time_m)*m.getPrice();
//			total_holding += hold_costs;
			perf.addHoldingCosts(m, hold_costs);

			// update stockout costs (based on criticality class)
			if (m.isShort()) {
				switch (m.criticality()) {
				case "1":	total_stockout_H += stockout_costs_H;
				case "2": 	total_stockout_M += stockout_costs_M;
				case "3": 	total_stockout_L += stockout_costs_L;
				default:	total_stockout_Z += stockout_costs_Z;
				}
			}
			
			// update system state
			system_state.put(m, scheduler.time());
		}
				
		// we are done, wrap up the simulation by calculating service measures
		calculateServiceMeasures();
		s += summary();
		
		return s;
	}
	
	/**
	 * Exports the fill rates per material.
	 * 
	 * @return	Fill rates per material.
	 */
//	public Map<Material, Double> getFillRates() {
//		return fillRate;
//	}
	
	private String summary() {
		String s = "SIMULATION SUMMARY\n";
		s += "==================\n";
		
		// FILL RATE
		Map<Material, Double> fillRate = perf.fillRateAll();
		// determine min fill rate
		double min_fr = Stats.min(fillRate);
		// determine max fill rate
		double max_fr = Stats.max(fillRate);
		// determine mean fill rate
		double mean_fr = Stats.mean(fillRate);
		// determine std of fill rate
		double std_fr = Stats.std(fillRate);
		
		s += "fill rate\n";
		s += "---------\n";
		s += "min:\t" + min_fr + "\n";
		s += "mean:\t" + mean_fr + "\n";
		s += "max:\t" + max_fr + "\n";
		s += "std:\t" + std_fr + "\n\n";
		
		// FILL RATE
		Map<Material, Double> CSL = perf.CSLAll();
		// determine min fill rate
		double min_csl = Stats.min(CSL);
		// determine max fill rate
		double max_csl = Stats.max(CSL);
		// determine mean fill rate
		double mean_csl = Stats.mean(CSL);
		// determine std of fill rate
		double std_csl = Stats.std(CSL);

		s += "cycle service level\n";
		s += "---------\n";
		s += "min:\t" + min_csl + "\n";
		s += "mean:\t" + mean_csl + "\n";
		s += "max:\t" + max_csl + "\n";
		s += "std:\t" + std_csl + "\n\n";
		
		// OVERVIEW OF COSTS
		s += "cost overview\n";
		s += "-------------\n";
		s += "total fixed costs:\t\t" + perf.getTotalFixedCosts() + "\n";
		s += "total holding costs:\t\t" + perf.getTotalHoldingCosts() + "\n";
		s += "total marginal costs:\t\t" + perf.getTotalMarginalCosts() + "\n";
		s += "total backorder costs (H):\t" + total_stockout_H + "\n";
		s += "total backorder costs (M):\t" + total_stockout_M + "\n";
		s += "total backorder costs (L):\t" + total_stockout_L + "\n";
		s += "total backorder costs (Z):\t" + total_stockout_Z + "\n";
		s += "------------------------------------------\n";
		double total_costs = perf.getTotalCosts();
//				total_stockout_H + total_stockout_M + total_stockout_L + total_stockout_Z;
		s += "TOTAL:\t\t\t\t" + total_costs + "\n\n";
		
		// summary based on group
		s += "GROUP\n";
		s += "-----\n";
		s += "Demand\t\tCSL\t\t\tFR\t\t\tHC\t\t\tFC\t\tMC\n";
		double demand_total_holding = 0.0;
		double demand_total_fixed = 0.0;
		double demand_total_marg = 0.0;
		for (String group : demandGroups) {
			s += "\t" + group + "\t" + CSLDemandGroup.get(group) + "\t" + fillRateDemandGroup.get(group) + "\t" + 
		perf.getDemandGroupHoldingCosts(group) + "\t" + perf.getDemandGroupFixedCosts(group) + "\t" + perf.getDemandGroupMarginalCosts(group) + "\n";
			demand_total_fixed += perf.getDemandGroupFixedCosts(group);
			demand_total_holding += perf.getDemandGroupHoldingCosts(group);
			demand_total_marg += perf.getDemandGroupMarginalCosts(group);
		}
		s += "\t\t\t\t\t\t\t\t" + demand_total_holding + "\t" + demand_total_fixed + "\t" + demand_total_marg + "\n";
		s += "\n";
		s += "Price\n";
		
		double price_total_holding = 0.0;
		double price_total_fixed = 0.0;
		double price_total_marg = 0.0;
		for (String group : priceGroups) {
			s += "\t" + group + "\t" + CSLPriceGroup.get(group) + "\t" + fillRatePriceGroup.get(group) + "\t" + 
		perf.getPriceGroupHoldingCosts(group) + "\t" + perf.getPriceGroupFixedCosts(group) + "\t" + perf.getPriceGroupMarginalCosts(group) + "\n";
			price_total_fixed += perf.getPriceGroupFixedCosts(group);
			price_total_holding += perf.getPriceGroupHoldingCosts(group);
			price_total_marg += perf.getPriceGroupMarginalCosts(group);
		}
		s += "\t\t\t\t\t\t\t\t" + price_total_holding + "\t" + price_total_fixed + "\t" + price_total_marg + "\n";
		s += "\n";
		s += "Criticality\n";
		
		double crit_total_holding = 0.0;
		double crit_total_fixed = 0.0;
		double crit_total_marg = 0.0;
		for (String group : critGroups) {
			s += "\t" + group + "\t" + CSLCritGroup.get(group) + "\t" + fillRateCritGroup.get(group) + "\t" + 
		perf.getCritGroupHoldingCosts(group) + "\t" + perf.getCritGroupFixedCosts(group) + "\t" + perf.getCritGroupMarginalCosts(group) + "\n";
			crit_total_fixed += perf.getCritGroupFixedCosts(group);
			crit_total_holding += perf.getCritGroupHoldingCosts(group);
			crit_total_marg += perf.getCritGroupMarginalCosts(group);
		}
		s += "\t\t\t\t\t\t\t\t" + crit_total_holding + "\t" + crit_total_fixed + "\t" + crit_total_marg + "\n";
		s += "\n";
		s += "Combined\n";
		
		double combined_total_holding = 0.0;
		double combined_total_fixed = 0.0;
		double combined_total_marg = 0.0;
		for (String group : combinedGroups) {
			s += "\t" + group + "\t" + CSLCombinedGroup.get(group) + "\t" + fillRateCombinedGroup.get(group) + "\t" + 
		 perf.getCombinedGroupHoldingCosts(group) + "\t" + perf.getCombinedGroupFixedCosts(group) + "\t" + perf.getCombinedGroupMarginalCosts(group) + "\n";
			combined_total_fixed += perf.getCombinedGroupFixedCosts(group);
			combined_total_holding += perf.getCombinedGroupHoldingCosts(group);
			combined_total_marg += perf.getCombinedGroupMarginalCosts(group);
		}
		s += "\t\t\t\t\t\t\t\t" + combined_total_holding + "\t" + combined_total_fixed + "\t" + combined_total_marg + "\n";
		s += "\n";

		return s;
	}
	
	private void callbackReorderEvent(ReorderEvent re) {
		Material m = re.associatedMaterial();
		int amount = re.quantity();
		m.replenishInventoryLevel(amount);
		
		// cycle ended
//		stockout.remove(m);
//		perf.endCycle(m);
	}
	
	private void callbackConsumptionEvent(ConsumptionEvent ce) {
		Material m = ce.associatedMaterial();
		int consumption = ce.demand();
//		if (consumption > m.getMaxStock())
//			throw new IllegalStateException("Demand larger than max stock.");
		
		// we have a 'visit', so update counter
//		totalVisits.put(m, totalVisits.get(m) + 1);
		if (consumption > 0)
			perf.addDemand(m, consumption);
		
		// check if we are able to supply demand and update service levels
		int stockOnhand = Math.max(m.getInventoryLevel(), 0);
		if (consumption > stockOnhand) {
			// fill rate
//			fillDenials.put(m, fillDenials.get(m) + 1);
			perf.deniedDemand(m, consumption - stockOnhand);
		}
		
		// CSL
//		if (consumption > m.getInventoryPosition() && !stockout.contains(m)) {
		if (consumption > m.getInventoryLevel() && m.getInventoryLevel() > 0 && !currentStockouts.contains(m)) {
			// we just stocked out, so process it
//			stockouts.put(m, stockouts.get(m) + 1);
			perf.stockout(m);
			currentStockouts.add(m);
//			stockout.add(m);
		}
		
		// consume the material
		m.consume(consumption);
		
		// if stock falls below minimum allowed stock, we schedule reorder event
		if (m.doReorder()) {
			int lead_time = (int)Math.ceil(m.getLeadTime());
			int quantity = m.reorder();
			scheduler.addReorderEvent(m, quantity, scheduler.time() + lead_time);
			// new cycle started
			perf.startCycle(m);
			currentStockouts.remove(m);
			
			// update inventory position
			m.replenishInventoryPosition(quantity);
			
			// incur fixed reorder costs
			perf.addFixedCosts(m, fixed_costs);
			
			// incur marginal reorder costs
			double marg_costs = m.getPrice()*quantity;
			perf.addMarginalCosts(m, marg_costs);
		}
	}
	
//	private int detLeadTime(int lead_time) {
//		return (int)Math.ceil((double)lead_time/(double)30);
//	}
	
	private void calculateServiceMeasures() {		
		// init variables
		for (String group : demandGroups) {
			groupTotalDemandDemand.put(group, 0);
			fillRateDemandGroup.put(group, 0.0);
			CSLDemandGroup.put(group, 0.0);
			groupSizeDemand.put(group, 0);
		}
		for (String group : priceGroups) {
			groupTotalDemandPrice.put(group, 0);
			fillRatePriceGroup.put(group, 0.0);
			CSLPriceGroup.put(group, 0.0);
			groupSizePrice.put(group, 0);
		}
		for (String group : critGroups) {
			groupTotalDemandCrit.put(group, 0);
			fillRateCritGroup.put(group, 0.0);
			CSLCritGroup.put(group, 0.0);
			groupSizeCrit.put(group, 0);
		}
		for (String group : combinedGroups) {
			groupTotalDemandCombined.put(group, 0);
			fillRateCombinedGroup.put(group, 0.0);
			CSLCombinedGroup.put(group, 0.0);
			groupSizeCombined.put(group, 0);
		}
		
		// group counts
		for (Material m : materials) {
			String demandGroup = m.getDemandClass();
			String priceGroup = m.getPriceClass();
			String critGroup = m.criticality();
			String combinedGroup = m.getCombinedClass();
			
			groupSizeDemand.put(demandGroup, groupSizeDemand.get(demandGroup) + 1);
			groupSizePrice.put(priceGroup, groupSizePrice.get(priceGroup) + 1);
			groupSizeCrit.put(critGroup, groupSizeCrit.get(critGroup) + 1);
			groupSizeCombined.put(combinedGroup, groupSizeCombined.get(combinedGroup) + 1);
		}
		
		// determine total group weight
		for (Material m : materials) {
			String demandGroup = m.getDemandClass();
			String priceGroup = m.getPriceClass();
			String critGroup = m.criticality();
			String combinedGroup = m.getCombinedClass();
			
			groupTotalDemandDemand.put(demandGroup, groupTotalDemandDemand.get(demandGroup) + m.totalPositiveDemand());
			groupTotalDemandPrice.put(priceGroup, groupTotalDemandPrice.get(priceGroup) + m.totalPositiveDemand());
			groupTotalDemandCrit.put(critGroup, groupTotalDemandCrit.get(critGroup) + m.totalPositiveDemand());
			groupTotalDemandCombined.put(combinedGroup, groupTotalDemandCombined.get(combinedGroup) + m.totalPositiveDemand());
		}
		
		// determine weighted sum of all service levels
		for (Material m : materials) {
			String demandGroup = m.getDemandClass();
			String priceGroup = m.getPriceClass();
			String critGroup = m.criticality();
			String combinedGroup = m.getCombinedClass();
			
			CSLDemandGroup.put(demandGroup, CSLDemandGroup.get(demandGroup) + perf.CSL(m)*m.totalPositiveDemand());
			fillRateDemandGroup.put(demandGroup, fillRateDemandGroup.get(demandGroup) + perf.fillRate(m)*m.totalPositiveDemand());
			
			CSLPriceGroup.put(priceGroup, CSLPriceGroup.get(priceGroup) + perf.CSL(m)*m.totalPositiveDemand());
			fillRatePriceGroup.put(priceGroup, fillRatePriceGroup.get(priceGroup) + perf.fillRate(m)*m.totalPositiveDemand());
			
			CSLCritGroup.put(critGroup, CSLCritGroup.get(critGroup) + perf.CSL(m)*m.totalPositiveDemand());
			fillRateCritGroup.put(critGroup, fillRateCritGroup.get(critGroup) + perf.fillRate(m)*m.totalPositiveDemand());
			
			CSLCombinedGroup.put(combinedGroup, CSLCombinedGroup.get(combinedGroup) + perf.CSL(m)*m.totalPositiveDemand());
			fillRateCombinedGroup.put(combinedGroup, fillRateCombinedGroup.get(combinedGroup) + perf.fillRate(m)*m.totalPositiveDemand());
		}
		// divide by group size to determine average service levels per group
		for (String group : demandGroups) {
			CSLDemandGroup.put(group, CSLDemandGroup.get(group) / groupTotalDemandDemand.get(group));
			fillRateDemandGroup.put(group, fillRateDemandGroup.get(group) / groupTotalDemandDemand.get(group));
		}
		for (String group : priceGroups) {
			CSLPriceGroup.put(group, CSLPriceGroup.get(group) / groupTotalDemandPrice.get(group));
			fillRatePriceGroup.put(group, fillRatePriceGroup.get(group) / groupTotalDemandPrice.get(group));
		}
		for (String group : critGroups) {
			CSLCritGroup.put(group, CSLCritGroup.get(group) / groupTotalDemandCrit.get(group));
			fillRateCritGroup.put(group, fillRateCritGroup.get(group) / groupTotalDemandCrit.get(group));
		}
		for (String group : combinedGroups) {
			CSLCombinedGroup.put(group, CSLCombinedGroup.get(group) / groupTotalDemandCombined.get(group));
			fillRateCombinedGroup.put(group, fillRateCombinedGroup.get(group) / groupTotalDemandCombined.get(group));
		}
		
		// check if all materials are accounted for in combined class
//		for (String k : groupSizesCombined.keySet())
//			System.out.println(k + ": " + groupSizesCombined.get(k));
	}
	
	private void startup() {
		// all demand is known beforehand, so populate scheduler with all consumption events
		for (Material m : materials) {
			for (int t = 0; t < horizon; t++) {
				int material_demand = m.demand(t);
				
				// if there is a demand, add a consumption event
				if (material_demand != 0)
					scheduler.addConsumptionEvent(m, material_demand, t);
			}
		}
	}
	
	public void exportServiceMeasures(String prefix) throws IOException {
		// fill rate
		BufferedWriter bw = new BufferedWriter(new FileWriter(prefix + "_fill_rates.csv"));
		for (Material m : materials) {
			bw.write(m.getId() + "," + perf.fillRate(m));
			bw.newLine();
		}
		bw.flush();
		bw.close();
		
		// CSL
		bw = new BufferedWriter(new FileWriter(prefix + "_CSL.csv"));
		for (Material m : materials) {
			bw.write(m.getId() + "," + perf.CSL(m));
			bw.newLine();
		}
		bw.flush();
		bw.close();
		
		// Combined group - results
		bw = new BufferedWriter(new FileWriter(prefix + "_combined_class.csv"));
//		String s = "Price,Demand,Criticality,CSL,Fill rate,Fixed costs,Holding costs,Marginal costs,Total costs (no marginal),Total costs\n";
		bw.write("Price,Demand,Criticality,CSL,Fill rate,Fixed costs,Holding costs,Marginal costs,Total costs (no marginal),Total costs,Weights,Counts");
		bw.newLine();
		
		for (String group : combinedGroups) {
//			s += group.charAt(0) + ",";
			bw.write(group.charAt(0) + ",");
			if (group.length() == 3)
				bw.write(group.charAt(1) + "," + group.charAt(2) + ",");
			else
				bw.write(group.substring(1, 3) + "," + group.charAt(3) + ",");
			bw.write(CSLCombinedGroup.get(group) + ",");
			bw.write(fillRateCombinedGroup.get(group) + ",");
			bw.write(perf.getCombinedGroupFixedCosts(group) + ",");
			bw.write(perf.getCombinedGroupHoldingCosts(group) + ",");
			bw.write(perf.getCombinedGroupMarginalCosts(group) + ",");
			bw.write((perf.getCombinedGroupFixedCosts(group) + perf.getCombinedGroupHoldingCosts(group)) + ",");
			bw.write((perf.getCombinedGroupFixedCosts(group) + perf.getCombinedGroupHoldingCosts(group) + perf.getCombinedGroupMarginalCosts(group)) + ",");
			bw.write(groupTotalDemandCombined.get(group) + ",");
			bw.write(groupSizeCombined.get(group) + "");
			bw.newLine();
		}
		bw.flush();
		bw.close();
		
		// write summary
		bw = new BufferedWriter(new FileWriter(prefix + "_summary.txt"));
		bw.write(summary());
		bw.flush();
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(prefix + "_training.csv"));
//		bw.write("Price,Demand,Criticality,CSL,Fill rate,Fixed costs,Holding costs,Marginal costs,Total costs (no marginal),Total costs,Weights,Counts");
//		bw.newLine();
		
		for (Material m : materials) {
			bw.write(m.getId() + ",");
			bw.write(Math.round(m.getLeadTime()) + ",");
			bw.write(m.getReorderPoint() + ",");
			bw.write(m.getMaxStock() + ",");
			bw.write(0 + ",");
			bw.write(m.getPrice() + ",");
			bw.write(m.getCritH() + ",");
			bw.write(m.getCritM() + ",");
			bw.write(m.getCritL() + ",");
			bw.write(0 + ",");

			// demand
			for (int i = 0; i < m.getDemand().length; i++) {
				bw.write(m.getDemand()[i] + ",");
			}
			
			bw.write(m.getPriceClass() + ",");
			bw.write(m.getDemandClass() + ",");
			bw.write(m.criticality());
			bw.newLine();
		}
		bw.flush();
		bw.close();
		
		// write summary
		bw = new BufferedWriter(new FileWriter(prefix + "_summary.txt"));
		bw.write(summary());
		bw.flush();
		bw.close();
	}
	
	public Set<Material> importMaterials(String file_name) throws IOException {
		Set<Material> imported = new TreeSet<>();
		BufferedReader br = new BufferedReader(new FileReader(file_name));
		String line = br.readLine();
		boolean first = true;
		int monthSize = 30;
		while (line != null) {
			String[] part = line.split(",");
			int n = part.length;
			
			String demandClass = part[n-2];			
			String id = part[0];
			double lead_time = Double.parseDouble(part[1])/monthSize;
			int min_stock = Integer.parseInt(part[2]);
			int max_stock = Integer.parseInt(part[3]);
//			int current_stock = Integer.parseInt(part[4]);
			double price = Double.parseDouble(part[5]);
			int crit_H = Integer.parseInt(part[6]);
			int crit_M = Integer.parseInt(part[7]);
			int crit_L = Integer.parseInt(part[8]);
			String priceClass = part[n-3];
			
			// training
//			String demandClass = part[n-2];			
//			String id = part[0];
//			double lead_time = Double.parseDouble(part[1])/monthSize;
//			int min_stock = Integer.parseInt(part[2]);
//			int max_stock = Integer.parseInt(part[3]);
////			int current_stock = Integer.parseInt(part[4]);
//			double price = Double.parseDouble(part[5]);
//			int crit_H = Integer.parseInt(part[6]);
//			int crit_M = Integer.parseInt(part[7]);
//			int crit_L = Integer.parseInt(part[8]);
//			String priceClass = part[n-3];
			
			// test
//			String demandClass = part[n-2];			
//			String id = part[0];
//			double lead_time = Double.parseDouble(part[1])/monthSize;
//			int min_stock = Integer.parseInt(part[2]);
//			int max_stock = Integer.parseInt(part[3]);
////			int current_stock = Integer.parseInt(part[4]);
//			double price = Double.parseDouble(part[5]);
//			int crit_H = Integer.parseInt(part[6]);
//			int crit_M = Integer.parseInt(part[7]);
//			int crit_L = Integer.parseInt(part[8]);
//			String priceClass = part[n-3];
			
			// training
			int[] demand = new int[part.length-13];
			for (int i = 10; i < part.length-3; i++) {
				demand[i-10] = Integer.parseInt(part[i]);
			}
			
			// historical demand
			// demand periods start at column 11, and last 3 columns are material classes
//			int[] demand = new int[part.length-13];
//			for (int i = 10; i < part.length-3; i++) {
//				demand[i-10] = Integer.parseInt(part[i]);
//			}
			
			if (first) {
				first = false;
				horizon = demand.length;
			}
			
			// estimate policy
			Material m = new Material(id, price, min_stock, max_stock, lead_time, 
					crit_H, crit_M, crit_L, demand, demandClass, priceClass);
			imported.add(m);
			
			line = br.readLine();
		}
		br.close();
		
		return imported;
	}
	
	public Map<String, Double> getRealizedCSLCombined() {
		return CSLCombinedGroup;
	}
	
	public Map<String, Double> getRealizedFRCombined() {
		return fillRateCombinedGroup;
	}
	
	public Set<Material> getMaterials() {
		return materials;
	}
	
	public double totalHoldingCosts() {
		double costs = 0.0;
		for (String group : combinedGroups) {
			costs += perf.getCombinedGroupHoldingCosts(group);
		}
		return costs;
	}
	
	public double totalFixedCosts() {
		double costs = 0.0;
		for (String group : combinedGroups) {
			costs += perf.getCombinedGroupFixedCosts(group);
		}
		return costs;
	}
	
	public double totalMarginalCosts() {
		double costs = 0.0;
		for (String group : combinedGroups) {
			costs += perf.getCombinedGroupMarginalCosts(group);
		}
		return costs;
	}
	
}
