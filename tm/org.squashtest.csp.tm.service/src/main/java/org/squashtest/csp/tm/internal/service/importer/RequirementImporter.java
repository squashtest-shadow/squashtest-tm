/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer;

import java.io.InputStream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReaderFactory;
import org.squashtest.csp.tm.internal.utils.archive.impl.ArchiveReaderFactoryImpl;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;


@Component
public class RequirementImporter {
	
	
	@Inject
	private RequirementLibraryNavigationService service;
	
	private RequirementParser parser = new RequirementParserImpl();
	
	
	public ImportSummary importExcelRequirements(InputStream excelStream, Long libraryId){
		
		ImportSummaryImpl summary = new ImportSummaryImpl();
		
		/* phase 1 : convert the content of the archive into Squash entities */
		
		RequirementHierarchyCreator creator = new RequirementHierarchyCreator();
		creator.setParser(parser);
		
		creator.create(excelStream);
		
		RequirementFolder root = creator.getNodes();
		summary.add(creator.getSummary());
		
		
//		/* phase 2 : merge with the actual database content */
//		
//		RequirementLibrary library = service.findLibrary(libraryId);	
//		
//		
//		RequirementLibraryMerger merger = new RequirementLibraryMerger();
//		merger.setLibraryService(service);
//		merger.mergeIntoLibrary(library, root);
//		
//		summary.add(merger.getSummary());
//		
//		
		return summary;
	}


	
	
}
