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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
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
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.users.PartyControllerSupport;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

@Controller
@RequestMapping("/administration/users")
public class UserAdministrationController extends PartyControllerSupport {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserAdministrationController.class);
	private static final String USER_URL = "/{userId}";
	
	private AdministrationService adminService;

	@Inject
	private MessageSource messageSource;
	
	private DatatableMapper userMapper = new NameBasedMapper(10)
											.mapAttribute(User.class, "id", String.class, "user-id")
											.mapAttribute(User.class, "login", String.class, "user-login")
											.mapAttribute(User.class, "group", UsersGroup.class, "user-group")
											.mapAttribute(User.class, "firstName", String.class, "user-firstname")
											.mapAttribute(User.class, "lastName", String.class, "user-lastname")
											.mapAttribute(User.class, "email", String.class, "user-email")
											.mapAttribute(User.class, "audit.createdOn", Date.class, "user-created-on")
											.mapAttribute(User.class, "audit.createdBy", String.class, "user-created-by")
											.mapAttribute(User.class, "audit.lastModifiedOn", Date.class, "user-modified-on")
											.mapAttribute(User.class, "audit.lastModifiedBy", String.class, "user-modified-by");

	private DatatableMapper<String> permissionMapper = new NameBasedMapper(2)
											.mapAttribute(ProjectPermission.class, "project.name", String.class, "project-name")
											.mapAttribute(ProjectPermission.class, "permissionGroup.qualifiedName", String.class, "permission-name");
	
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
		
		DataTableModel model = getTableModel(paging, filter,  "noneed", locale);
		
		mav.addObject("usersGroupList", list);
		mav.addObject("userList", model.getAaData());
		
		return mav;
	}
	
	@RequestMapping(value = "/table", params = RequestParams.S_ECHO_PARAM, method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTable(final  DataTableDrawParameters params, final Locale locale) {
		LOGGER.trace("getTable called ");

		DataTableSorting sorting = createSorting(params, userMapper);
		Filtering filtering = new DataTableFiltering(params);

		return getTableModel(sorting, filtering, params.getsEcho(), locale);

	}
	
	private DataTableModel getTableModel(PagingAndSorting sorting, Filtering filtering, String sEcho, Locale locale){ 
		FilteredCollectionHolder<List<User>> holder = adminService.findAllActiveUsersFiltered(sorting, filtering);

		return new UserDataTableModelBuilder(locale).buildDataModel(holder, sorting.getFirstItemIndex() + 1,
				sEcho);	
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody
	void addNewUser(@ModelAttribute("add-user") @Valid UserForm userForm) {
		adminService.addUser(userForm.getUser(), userForm.getGroupId(), userForm.getPassword());
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
	@RequestMapping(value = USER_URL+"/info", method = RequestMethod.GET)
	public String getUserInfos(@PathVariable Long userId, Model model) {
		User user = adminService.findUserById(userId);
		List<UsersGroup> usersGroupList = adminService.findAllUsersGroupOrderedByQualifiedName();
		
		List<?> permissionModel = createPermissionTableModel(userId, new DefaultPagingAndSorting(), DefaultFiltering.NO_FILTERING, "").getAaData();
		model.addAttribute("permissions",permissionModel);
		
		Map<String,Object> permissionPopupModel = getPermissionPopup(userId);
		model.addAttribute("permissionList",permissionPopupModel.get("permissionList"));
		model.addAttribute("projectList",permissionPopupModel.get("projectList"));

		model.addAttribute("usersGroupList", usersGroupList);
		model.addAttribute("user", user);
		
		return "user-modification.html";
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
	public @ResponseBody Map<String,Object> getPermissionPopup(@PathVariable long userId) {
		return createPermissionPopupModel(userId);
	}

	@RequestMapping(value = USER_URL+"/permissions", method = RequestMethod.GET, params=RequestParams.S_ECHO_PARAM)
	public 	@ResponseBody DataTableModel getPermissionTableModel(DataTableDrawParameters params, @PathVariable("userId") long userId) {
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, permissionMapper, SortedAttributeSource.SINGLE_ENTITY);
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
		public Map<?,?> buildItemData(User item) {
			AuditableMixin newP = (AuditableMixin) item;
			String group = messageSource.getMessage("user.account.group." + item.getGroup().getQualifiedName() + ".label",
					null, locale);
			if (group == null) {
				group = item.getGroup().getSimpleName();
			}
			
			Map<Object,Object> result = new HashMap<Object, Object>();
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
	

	
}
