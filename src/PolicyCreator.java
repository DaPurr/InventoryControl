import java.util.Map;
import java.util.Set;

/**
 * The PolicyCreator interface is used for classes which create certain policies for a material.
 * 
 * @author Nemanja Milovanovic
 *
 */
public interface PolicyCreator {
	
	/**
	 * Creates a certain policy for a material given its demand, lead-time, and cycle service level.
	 * 
	 * @param demand Demand horizon for the material.
	 * @param leadTime Lead-time for the material.
	 * @param target Target fill rate for the material.
	 * @return
	 */
	public ReorderPolicy createPolicyCSL(Material m, double target);
	
	public Set<Material> createPolicyCSL(Set<Material> materials, Map<String, Double> mapTargetCSL);
	
	/**
	 * Creates a certain policy for a material given its demand, lead-time, and cycle service level.
	 * 
	 * @param demand Demand horizon for the material.
	 * @param leadTime Lead-time for the material.
	 * @param target Target CSL for the material.
	 * @return Order policy for the material.
	 */
	public ReorderPolicy createPolicyFR(Material m, double target);
	
}