package controllers;

import com.openseedbox.models.User;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.cache.Cache;
import play.mvc.Before;

public class Base extends BaseController {

	@Before
	protected static void before() {
		User u = getCurrentUser();
		renderArgs.put("currentUser", u);

		String mode = Play.configuration.getProperty("application.mode");
		if (StringUtils.isEmpty(mode)) {
			mode = "dev";
		}
		renderArgs.put("mode", mode.toLowerCase());
	}

	protected static User getCurrentUser() {
		String cache_key = getCurrentUserCacheKey();
		User fromCache = Cache.get(cache_key, User.class);
		if (fromCache != null) {
			return fromCache;
		}
		if (session.contains("currentUserId")) {
			fromCache = User.findById(Long.parseLong(session.get("currentUserId")));
			Cache.set(getCurrentUserCacheKey(), fromCache, "10mn");
		}
		return fromCache;
	}

	protected static String getCurrentUserCacheKey() {
		return session.getId() + "_currentUser";
	}

	protected static String getActiveAccountCacheKey() {
		return session.getId() + "_activeAccount";
	}

	protected static void setMessageIsRaw() {
		flash.put("messageIsRaw", true);
	}
}
