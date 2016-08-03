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
package org.squashtest.tm.service.internal.campaign;


import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.project.ProjectManagerService;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Service("squashtest.tm.service.CampaignAdvancedSearchService")
public class CampaignAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	CampaignAdvancedSearchService {

	@Inject
	protected ProjectManagerService projectFinder;

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private ProjectsPermissionManagementService projectsPermissionManagementService;


	private static final SortField[] DEFAULT_SORT_EXECUTION = new SortField[]{
		new SortField("project.name", SortField.Type.STRING, false),
		new SortField("campaign-name", SortField.Type.STRING, false),
		new SortField("iteration-name", SortField.Type.STRING, false),
		new SortField("itpi-id", SortField.Type.STRING, false),
		new SortField("itpi-label", SortField.Type.STRING, false),
		new SortField("itpi-mode", SortField.Type.STRING, false),
		new SortField("itpi-status", SortField.Type.STRING, false),
		new SortField("itpi-executed-by", SortField.Type.STRING, false),
		new SortField("itpi-executed-on", SortField.Type.STRING, false),
		new SortField("itpi-datasets", SortField.Type.STRING, false)};

	// FIXME This list which contains sweet FA is used to decide when a field should be processed as a numeric field or a text field. Looks like horseshit to me.
	private static final List<String> LONG_SORTABLE_FIELDS = Collections.singletonList("");

	private static final String TEST_SUITE_ID_FIELD_NAME = "testSuites.id";
	private static final String ITERATION_ID_FIELD_NAME = "iteration.id";
	private static final String CAMPAIGN_ID_FIELD_NAME = "campaign.id";
	private static final String PROJECT_ID_FIELD_NAME = "project.id";


	@Override
	public List<String> findAllAuthorizedUsersForACampaign() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}

		return findUsersWhoCanAccessProject(projectIds);
	}

	private List<String> findUsersWhoCanAccessProject(List<Long> projectIds) {
		List<String> list = new ArrayList<>();

		List<PartyProjectPermissionsBean> findPartyPermissionBeanByProject = new ArrayList<>();

		for (Long projectId : projectIds) {
			findPartyPermissionBeanByProject.addAll(projectsPermissionManagementService
				.findPartyPermissionsBeanByProject(projectId));
		}

		for (PartyProjectPermissionsBean partyProjectPermissionsBean : findPartyPermissionBeanByProject) {
			if (partyProjectPermissionsBean.isUser()) {

				User user = (User) partyProjectPermissionsBean.getParty();
				list.add(user.getLogin());
			}
		}
		return list;
	}

	@Override
	public PagedCollectionHolder<List<IterationTestPlanItem>> searchForIterationTestPlanItem(AdvancedSearchModel searchModel,
		PagingAndMultiSorting paging, Locale locale) {


		FullTextEntityManager ftSession = Search.getFullTextEntityManager(entityManager);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(IterationTestPlanItem.class).get();
		Query luceneQuery = buildLuceneQuery(qb, searchModel);

		List<IterationTestPlanItem> result = Collections.emptyList();
		int countAll = 0;

		if (!checkSearchModelPerimeterIsEmpty(searchModel) && luceneQuery != null) {
			Sort sort = getExecutionSort(paging);

			FullTextQuery fullTextQuery = ftSession.createFullTextQuery(luceneQuery, IterationTestPlanItem.class).setSort(sort);

			// FIXME The 2 lines below seem to
			// FIXME 1. fetch all the data
			// FIXME 2. fetch the paged data
			// FIXME Looks like horseshit to me
			countAll = fullTextQuery.getResultList().size();
			result = fullTextQuery.setFirstResult(paging.getFirstItemIndex()).setMaxResults(paging.getPageSize()).getResultList();
		}

		// Please, don't return null there, it will explode everything. It did.
		return new PagingBackedPagedCollectionHolder<>(paging, countAll, result);

	}

	private boolean checkSearchModelPerimeterIsEmpty(AdvancedSearchModel searchModel) {
		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();
		return checkParamNullOrEmpty(fields.get(PROJECT_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(CAMPAIGN_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(ITERATION_ID_FIELD_NAME)) &&
			checkParamNullOrEmpty(fields.get(TEST_SUITE_ID_FIELD_NAME));
	}

	private boolean checkParamNullOrEmpty(AdvancedSearchFieldModel field) {
		if (field == null) {
			return true;
		}
		if (field.getType() != AdvancedSearchFieldModelType.LIST) {
			return false;
		}
		AdvancedSearchListFieldModel listField = (AdvancedSearchListFieldModel) field;
		return listField.getValues().isEmpty();
	}

	private Sort getExecutionSort(PagingAndMultiSorting multisorting) {


		List<Sorting> sortings = multisorting.getSortings();

		if (sortings == null || sortings.isEmpty()) {
			return new Sort(DEFAULT_SORT_EXECUTION);
		}

		boolean isReverse = true;
		SortField[] sortFieldArray = new SortField[sortings.size()];

		for (int i = 0; i < sortings.size(); i++) {
			if (SortOrder.ASCENDING == sortings.get(i).getSortOrder()) {
				isReverse = false;
			}

			String fieldName = sortings.get(i).getSortedAttribute();
			fieldName = formatSortFieldName(fieldName);

			if (LONG_SORTABLE_FIELDS.contains(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.LONG, isReverse);
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);
	}

	private String formatSortFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("IterationTestPlanItem.")) {
			result = fieldName.replaceFirst("IterationTestPlanItem.", "");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "project.");
		} else if (fieldName.startsWith("Campaign.")) {
			result = fieldName.replaceFirst("Campaign.", "campaign.");
		}
		return result;
	}

}
