package code;

import java.util.UUID;
import models.InvitedUser;
import models.User;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserService;

public class SeedboxUserService implements UserService.Service {

	@Override
	public SocialUser find(UserId id) {
		User u = User.fromUserId(id);
		if (u == null) { return null; }
		SocialUser su = new SocialUser();
		su.email = u.emailAddress;
		su.displayName = u.displayName;
		su.avatarUrl = u.avatarUrl;
		su.password = u.password;
		su.lastAccess = u.lastAccess;
		su.isEmailVerified = u.isActivated;
		su.id = id;
		return su;
	}

	@Override
	public void save(SocialUser user) {
		User exists = User.fromSocialUser(user);
		User u = new User();
		if (exists != null) {
			u = exists;
		}
		u.authProviderType = user.id.provider.name();
		u.displayName = user.displayName;
		u.avatarUrl = user.avatarUrl;
		u.emailAddress = user.email;
		u.isActivated = user.isEmailVerified;
		u.maxDiskspaceGB = 1;
		u.isAdmin = false;
		u.lastAccess = user.lastAccess;
		u.password = user.password;
		if (user.id.provider == ProviderType.userpass) {
			u.oauthId = user.email;
			user.id.id = user.email;
		} else {
			u.oauthId = user.id.id;
		}
		if (exists != null) {
			u.update();
		} else {
			u.insert();
		}
	}

	@Override
	public String createActivation(SocialUser user) {
		User u = User.fromSocialUser(user);
		String uuid = UUID.randomUUID().toString();
		u.activationUuid = uuid;
		u.save();
		return uuid;
	}

	@Override
	public boolean activate(String uuid) {
		User u = User.all().filter("activationUuid", uuid).get();
		if (u != null) {
			u.isActivated = true;
			u.save();
			return true;
		}
		return false;
	}

	@Override
	public void deletePendingActivations() {
		User.all().filter("isActivated", false).delete();
	}
	
}
