package org.squashtest.tm.service.security.customSecurity;

import java.util.Collection;

import javax.inject.Inject;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.user.UserManagerService;

public class StubCustomUserAccountService implements UserAccountService {

	@Inject
	UserManagerService userManager;

	@Override
	public void modifyUserFirstName(long userId, String newName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivateUser(long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activateUser(long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUser(long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public User findCurrentUser() {
		return userManager.findByLogin("chef");
	}

	@Override
	public void setCurrentUserEmail(String newEmail) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentUserPassword(String oldPasswd, String newPasswd) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Milestone> findAllMilestonesForUser(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
