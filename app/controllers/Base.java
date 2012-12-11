package controllers;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.mvc.Before;

public class Base extends BaseController {
	@Before
	protected static void before() {
		User u = getCurrentUser();
		renderArgs.put("currentUser", u);
		//Account a = getActiveAccount();
		//renderArgs.put("activeAccount", a);
		
		String mode = play.Play.configuration.getProperty("application.mode");
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
		long currentUserId = 0l;
		if (session.contains("currentUserId")) {
			String s = session.get("currentUserId");
			currentUserId = Long.parseLong(s);
		}
		if (currentUserId > 0) {
			fromCache = User.findById(currentUserId);
			Cache.set(getCurrentUserCacheKey(), fromCache, "10mn");
		}
		return fromCache;
	}
/*
	protected static Account getActiveAccount() {
		//if no user, then there will be no account
		User u = getCurrentUser();
		if (u == null) {
			return null;
		}
		String cache_key = getActiveAccountCacheKey();
		Account fromCache = Cache.get(cache_key, Account.class);
		if (fromCache != null) {
			return fromCache;
		}
		long activeAccountId = 0;
		if (session.contains("activeAccountId")) {
			activeAccountId = Long.parseLong(session.get("activeAccountId"));
		} else {
			//no active account in session, default to the current user
			activeAccountId = getCurrentUser().getPrimaryAccount().id;
		}
		if (activeAccountId > 0) {
			fromCache = Account.findById(activeAccountId);
			Cache.set(getActiveAccountCacheKey(), fromCache, "10mn");
		}
		return fromCache;
	}*/

	protected static String getCurrentUserCacheKey() {
		return session.getId() + "_currentUser";
	}

	protected static String getActiveAccountCacheKey() {
		return session.getId() + "_activeAccount";
	}
}
