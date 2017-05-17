/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.requirement;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * Controller for the management screen of Requirement Versions linked to other Requirement Versions.
 *
 * Created by jlor on 11/05/2017.
 */
@Controller
@RequestMapping("/requirement-versions/{requirementVersionId}/linked-requirement-versions")
public class LinkedRequirementVersionsManagerController {

	@Inject
	private InternationalizationHelper i18nHelper;
	@Inject
	private LinkedRequirementVersionManagerService linkedReqVersionManager;
	/*
	 * See VerifyingTestCaseManagerController.verifyingTCMapper
	 */
	private final DatatableMapper<String> linkedReqVersionMapper = new NameBasedMapper(5)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "name", Project.class)
		.mapAttribute("rv-reference", "reference", RequirementVersion.class)
		.mapAttribute("rv-name", "name", RequirementVersion.class)
		.mapAttribute("rv-role", "role", String.class)
		.map("milestone-dates", "endDate");

	@RequestMapping("/table")
	@ResponseBody
	public DataTableModel getLinkedRequirementVersionsTableModel(@PathVariable long requirementVersionId, DataTableDrawParameters params) {

		PagingAndSorting pas = new DataTableSorting(params, linkedReqVersionMapper);

		return buildLinkedRequirementVersionsModel(requirementVersionId, pas, params.getsEcho());
	}

	protected DataTableModel buildLinkedRequirementVersionsModel(long requirementVersionId, PagingAndSorting pas, String sEcho){
		PagedCollectionHolder<List<LinkedRequirementVersion>> holder = linkedReqVersionManager.findAllByRequirementVersion(
			requirementVersionId, pas);

		return new LinkedRequirementVersionsTableModelHelper(i18nHelper).buildDataModel(holder, sEcho);
	}
}
