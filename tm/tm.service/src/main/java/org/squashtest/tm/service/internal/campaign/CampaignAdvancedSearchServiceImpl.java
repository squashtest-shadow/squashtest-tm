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
package org.squashtest.tm.service.internal.campaign;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.AdvancedSearchService;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.infolist.InfoListItemComparatorSource;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;

@Service("squashtest.tm.service.CampaignAdvancedSearchService")
public class CampaignAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
		CampaignAdvancedSearchService {


	@Inject
	protected ProjectManagerService projectFinder;

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private MessageSource source;

	private final static SortField[] DEFAULT_SORT_EXECUTION = new SortField[] {
			new SortField("project.name", SortField.STRING, false),
			new SortField("campaign-name", SortField.STRING, false),
			new SortField("iteration-name", SortField.STRING, false),
			new SortField("iteration-name", SortField.STRING, false),
			new SortField("execution-id", SortField.STRING, false),
			new SortField("execution-mode", SortField.STRING, false),
			new SortField("execution-milestone-nb", SortField.STRING, false),
			new SortField("testsuite-execution", SortField.STRING, false),
			new SortField("execution-status", SortField.STRING, false),
			new SortField("execution-executed-by", SortField.STRING, false),
			new SortField("execution-executed-on", SortField.STRING, false),
			new SortField("execution-datasets", SortField.STRING, false) };

	private final static List<String> LONG_SORTABLE_FIELDS = Arrays.asList("");

	private static final String FAKE_TC_ID = "-9000";

	public List<String> findAllAuthorizedUsersForACampaign() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<Long>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoCanAccessProject(projectIds);
		/*
		 * List<String> temp = new ArrayList<String>(); return temp;
		 */

	}

	@Override
	public List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PagedCollectionHolder<List<Execution>> searchForCampaign(AdvancedSearchModel searchModel,
			PagingAndMultiSorting paging, Locale locale) {
		// Actually, it's more searchForExecution than searcgForCampaign

		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		/*
		 * QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(Campaign.class).get();
		 * 
		 * Query luceneQuery = buildLuceneQuery(qb, searchModel, locale);
		 */

		// Let's try without milestones
		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(Execution.class).get();
		Query luceneQuery = buildLuceneQuery(qb, searchModel, locale);

		// Query luceneQuery = searchExecutionQuery(searchModel, ftSession, locale);

		List<Execution> result = Collections.emptyList();
		int countAll = 0;
		if (luceneQuery != null) {
			Sort sort = getExecutionSort(paging);

			org.hibernate.Query hibQuery = ftSession.createFullTextQuery(luceneQuery, Execution.class).setSort(sort);

			// Trytest
			org.hibernate.Query hibQueryTest1 = ftSession.createFullTextQuery(luceneQuery, Execution.class);
			int countAll1 = hibQueryTest1.list().size();
			int countAllTest = countAll1;
			// End trytest

			countAll = hibQuery.list().size();

			// TODO : it should be good if this result get something...
			result = hibQuery.setFirstResult(paging.getFirstItemIndex()).setMaxResults(paging.getPageSize()).list();
		}

		// TODO please, don't return null there, it will explode everything. It did.
		return new PagingBackedPagedCollectionHolder<List<Execution>>(paging, countAll, result);

	}

	private Sort getExecutionSort(PagingAndMultiSorting multisorting) {

		Locale locale = LocaleContextHolder.getLocale();

		List<Sorting> sortings = multisorting.getSortings();

		if (sortings == null || sortings.size() == 0) {
			return new Sort(DEFAULT_SORT_EXECUTION);
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
			} else if ("nature".equals(fieldName) || "type".equals(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, new InfoListItemComparatorSource(source, locale),
						isReverse);
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);
	}

	private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("Execution.")) {
			result = fieldName.replaceFirst("Execution.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		} else if (fieldName.startsWith("Campaign.")) {
			result = fieldName.replaceFirst("Campaign.", "campaign.");
		}
		return result;
	}
	/*
	 * That implementation is special because we cannot process the milestones as usual. Indeed, we need the test cases
	 * that belongs both directly and indirectly to the milestone. That's why we use the method noMilestoneLuceneQuery.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService#searchForTestCases(org.squashtest.tm.domain.
	 * search.AdvancedSearchModel, java.util.Locale)
	 */
	/*
	 * TODO :
	 * 
	 * This method is basically an override of "buildLuceneQuery" defined in the superclass -> thus we could rename it
	 * accordingly. However in method "searchForTestCasesThroughRequirementModel" we must use the super implementation
	 * of "buildLuceneQuery" -> thus renaming "searchTestCaseQuery" to "buildLuceneQuery" could lead to ambiguity.
	 * 
	 * I don't know what to do about it.
	 */
	protected Query searchExecutionQuery(AdvancedSearchModel model, FullTextSession ftSession, Locale locale) {

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(Execution.class).get();

		/*
		 * we must not include the milestone criteria yet because it'll be the subject of a separate query.
		 * 
		 * Let's save the search model and create a milestone-stripped version of it
		 */

		AdvancedSearchModel modelCopy = model.shallowCopy();
		removeMilestoneSearchFields(model);

		// create the main query (search test cases, no milestones)
		Query luceneQuery = buildCoreLuceneQuery(qb, model, locale);

		// now add the test-cases specific milestones criteria
		if (shouldSearchByMilestones(modelCopy)) {
			luceneQuery = addAggregatedMilestonesCriteria(luceneQuery, qb, modelCopy, locale);
		}

		return luceneQuery;

	}

	// Behold copypasta from TCASSI

	public boolean shouldSearchByMilestones(AdvancedSearchModel model) {
		boolean enabled = getFeatureManager().isEnabled(Feature.MILESTONE);

		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) model.getFields().get(
				"searchByMilestone");
		boolean hasCriteria = (searchByMilestone != null && "true".equals(searchByMilestone.getValue()));

		return enabled && hasCriteria;
	}

	public Query addAggregatedMilestonesCriteria(Query mainQuery, QueryBuilder qb, AdvancedSearchModel modelCopy,
			Locale locale) {

		// find the milestones
		addMilestoneFilter(modelCopy);

		List<String> strMilestoneIds = ((AdvancedSearchListFieldModel) modelCopy.getFields().get("milestones.id"))
				.getValues();

		// NOW FIND THE CAMPAIGN DUDE CAAAAAAAAAAAAAAAAAAAAAAMPAIGN

		// now find the test cases
		Collection<Long> milestoneIds = new ArrayList<>(strMilestoneIds.size());
		for (String str : strMilestoneIds) {
			milestoneIds.add(Long.valueOf(str));
		}

		List<Long> lTestcaseIds = testCaseDao.findAllTestCasesLibraryNodeForMilestone(milestoneIds);
		List<String> testcaseIds = new ArrayList<>(lTestcaseIds.size());
		for (Long l : lTestcaseIds) {
			testcaseIds.add(l.toString());
		}

		// if no tc are found then use fake id so the lucene query will not find anything

		if (testcaseIds.isEmpty()) {
			testcaseIds.add(FAKE_TC_ID);
		}

		// finally, add a criteria that restrict the test case ids
		Query idQuery = buildLuceneValueInListQuery(qb, "id", testcaseIds, false);

		return qb.bool().must(mainQuery).must(idQuery).createQuery();

	}

}
