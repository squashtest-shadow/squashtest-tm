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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.project.ProjectManagerService;

@Service("squashtest.tm.service.AdvancedSearchService")
@Transactional(readOnly = true)
public class AdvancedSearchServiceImpl implements AdvancedSearchService {

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;

	@Inject
	private ProjectManagerService projectFinder;

	@Inject
	private TestCaseDao testCaseDao;

	@Override
	public List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity) {

		Set<CustomField> result = new HashSet<CustomField>();

		List<Project> readableProjects = projectFinder.findAllReadable();
		for (Project project : readableProjects) {
			result.addAll(customFieldBindingFinderService
					.findBoundCustomFields(project.getId(), entity));
		}

		return new ArrayList<CustomField>(result);
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCases(PagingAndSorting sorting) {

		List<TestCase> testCases = testCaseDao.findAll();
		Long countAll = new Long(testCases.size());
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(sorting, countAll, testCases);
	}
}
