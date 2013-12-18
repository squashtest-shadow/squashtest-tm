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
package org.squashtest.tm.service.library;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.library.IndexModel;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.SearchExportCSVModel;
import org.squashtest.tm.domain.testcase.TestCase;



public interface AdvancedSearchService {

	//Indexing Test Cases
	void indexTestCases();
	
	void reindexTestCase(Long testCaseId);

	void reindexTestCases(List<TestCase> testCaseList);

	//Indexing Requirement Versions
	void indexRequirementVersions();
	
	void reindexRequirementVersion(Long requirementVersionId);
	
	void reindexRequirementVersions(List<RequirementVersion> requirementVersionList);	

	void reindexRequirementVersionsByIds(List<Long> requirementVersionsIds);
	
	//Indexing All
	void indexAll();
	
	IndexModel findIndexModel();
	
	Boolean isIndexedOnPreviousVersion();
	
	//Querying
	List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity);
	
	PagedCollectionHolder<List<TestCase>> searchForTestCases(AdvancedSearchModel model, PagingAndMultiSorting sorting, Locale locale);
	
	PagedCollectionHolder<List<TestCase>> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model, PagingAndMultiSorting sorting, Locale locale);
	
	PagedCollectionHolder<List<RequirementVersion>> searchForRequirementVersions(AdvancedSearchModel searchModel, PagingAndMultiSorting paging, MessageSource source, Locale locale);
	
	List<TestCase> searchForTestCases(AdvancedSearchModel model, Locale locale);

	List<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model, Locale locale);
	
	List<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model, Locale locale);

	List<String> findAllUsersWhoModifiedTestCases();

	List<String> findAllUsersWhoCreatedTestCases();

	List<String> findAllUsersWhoCreatedRequirementVersions();
	
	List<String> findAllUsersWhoModifiedRequirementVersions();
	
	//Exporting
	SearchExportCSVModel exportTestCaseSearchResultsToCSV(AdvancedSearchModel model, Locale locale);

	SearchExportCSVModel exportRequirementVersionSearchResultsToCSV(AdvancedSearchModel model, Locale locale);



}
