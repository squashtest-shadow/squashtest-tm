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
package org.squashtest.tm.service.internal.customreport;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;

@Service("org.squashtest.tm.service.customreport.CustomReportWorkspaceService")
public class CustomReportWorkspaceServiceImpl implements
		CustomReportWorkspaceService {
	
	@Inject
	private CustomReportLibraryDao dao;
	
	@Inject
	private CustomReportLibraryNodeDao crlnDao;

	@Override
	public List<CustomReportLibrary> findAllLibraries() {
		return dao.findAll();
	}

	@Override
	public List<CustomReportLibrary> findAllEditableLibraries() {
		throw new UnsupportedOperationException("IMPLEMENTS ME");
	}

	@Override
	public List<CustomReportLibrary> findAllImportableLibraries() {
		throw new UnsupportedOperationException("IMPLEMENTS ME");
	}

	@Override
	public List<TreeLibraryNode> findContent(Long libraryId) {
		return crlnDao.findChildren(libraryId);
	}

	@Override
	public List<CustomReportLibraryNode> findRootNodes() {
		return crlnDao.findAllByEntityType(CustomReportTreeDefinition.LIBRARY);
	}
	
}
