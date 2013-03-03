package controllers.api;

import com.openseedbox.code.Util;
import com.openseedbox.models.User;
import java.util.List;

public class ApiUsers extends Api {
	
	/* GET /api/users/details */
	public static void details() {
		User u = getApiUser();
		result(Util.convertToMap(new Object[] {
			"stats", u.getUserStats(),
			"plan", u.getPlan()
		}));		
	}
		
	/* GET /api/users/groups */
	public static void groups() {
		List<String> groups = getApiUser().getGroups();
		result(Util.convertToMap(new Object[] {
			"groups", groups
		}));
	}
	
}
