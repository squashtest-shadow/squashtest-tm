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
import javax.inject.Provider;
import javax.transaction.Transaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseSearchExportCSVModel;
import org.squashtest.tm.service.campaign.IterationModificationService;
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

	@Inject 
	private IterationModificationService iterationService;
	
	@Inject
	private Provider<TestCaseSearchExportCSVModelImpl> testCaseSearchExportCSVModelProvider;
	
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
	public List<TestCase> searchForTestCases() {
		
		Session session = sessionFactory.openSession();
 		
		FullTextSession ftSession = Search.getFullTextSession(session);
		
		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity( TestCase.class ).get();
		 		
		org.apache.lucene.search.Query query = qb.keyword().onFields("prerequisite").matching("Batman").createQuery();
		 
		 org.hibernate.Query hibQuery = ftSession.createFullTextQuery(query, TestCase.class);
		 
		List result = hibQuery.list();

		session.close();
		
		return result;
	}
	
	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCases(PagingAndSorting sorting) {

		Session session = sessionFactory.openSession();
		 		
		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity( TestCase.class ).get();
		 		
		org.apache.lucene.search.Query query = qb.keyword().onFields("prerequisite").matching("Batman").createQuery();
		 
		 org.hibernate.Query hibQuery = ftSession.createFullTextQuery(query, TestCase.class);
		 
		List result = hibQuery.list();
		
		session.close();
		 			
		List<TestCase> testCases = testCaseDao.findAll();
		Long countAll = new Long(testCases.size());
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(sorting, countAll, testCases);
	}

	@Override
	public TestCaseSearchExportCSVModel exportTestCaseSearchToCSV() {

		TestCaseSearchExportCSVModelImpl model = testCaseSearchExportCSVModelProvider.get();
		
		List<TestCase> testCases = testCaseDao.findAll();
		model.setTestCases(testCases);
		model.setIterationService(iterationService);	
		return model;
	}
}
