package controllers.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Plan;

public class ApiPlansController extends ApiController {
	
	public static void plans() {
		List<Plan> plans = Plan.getVisiblePlans();
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		for (Plan plan : plans) {
			Map<String, Object> sub = new HashMap<String, Object>();
			sub.put("name", plan.name);
			sub.put("active_torrents", plan.maxActiveTorrents);
			sub.put("diskspace_gb", plan.maxDiskspaceGb);
			sub.put("monthly_cost", plan.monthlyCost);
			sub.put("free_slots", plan.getTotalFreeSlots());
			ret.add(sub);
		}
		result(ret);
	}
	
	public static void renderPlans() {
		List<Plan> plans = Plan.getVisiblePlans();
		render("api/plans.html", plans);
	}
	
}
