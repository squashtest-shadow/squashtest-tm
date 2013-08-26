/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.TeamFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.users.PartyControllerSupport;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
import org.squashtest.tm.web.internal.security.authentication.AuthenticationProviderContext;

@Controller
@RequestMapping("/administration/users")
public class UserAdministrationController extends PartyControllerSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserAdministrationController.class);
	private static final String USER_URL = "/{userId}";

	private static final PagingAndSorting TEAMS_DEFAULT_PAGING = new DefaultPagingAndSorting("name");
	private static final Filtering TEAMS_DEFAULT_FILTERING = DefaultFiltering.NO_FILTERING;

	private AdministrationService adminService;

	@Inject
	private TeamFinderService teamFinderService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private AuthenticationProviderContext authenticationProviderContext;

	private DatatableMapper<String> userMapper = new NameBasedMapper(10)
			.mapAttribute("user-id", "id")
			.mapAttribute("user-login", "login")
			.mapAttribute("user-group", "group")
			.mapAttribute("user-firstname", "firstName")
			.mapAttribute("user-lastname", "lastName")
			.mapAttribute("user-email", "email")
			.mapAttribute("user-created-on", "audit.createdOn")
			.mapAttribute("user-created-by", "audit.createdBy")
			.mapAttribute("user-modified-on", "audit.lastModifiedOn")
			.mapAttribute("user-modified-by", "audit.lastModifiedBy");

	private DatatableMapper<String> permissionMapper = new NameBasedMapper(2).mapAttribute("project-name",
			"project.name", ProjectPermission.class).mapAttribute("permission-name",
			"permissionGroup.qualifiedName", ProjectPermission.class);

	@ServiceReference
	public void setAdministrationService(AdministrationService adminService) {
		this.adminService = adminService;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ModelAndView getUserList(Locale locale) {

		ModelAndView mav = new ModelAndView("page/users/show-users");

		List<UsersGroup> list = adminService.findAllUsersGroupOrderedByQualifiedName();

		PagingAndSorting paging = new DefaultPagingAndSorting("User.login");
		Filtering filter = DefaultFiltering.NO_FILTERING;

		DataTableModel model = getTableModel(paging, filter, "noneed", locale);

		mav.addObject("usersGroupList", list);
		mav.addObject("userList", model.getAaData());

		PagedCollectionHolder<List<Team>> teams = teamFinderService.findAllFiltered(TEAMS_DEFAULT_PAGING,
				TEAMS_DEFAULT_FILTERING);
		mav.addObject("pagedTeams", teams);
		mav.addObject("teamsPageSize", TEAMS_DEFAULT_PAGING.getPageSize());
		return mav;
	}

	@RequestMapping(value = "/table", params = RequestParams.S_ECHO_PARAM, method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTable(final DataTableDrawParameters params, final Locale locale) {
		LOGGER.trace("getTable called ");

		DataTableSorting sorting = createSorting(params, userMapper);
		Filtering filtering = new DataTableFiltering(params);

		return getTableModel(sorting, filtering, params.getsEcho(), locale);

	}

	private DataTableModel getTableModel(PagingAndSorting sorting, Filtering filtering, String sEcho, Locale locale) {
		FilteredCollectionHolder<List<User>> holder = adminService.findAllActiveUsersFiltered(sorting, filtering);

		return new UserDataTableModelBuilder(locale).buildDataModel(holder, sorting.getFirstItemIndex() + 1, sEcho);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "password")
	public @ResponseBody
	void addUser(@ModelAttribute("add-user") @Valid UserForm userForm) {
		if (!currentProviderFeatures().isManagedPassword()) {
			adminService.addUser(userForm.getUser(), userForm.getGroupId(), userForm.getPassword());

		} else {
			// If this happens, it's either a bug or a forged request
			LOGGER.warn(
					"Received a password while password are managed by auth provider. This is either a bug or a forged request. User form : {}",
					ToStringBuilder.reflectionToString(userForm));
			throw new IllegalArgumentException(
					"Received a password while password are managed by auth provider. This is either a bug or a forged request.");
		}
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "noPassword")
	public @ResponseBody
	void addUserWithoutCredentials(@ModelAttribute("add-user") @Valid UserForm userForm) {
		if (currentProviderFeatures().isManagedPassword()) {
			adminService.createUserWithoutCredentials(userForm.getUser(), userForm.getGroupId());

		} else {
			// If this happens, it's either a bug or a forged request
			LOGGER.warn(
					"Received no password while password are managed by Squash. This is either a bug or a forged request. User form : {}",
					ToStringBuilder.reflectionToString(userForm));
			throw new IllegalArgumentException(
					"Received no password while password are managed by Squash. This is either a bug or a forged request.");
		}
	}

	@SuppressWarnings("rawtypes")
	private DataTableSorting createSorting(final DataTableDrawParameters params, final DatatableMapper mapper) {
		return new DataTableSorting(params, mapper);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.DELETE)
	public @ResponseBody
	void removeUser(@PathVariable long userId) {
		adminService.deactivateUser(userId);
	}

	/**
	 * Will return a view for the user of the given id
	 * 
	 * @param userId
	 */
	@RequestMapping(value = USER_URL + "/info", method = RequestMethod.GET)
	public String getUserInfos(@PathVariable long userId, Model model) {
		User user = adminService.findUserById(userId);
		List<UsersGroup> usersGroupList = adminService.findAllUsersGroupOrderedByQualifiedName();

		List<?> permissionModel = createPermissionTableModel(userId, new DefaultPagingAndSorting(),
				DefaultFiltering.NO_FILTERING, "").getAaData();
		model.addAttribute("permissions", permissionModel);

		Map<String, Object> permissionPopupModel = getPermissionPopup(userId);
		model.addAttribute("permissionList", permissionPopupModel.get("permissionList"));
		model.addAttribute("projectList", permissionPopupModel.get("projectList"));

		model.addAttribute("usersGroupList", usersGroupList);
		model.addAttribute("user", user);

		return "user-modification.html";
	}

	@RequestMapping(value = USER_URL + "/change-group", method = RequestMethod.POST)
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

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = "password")
	@ResponseBody
	public void resetPassword(@ModelAttribute @Valid PasswordResetForm form, @PathVariable long userId) {
		LOGGER.trace("Reset password for user #" + userId);
		adminService.resetUserPassword(userId, form.getPassword());
	}

	@RequestMapping(value = USER_URL + "/authentication", method = RequestMethod.PUT, params = "password")
	@ResponseBody
	public void createAuthentication(@ModelAttribute @Valid PasswordResetForm form, @PathVariable long userId) {
		LOGGER.trace("Create authentication for user #" + userId);
		if (!currentProviderFeatures().isManagedPassword()) {
			adminService.createAuthentication(userId, form.getPassword());
		}
		// when password are managed, we should not create internal authentications.
	}

	// *********************************************************************************
	@RequestMapping(value = USER_URL + "/add-permission", method = RequestMethod.POST)
	public @ResponseBody
	void addNewPermission(@RequestParam("project") long projectId, @PathVariable long userId,
			@RequestParam String permission) {
		permissionService.addNewPermissionToProject(userId, projectId, permission);
	}

	@RequestMapping(value = USER_URL + "/remove-permission", method = RequestMethod.POST)
	public @ResponseBody
	void removePermission(@RequestParam("project") long projectId, @PathVariable long userId) {
		permissionService.removeProjectPermission(userId, projectId);
	}

	@RequestMapping(value = USER_URL + "/permission-popup", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, Object> getPermissionPopup(@PathVariable long userId) {
		return createPermissionPopupModel(userId);
	}

	@RequestMapping(value = USER_URL + "/permissions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	public @ResponseBody
	DataTableModel getPermissionTableModel(DataTableDrawParameters params, @PathVariable("userId") long userId) {
		PagingAndSorting paging = new DataTableSorting(params, permissionMapper);
		Filtering filtering = new DataTableFiltering(params);
		return createPermissionTableModel(userId, paging, filtering, params.getsEcho());
	}

	private String formatString(String arg, Locale locale) {
		if (arg == null) {
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale) {
		return messageSource.localizeDate(date, locale);

	}

	private String formatNoData(Locale locale) {
		return messageSource.noData(locale);
	}

	/**
	 * Builds datatable model for users table
	 */
	private final class UserDataTableModelBuilder extends DataTableModelBuilder<User> {
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
		public Map<?, ?> buildItemData(User item) {
			AuditableMixin newP = (AuditableMixin) item;
			String group = messageSource.internationalize("user.account.group." + item.getGroup().getQualifiedName()
					+ ".label", locale);
			if (group == null) {
				group = item.getGroup().getSimpleName();
			}

			Map<Object, Object> result = new HashMap<Object, Object>();
			result.put("user-id", item.getId());
			result.put("user-index", getCurrentIndex());
			result.put("user-login", item.getLogin());
			result.put("user-group", group);
			result.put("user-firstname", item.getFirstName());
			result.put("user-lastname", item.getLastName());
			result.put("user-email", item.getEmail());
			result.put("user-created-on", formatDate(newP.getCreatedOn(), locale));
			result.put("user-created-by", formatString(newP.getCreatedBy(), locale));
			result.put("user-modified-on", formatDate(newP.getLastModifiedOn(), locale));
			result.put("user-modified-by", formatString(newP.getLastModifiedBy(), locale));
			result.put("empty-delete-holder", null);

			return result;

		}
	}

	@ModelAttribute("authenticationProvider")
	public AuthenticationProviderFeatures getAuthenticationProviderModelAttribute() {
		return currentProviderFeatures();
	}

	private AuthenticationProviderFeatures currentProviderFeatures() {
		return authenticationProviderContext.getCurrentProviderFeatures();
	}

}
