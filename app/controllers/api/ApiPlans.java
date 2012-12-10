package controllers.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Plan;

public class ApiPlans extends Api {
	
	public static void plans() {
		List<Plan> plans = Plan.getVisiblePlans();
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		for (Plan plan : plans) {
			Map<String, Object> sub = new HashMap<String, Object>();
			sub.put("name", plan.getName());
			sub.put("active_torrents", plan.getMaxActiveTorrents());
			sub.put("diskspace_gb", plan.getMaxDiskspaceGb());
			sub.put("monthly_cost", plan.getMonthlyCost());
			sub.put("free_slots", plan.getTotalSlots());
			ret.add(sub);
		}
		result(ret);
	}
	
	public static void renderPlans() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("plans", Plan.getVisiblePlans());
		String rendered = renderToString("api/plans.html", args);
		result(rendered);
	}
	
}
