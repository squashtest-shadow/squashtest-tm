/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.administration;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.audit.AuditableMixin;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.project.ProjectPermission;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UsersGroup;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.AdministrationService;
import org.squashtest.csp.tm.service.ProjectsPermissionManagementService;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFilterSorter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.viewmapper.DataTableMapper;

@Controller
@RequestMapping("/administration/users")
public class UserAdministrationController {

	/**
	 * Builds datatable model for users table
	 */
	private final class UserDataTableModelBuilder extends DataTableModelHelper<User> {
		/**
		 * 
		 */
		private final Locale locale;

		/**
		 * @param locale
		 */
		private UserDataTableModelBuilder(Locale locale) {
			this.locale = locale;
		}

		@Override
		public Object[] buildItemData(User item) {
			AuditableMixin newP = (AuditableMixin) item;
			String group = messageSource.getMessage("user.account.group." + item.getGroup().getQualifiedName() + ".label",
					null, locale);
			if (group == null) {
				group = item.getGroup().getSimpleName();
			}
			return new Object[] { 
					item.getId(), 
					getCurrentIndex(), 
					item.getLogin(), 
					group, 
					item.getFirstName(),
					item.getLastName(), item.getEmail(), formatDate(newP.getCreatedOn(), locale),
					formatString(newP.getCreatedBy(), locale), formatDate(newP.getLastModifiedOn(), locale),
					formatString(newP.getLastModifiedBy(), locale),""};
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(UserAdministrationController.class);
	private static final String USER_URL = "/{userId}";
	
	private AdministrationService adminService;
	private ProjectsPermissionManagementService permissionService;

	@Inject
	private MessageSource messageSource;
	
	private DataTableMapper userMapper = new DataTableMapper("users-list-table", User.class).initMapping(13)
	.mapAttribute(User.class, 2, "login", String.class)
	.mapAttribute(User.class, 3, "group", UsersGroup.class)
	.mapAttribute(User.class, 4, "firstName", String.class)
	.mapAttribute(User.class, 5, "lastName", String.class)
	.mapAttribute(User.class, 6, "email", String.class)
	.mapAttribute(User.class, 7, "audit.createdOn", Date.class)
	.mapAttribute(User.class, 8, "audit.createdBy", String.class)
	.mapAttribute(User.class, 9, "audit.lastModifiedOn", Date.class)
	.mapAttribute(User.class, 10, "audit.lastModifiedBy", String.class);

	@ServiceReference
	public void setAdministrationService(AdministrationService adminService) {
		this.adminService = adminService;
	}

	@ServiceReference
	public void setProjectsPermissionManagementService(ProjectsPermissionManagementService permissionService) {
		this.permissionService = permissionService;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ModelAndView getUserList() {

		ModelAndView mav = new ModelAndView("page/users/show-users");
		List<UsersGroup> list = adminService.findAllUsersGroupOrderedByQualifiedName();
		mav.addObject("usersGroupList", list);
		return mav;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody
	void addNewUser(@ModelAttribute("add-user") @Valid UserForm userForm) {
		adminService.addUser(userForm.getUser(), userForm.getGroupId(), userForm.getPassword());
	}
	
	@RequestMapping(value = "/table", params = "sEcho", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTable(final DataTableDrawParameters params, final Locale locale) {
		LOGGER.trace("getTable called ");

		CollectionSorting filter = createPaging(params, userMapper);

		FilteredCollectionHolder<List<User>> holder = adminService.findAllActiveUsersFiltered(filter);

		return new UserDataTableModelBuilder(locale).buildDataModel(holder, filter.getFirstItemIndex() + 1,
				params.getsEcho());
	}

	private CollectionSorting createPaging(final DataTableDrawParameters params, final DataTableMapper mapper) {
		return new DataTableFilterSorter(params, mapper);
	}

	@RequestMapping(value = USER_URL+"/remove", method = RequestMethod.POST)
	public @ResponseBody
	void removeUser(@PathVariable long userId) {
		adminService.deactivateUser(userId);
	}
	
	@RequestMapping(value = USER_URL+"/info", method = RequestMethod.GET)
	public ModelAndView getUserInfos(@PathVariable long userId) {
		User user = adminService.findUserById(userId);
		List<UsersGroup> usersGroupList = adminService.findAllUsersGroupOrderedByQualifiedName();
		ModelAndView mav = new ModelAndView("page/users/user-info");
		mav.addObject("usersGroupList", usersGroupList);
		mav.addObject("user", user);
		return mav;
	}

	@RequestMapping(value = USER_URL+"/change-group", method = RequestMethod.POST)
	public @ResponseBody
	void changeUserGroup(@PathVariable long userId, @RequestParam long groupId) {
		adminService.setUserGroupAuthority(userId, groupId);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-login", VALUE })
	@ResponseBody
	public String updateLogin(@RequestParam(VALUE) String userLogin, @PathVariable long userId) {
		adminService.modifyUserLogin(userId, userLogin);
		return HtmlUtils.htmlEscape(userLogin);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-first-name", VALUE })
	@ResponseBody
	public String updateFirstName(@RequestParam(VALUE) String firstName, @PathVariable long userId) {
		adminService.modifyUserFirstName(userId, firstName);
		return HtmlUtils.htmlEscape(firstName);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-last-name", VALUE })
	@ResponseBody
	public String updateLastName(@RequestParam(VALUE) String lastName, @PathVariable long userId) {
		adminService.modifyUserLastName(userId, lastName);
		return HtmlUtils.htmlEscape(lastName);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-email", VALUE })
	@ResponseBody
	public String updateEmail(@RequestParam(VALUE) String email, @PathVariable long userId) {
		adminService.modifyUserEmail(userId, email);
		return HtmlUtils.htmlEscape(email);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "newPassword" })
	@ResponseBody
	public void resetPassword(@ModelAttribute @Valid PasswordResetForm form, @PathVariable long userId) {
		LOGGER.trace("Reset password for user #" + userId);
		adminService.resetUserPassword(userId, form.getNewPassword());
	}

	// *********************************************************************************
	@RequestMapping(value = USER_URL+"/add-permission", method = RequestMethod.POST)
	public @ResponseBody
	void addNewPermission(@RequestParam("project") long projectId, @PathVariable long userId,
			@RequestParam String permission) {
		permissionService.addNewPermissionToProject(userId, projectId, permission);
	}

	@RequestMapping(value = USER_URL+"/remove-permission", method = RequestMethod.POST)
	public @ResponseBody
	void removePermission(@RequestParam("project") long projectId, @PathVariable long userId) {
		permissionService.removeProjectPermission(userId, projectId);
	}

	@RequestMapping(value = USER_URL+"/permission-popup", method = RequestMethod.GET)
	public ModelAndView getPermissionPopup(@PathVariable long userId) {
		User user = adminService.findUserById(userId);
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();
		List<Project> projectList = permissionService.findProjectWithoutPermissionByLogin(user.getLogin());

		ModelAndView mav = new ModelAndView("fragment/users/user-permission-popup");
		mav.addObject("user", user);
		mav.addObject("projectList", projectList);
		mav.addObject("permissionList", permissionList);
		return mav;
	}

	@RequestMapping(value = USER_URL+"/permission-table", method = RequestMethod.GET)
	public ModelAndView getPermissionTable(@PathVariable long userId) {
		User user = adminService.findUserById(userId);
		List<ProjectPermission> projectPermissions = permissionService.findProjectPermissionByLogin(user.getLogin());
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();

		ModelAndView mav = new ModelAndView("fragment/users/user-permission-table");
		mav.addObject("user", user);
		mav.addObject("permissionList", permissionList);
		mav.addObject("projectPermissionList", projectPermissions);
		return mav;
	}

	private String formatString(String arg, Locale locale) {
		if (arg == null) {
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale) {
		try {
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		} catch (Exception anyException) {
			return formatNoData(locale);
		}

	}

	private String formatNoData(Locale locale) {
		return messageSource.getMessage("squashtm.nodata", null, locale);
	}
}
