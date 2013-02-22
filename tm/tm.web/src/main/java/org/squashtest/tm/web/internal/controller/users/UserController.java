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

package org.squashtest.tm.web.internal.controller.users;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * @author mpagnon
 * 
 */
@Controller
@RequestMapping("/users")
public class UserController {

	@Inject
	private AdministrationService service;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	private static final String USER_ID_URL = "/{userId}";

	private DatatableMapper<String> teamsMapper = new NameBasedMapper(1).mapAttribute(Team.class, "name", String.class,
			"team-name");

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = USER_ID_URL + "/general")
	public String refreshGeneralInfos(@PathVariable("userId") long userId, Model model) {
		LOGGER.info("Refresh infos for user #{}", userId);
		User user = service.findUserById(userId);
		model.addAttribute("auditableEntity", user);
		return "fragments-utils/general-information-panel.html";
	}

	private ProjectsPermissionManagementService permissionService;
	
	@ServiceReference
	public void setProjectsPermissionManagementService(ProjectsPermissionManagementService permissionService) {
		this.permissionService = permissionService;
	}

	
	private DatatableMapper<String> permissionMapper = new NameBasedMapper(1)
														.mapAttribute(ProjectPermission.class, "project.name", String.class, "project-name");


	
	//*************************************permission*****************************
	@RequestMapping(value = USER_ID_URL + "/permissions", method = RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getPermissionTableModel(DataTableDrawParameters params, @PathVariable("userId") long userId) {
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, permissionMapper, SortedAttributeSource.SINGLE_ENTITY);
		Filtering filtering = new DataTableFiltering(params);
		return _getPermissionTableModel(userId, paging, filtering, params.getsEcho());
	}

	// ************************************ team section ************************

	@RequestMapping(value = USER_ID_URL + "/teams", method = RequestMethod.GET, params = "sEcho")
	@ResponseBody
	public DataTableModel getTeamsTableModel(DataTableDrawParameters params, @PathVariable("userId") long userId) {
		LOGGER.info("Find associated teams table model for user #{}", userId);
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, teamsMapper,
				SortedAttributeSource.SINGLE_ENTITY);
		Filtering filtering = new DataTableFiltering(params);
		return getTeamsTableModel(userId, paging, filtering, params.getsEcho());
	}

	@RequestMapping(value = USER_ID_URL + "/teams/{teamIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deassociateTeams(@PathVariable("userId") long userId, @PathVariable("teamIds") List<Long> teamIds) {
		LOGGER.info("Remove the user #{} from the given teams members.", userId);
		service.deassociateTeams(userId, teamIds);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = USER_ID_URL + "/non-associated-teams", headers = "Accept=application/json")
	@ResponseBody
	public Collection<TeamModel> getNonAssociatedTeams(@PathVariable("userId") long userId) {
		LOGGER.info("Find teams where user #{} is not a member.", userId);
		List<Team> nonAssociatedTeams = service.findAllNonAssociatedTeams(userId);
		return CollectionUtils.collect(nonAssociatedTeams, new TeamModelCreator());
	}

	@RequestMapping(value = USER_ID_URL + "/teams/{ids}", method = RequestMethod.PUT)
	@ResponseBody
	public void associateToTeams(@PathVariable("userId") long userId, @PathVariable("ids") List<Long> teamIds) {
		LOGGER.info("Add user #{} to given teams members.", userId);
		service.associateToTeams(userId, teamIds);
	}

	private DataTableModel getTeamsTableModel(long userId, PagingAndSorting paging, Filtering filtering, String secho) {
		PagedCollectionHolder<List<Team>> holder = service.findSortedAssociatedTeams(userId, paging, filtering);
		return new TeamsTableModelHelper().buildDataModel(holder, secho);
	}

	private static final class TeamModelCreator implements Transformer {
		@Override
		public Object transform(Object team) {
			return new TeamModel((Team) team);
		}
	}

	private static final class TeamsTableModelHelper extends DataTableModelHelper<Team> {
		private TeamsTableModelHelper() {
		}

		@Override
		protected Map<?, ?> buildItemData(Team item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("team-id", item.getId());
			res.put("team-index", getCurrentIndex());
			res.put("team-name", item.getName());
			res.put("empty-delete-holder", null);
			return res;
		}
	}

	private DataTableModel _getPermissionTableModel(long userId, PagingAndSorting paging, Filtering filtering, String secho){
		Locale locale = LocaleContextHolder.getLocale();
		PagedCollectionHolder<List<ProjectPermission>> holder = permissionService.findProjectPermissionByParty(userId,paging,filtering);
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();
		return new PermissionTableModelHelper(permissionList).buildDataModel(holder, secho);
	}

	private static final class PermissionTableModelHelper extends DataTableModelHelper<ProjectPermission> {

		private List<PermissionGroup> permissionList;
		
		private PermissionTableModelHelper(List<PermissionGroup> permissionList){
			this.permissionList = permissionList;
		}
		
		@Override
		public Map<String, Object> buildItemData(ProjectPermission item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("project-id",item.getProject().getId());
			res.put("project-index", getCurrentIndex());
			res.put("project-name",item.getProject().getName());
			res.put("permission-id",item.getPermissionGroup().getId());
			res.put("permission-name",item.getPermissionGroup().getQualifiedName());
			res.put("permission-simplename", item.getPermissionGroup().getSimpleName());
			res.put("permission-list", permissionList);
			res.put("empty-delete-holder", null);
			res.put("empty-permission-list-holder", null);
			return res;
		}
	}
}
