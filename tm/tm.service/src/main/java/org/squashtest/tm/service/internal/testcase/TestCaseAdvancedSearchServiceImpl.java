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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.SearchExportCSVModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.requirement.InfoListItemComparatorSource;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

@Service("squashtest.tm.service.TestCaseAdvancedSearchService")
public class TestCaseAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
TestCaseAdvancedSearchService {

	@Inject
	protected SessionFactory sessionFactory;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	private RequirementVersionAdvancedSearchService requirementSearchService;

	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;

	@Inject
	private TestCaseCallTreeFinder testCaseCallTreeFinder;

	@Inject
	private Provider<TestCaseSearchExportCSVModelImpl> testCaseSearchExportCSVModelProvider;

	@Inject
	private MessageSource source;

	private final static SortField[] DEFAULT_SORT_TESTCASES = new SortField[] {
		new SortField("project.name", SortField.STRING, false),
		new SortField("reference", SortField.STRING, false), new SortField("importance", SortField.STRING, false),
		new SortField("label", SortField.STRING, false) };

	private final static List<String> LONG_SORTABLE_FIELDS = Arrays.asList("requirements", "steps", "id", "iterations",
			"attachments");

	@Override
	public List<String> findAllUsersWhoCreatedTestCases() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<Long>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoCreatedTestCases(projectIds);
	}

	@Override
	public List<String> findAllUsersWhoModifiedTestCases() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<Long>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoModifiedTestCases(projectIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> searchForTestCases(AdvancedSearchModel model, Locale locale) {

		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(TestCase.class).get();

		Query luceneQuery = buildLuceneQuery(qb, model, locale);

		org.hibernate.Query hibQuery = ftSession.createFullTextQuery(luceneQuery, TestCase.class);
		return hibQuery.list();

	}

	@Override
	public List<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model, Locale locale) {
		List<RequirementVersion> requirements = requirementSearchService.searchForRequirementVersions(model, locale);
		List<TestCase> result = new ArrayList<TestCase>();
		Set<TestCase> testCases = new HashSet<TestCase>();
		// Get testcases from found requirements
		for (RequirementVersion requirement : requirements) {
			List<TestCase> verifiedTestCases = verifyingTestCaseManagerService.findAllByRequirementVersion(requirement
					.getId());
			testCases.addAll(verifiedTestCases);
		}

		// Get calling testcases
		Set<Long> callingTestCaseIds = new HashSet<Long>();
		for (TestCase testcase : testCases) {
			callingTestCaseIds.addAll(testCaseCallTreeFinder.getTestCaseCallers(testcase.getId()));
		}
		// add callees ids
		callingTestCaseIds.addAll(IdentifiedUtil.extractIds(testCases));
		// get all test cases
		result.addAll(testCaseDao.findAllByIds(callingTestCaseIds));
		return result;
	}

	private Sort getTestCaseSort(PagingAndMultiSorting multisorting) {

		Locale locale = LocaleContextHolder.getLocale();

		List<Sorting> sortings = multisorting.getSortings();

		if (sortings == null || sortings.size() == 0) {
			return new Sort(DEFAULT_SORT_TESTCASES);
		}

		boolean isReverse = true;
		SortField[] sortFieldArray = new SortField[sortings.size()];

		for (int i = 0; i < sortings.size(); i++) {
			if (SortOrder.ASCENDING.equals(sortings.get(i).getSortOrder())) {
				isReverse = false;
			}

			String fieldName = sortings.get(i).getSortedAttribute();
			fieldName = formatSortFieldName(fieldName);

			if (LONG_SORTABLE_FIELDS.contains(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, SortField.LONG, isReverse);
			}
			else if ("nature".equals(fieldName) || "type".equals(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, new InfoListItemComparatorSource(source,
						locale), isReverse);
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);
	}

	private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("TestCase.")) {
			result = fieldName.replaceFirst("TestCase.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		}
		return result;
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model,
			PagingAndMultiSorting sorting, Locale locale) {

		List<TestCase> testcases = searchForTestCasesThroughRequirementModel(model, locale);

		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(TestCase.class).get();

		Query luceneQuery = buildLuceneQuery(qb, testcases, locale);

		List<TestCase> result = Collections.emptyList();
		int countAll = 0;
		if (luceneQuery != null) {
			Sort sort = getTestCaseSort(sorting);
			org.hibernate.Query hibQuery = ftSession.createFullTextQuery(luceneQuery, TestCase.class).setSort(sort);

			countAll = hibQuery.list().size();

			result = hibQuery.setFirstResult(sorting.getFirstItemIndex()).setMaxResults(sorting.getPageSize()).list();
		}
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(sorting, countAll, result);
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCases(AdvancedSearchModel model,
			PagingAndMultiSorting sorting, Locale locale) {

		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(TestCase.class).get();

		Query luceneQuery = buildLuceneQuery(qb, model, locale);

		List<TestCase> result = Collections.emptyList();
		int countAll = 0;
		if (luceneQuery != null) {
			Sort sort = getTestCaseSort(sorting);
			org.hibernate.Query hibQuery = ftSession.createFullTextQuery(luceneQuery, TestCase.class).setSort(sort);
			countAll = hibQuery.list().size();

			result = hibQuery.setFirstResult(sorting.getFirstItemIndex()).setMaxResults(sorting.getPageSize()).list();
		}
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(sorting, countAll, result);
	}

	@Override
	public SearchExportCSVModel exportTestCaseSearchResultsToCSV(AdvancedSearchModel searchModel, Locale locale) {

		TestCaseSearchExportCSVModelImpl model = testCaseSearchExportCSVModelProvider.get();

		List<TestCase> testCases = this.searchForTestCases(searchModel, locale);
		model.setTestCases(testCases);
		model.setIterationService(iterationService);
		return model;
	}

}
