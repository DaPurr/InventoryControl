import java.util.Map;

public class Stats {
	public static double mean(Map<Material, Double> map) {
		double sum = 0.0;
		for (Material m : map.keySet()) {
			sum += map.get(m);
		}
		return sum/map.size();
	}
	
	public static double std(Map<Material, Double> map) {
		double sum = 0.0;
		double mean_d = mean(map);
		for (Material m : map.keySet()) {
			sum += Math.pow(mean_d - map.get(m), 2.0);
		}
		return Math.sqrt( sum/Math.max(map.size()-1, 1) );
	}
	
	public static double min(Map<Material, Double> map) {
		double min = Double.POSITIVE_INFINITY;
		for (Material m : map.keySet()) {
			double d = map.get(m);
			if (d < min)
				min = d;
		}
		return min;
	}
	
	public static double max(Map<Material, Double> map) {
		double max = Double.NEGATIVE_INFINITY;
		for (Material m : map.keySet()) {
			double d = map.get(m);
			if (d > max)
				max = d;
		}
		return max;
	}
}
