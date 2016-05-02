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
package org.squashtest.tm.service.internal.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.infolist.InfoListItemComparatorSource;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
		RequirementVersionAdvancedSearchService {

	@PersistenceContext
	private EntityManager em;

	@Inject
	private ProjectDao projectDao;

	private static final SortField[] DEFAULT_SORT_REQUIREMENTS = new SortField[] {
			new SortField("requirement.project.name", SortField.Type.STRING, false),
			new SortField("reference", SortField.Type.STRING, false), new SortField("criticality", SortField.Type.STRING, false),
			new SortField("category", SortField.Type.STRING, false), new SortField("status", SortField.Type.STRING, false),
			new SortField("labelUpperCased", SortField.Type.STRING, false) };

	private static final List<String> LONG_SORTABLE_FIELDS = Arrays.asList("requirement.id", "versionNumber", "id",
			"requirement.versions", "testcases", "attachments");

	@Override
	public List<String> findAllUsersWhoCreatedRequirementVersions() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoCreatedRequirementVersions(projectIds);
	}

	@Override
	public List<String> findAllUsersWhoModifiedRequirementVersions() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoModifiedRequirementVersions(projectIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model, Locale locale) {

		Session session = em.unwrap(Session.class);

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(RequirementVersion.class).get();

		Query luceneQuery = buildLuceneQuery(qb, model);

		org.hibernate.Query hibQuery = ftSession.createFullTextQuery(luceneQuery, RequirementVersion.class);

		return  hibQuery.list();
	}

	private Sort getRequirementVersionSort(List<Sorting> sortings, MessageSource source, Locale locale) {

		if (sortings == null || sortings.isEmpty()) {
			return new Sort(DEFAULT_SORT_REQUIREMENTS);
		}

		boolean isReverse = true;
		SortField[] sortFieldArray = new SortField[sortings.size()];

		for (int i = 0; i < sortings.size(); i++) {

			if (SortOrder.ASCENDING == sortings.get(i).getSortOrder()) {
				isReverse = false;
			}

			String fieldName = sortings.get(i).getSortedAttribute();

			fieldName = formatSortedFieldName(fieldName);

			if (LONG_SORTABLE_FIELDS.contains(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.LONG, isReverse);
			} else if ("category".equals(fieldName)) {
				sortFieldArray[i] = new SortField(fieldName, new InfoListItemComparatorSource(source, locale),
						isReverse);
			} else {
				sortFieldArray[i] = new SortField(fieldName, SortField.Type.STRING, isReverse);
			}
		}

		return new Sort(sortFieldArray);

	}

	private String formatSortedFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("RequirementVersion.")) {
			result = fieldName.replaceFirst("RequirementVersion.", "");
		} else if (fieldName.startsWith("Requirement.")) {
			result = fieldName.replaceFirst("Requirement.", "requirement.");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "requirement.project.");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PagedCollectionHolder<List<RequirementVersion>> searchForRequirementVersions(AdvancedSearchModel model,
			PagingAndMultiSorting sorting, MessageSource source, Locale locale) {

		Session session = em.unwrap(Session.class);

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity(RequirementVersion.class).get();

		Query luceneQuery = buildLuceneQuery(qb, model);

		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0;
		if (luceneQuery != null) {
			Sort sort = getRequirementVersionSort(sorting.getSortings(), source, locale);
			org.hibernate.Query hibQuery = ftSession.createFullTextQuery(luceneQuery, RequirementVersion.class)
					.setSort(sort);

			countAll = hibQuery.list().size();

			result = hibQuery.setFirstResult(sorting.getFirstItemIndex()).setMaxResults(sorting.getPageSize()).list();


		}
		return new PagingBackedPagedCollectionHolder<>(sorting, countAll, result);
	}


}
