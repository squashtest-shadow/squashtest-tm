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
package org.squashtest.tm.web.internal.controller.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.web.internal.controller.project.ProjectModel;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;

/**
 * Superclass of team and user controllers (code factorization)
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class PartyControllerSupport {
	protected ProjectsPermissionManagementService permissionService;

	@Inject
	protected InternationalizationHelper messageSource;

	/**
	 * 
	 */
	public PartyControllerSupport() {
		super();
	}

	@ServiceReference
	public void setProjectsPermissionManagementService(ProjectsPermissionManagementService permissionService) {
		this.permissionService = permissionService;
	}

	protected Map<String, Object> createPermissionPopupModel(long partyId) {
		Locale locale = LocaleContextHolder.getLocale();
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();
		List<GenericProject> projectList = permissionService.findProjectWithoutPermissionByParty(partyId);

		List<PermissionGroupModel> permissionGroupModelList = new ArrayList<PermissionGroupModel>();
		if (permissionList != null) {
			for (PermissionGroup permission : permissionList) {
				PermissionGroupModel model = new PermissionGroupModel(permission);
				model.setDisplayName(messageSource.getMessage(
						"user.project-rights." + model.getSimpleName() + ".label", null, locale));
				permissionGroupModelList.add(model);

			}
		}

		List<ProjectModel> projectModelList = new ArrayList<ProjectModel>();
		if (projectList != null) {
			for (GenericProject project : projectList) {
				projectModelList.add(new ProjectModel(project));
			}
		}

		Map<String, Object> res = new HashMap<String, Object>();
		res.put("myprojectList", projectModelList);
		res.put("permissionList", permissionGroupModelList);

		return res;
	}

	protected DataTableModel createPermissionTableModel(long userId, PagingAndSorting paging, Filtering filtering,
			String secho) {
		Locale locale = LocaleContextHolder.getLocale();
		PagedCollectionHolder<List<ProjectPermission>> holder = permissionService.findProjectPermissionByParty(userId,
				paging, filtering);
		List<PermissionGroup> permissionList = permissionService.findAllPossiblePermission();
		return new PermissionTableModelHelper(locale, messageSource, permissionList).buildDataModel(holder, secho);
	}

	protected static final class PermissionTableModelHelper extends DataTableModelHelper<ProjectPermission> {

		private List<PermissionGroup> permissionList;
		private MessageSource messageSource;
		private Locale locale;

		private PermissionTableModelHelper(Locale locale, MessageSource messageSource,
				List<PermissionGroup> permissionList) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.permissionList = permissionList;
		}

		@Override
		public Map<String, Object> buildItemData(ProjectPermission item) {
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("project-id", item.getProject().getId());
			res.put("project-index", getCurrentIndex());
			res.put("project-name", item.getProject().getName());
			res.put("permission-id", item.getPermissionGroup().getId());
			res.put("permission-name", item.getPermissionGroup().getQualifiedName());
			res.put("permission-simplename", item.getPermissionGroup().getSimpleName());
			res.put("permission-displayname",
					messageSource.getMessage("user.project-rights." + item.getPermissionGroup().getSimpleName()
							+ ".label", null, locale));
			res.put("permission-list", permissionList);
			res.put("empty-delete-holder", null);
			res.put("empty-permission-list-holder", null);
			return res;
		}
	}

}