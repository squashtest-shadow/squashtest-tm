/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.importer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;

@Component
public class RequirementImporter {

	@Inject
	private RequirementLibraryNavigationService service;
	
	@Inject
	private MilestoneManagerService milestoneService;
	
	@Inject
	private FeatureManager featureManager;

	public static final String DEFAULT_CREATED_BY = "import";

	private RequirementParser parser = new RequirementParserImpl();

	public ImportSummary importExcelRequirements(InputStream excelStream, long libraryId) {

		ImportSummaryImpl summary = new ImportSummaryImpl();
		RequirementLibrary library = service.findCreatableLibrary(libraryId);

		/* phase 1 : convert the content of the archive into Squash entities */

		RequirementHierarchyCreator creator = new RequirementHierarchyCreator();
		creator.setParser(parser);
		creator.setProject(library.getProject());

		Map<RequirementFolder, List<PseudoRequirement>> organizedPseudoReqNodes = creator.create(excelStream);

		RequirementFolder root = creator.getNodes();
		summary.add(creator.getSummary());

		// /* phase 2 : merge with the actual database content */

		RequirementLibraryMerger merger = new RequirementLibraryMerger(service, milestoneService, featureManager);
		merger.mergeIntoLibrary(library, root, organizedPseudoReqNodes);

		summary.add(merger.getSummary());

		return summary;
	}

}
