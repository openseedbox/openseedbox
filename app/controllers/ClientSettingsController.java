package controllers;

import notifiers.Mails;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import models.User;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;

public class ClientSettingsController extends ClientController {

	public static void index() {
		User currentUser = getCurrentUser();
		//User actualUser = getActualUser();
		/*
		List<InvitedUser> invitedUsers = currentUser.getInvitedUsers();
		List<InvitedUser> pendingInvites = currentUser.getPendingInvites();
		List<InvitedUser> sharedAccounts = currentUser.getSharedAccounts();
		renderTemplate("client/settings.html", invitedUsers, pendingInvites,
				sharedAccounts, actualUser, currentUser);*/
	}
	
	public static void setTheme() {
		index();
	}
	
	public static void inviteUser(@Required @Email String emailAddress) {
		/*
		User u = getCurrentUser();
		//check email address hasnt already been added
		int num = InvitedUser.all().filter("invitingUser", u)
				.filter("emailAddress", emailAddress).count();
		if (num > 0) {
			Validation.addError("emailAddress", "Email address has already been added!");
		}
		//check email isnt that of current user
		if (emailAddress.equals(u.emailAddress)) {
			Validation.addError("emailAddress", "You cant invite yourself!");
		}
		if (Validation.hasErrors()) {
			params.flash();
			Validation.keep();
			index();
		}		
		InvitedUser iv = new InvitedUser();
		iv.emailAddress = emailAddress;
		iv.invitationDate = new Date();
		iv.accepted = false;
		iv.invitingUser = u;
		iv.insert();
		Mails.inviteUser(iv, getServerPath());
		index();*/
	}
	
	public static void removeInvitedUser(long id) {
		/*
		InvitedUser.findById(id).delete();
		index();*/
	}
	
	public static void acceptInvite(long id) {
		/*
		InvitedUser iv = InvitedUser.getByKey(id);
		iv.accepted = true;
		iv.actualUser = getCurrentUser();
		iv.save();
		index();*/
	}
	
	public static void rejectInvite(long id) {
		/*
		InvitedUser iv = InvitedUser.getByKey(id);
		iv.accepted = false;
		iv.actualUser = getCurrentUser();
		iv.save();
		index();		*/
	}
}
