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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.library.IndexModel;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchIndexMonitoring;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTextFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.domain.search.SearchExportCSVModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.library.AdvancedSearchService;
import org.squashtest.tm.service.project.ProjectManagerService;

@Service("squashtest.tm.service.AdvancedSearchService")
public class AdvancedSearchServiceImpl implements AdvancedSearchService {

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;

	@Inject
	private ProjectManagerService projectFinder;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private IterationModificationService iterationService;

	@Inject
	private Provider<TestCaseSearchExportCSVModelImpl> testCaseSearchExportCSVModelProvider;

	@Inject
	private Provider<RequirementVersionSearchExportCSVModelImpl> requirementVersionSearchExportCSVModelProvider;

	
	@Inject
	private ConfigurationService configurationService;
	
	private final static Integer EXPECTED_LENGTH = 7;
	public final static String REQUIREMENT_INDEXING_DATE_KEY = "lastindexing.requirement.date";
	public final static String TESTCASE_INDEXING_DATE_KEY = "lastindexing.testcase.date";
	public final static String CAMPAIGN_INDEXING_DATE_KEY = "lastindexing.campaign.date";

	public final static String REQUIREMENT_INDEXING_VERSION_KEY = "lastindexing.requirement.version";
	public final static String TESTCASE_INDEXING_VERSION_KEY = "lastindexing.testcase.version";
	public final static String CAMPAIGN_INDEXING_VERSION_KEY = "lastindexing.campaign.version";

	public final static String SQUASH_VERSION_KEY = "squashtest.tm.database.version";

	private final static SortField[] DEFAULT_SORT_TESTCASES = new SortField[]{
		new SortField("project.name", SortField.STRING, false),
		new SortField("reference", SortField.STRING, false),
		new SortField("importance", SortField.STRING, false),
		new SortField("label", SortField.STRING, false)
	};
	
	private final static SortField[] DEFAULT_SORT_REQUIREMENTS = new SortField[]{
			new SortField("requirement.project.name", SortField.STRING, false),
			new SortField("reference", SortField.STRING, false),
			new SortField("criticality", SortField.STRING, false),
			new SortField("category", SortField.STRING, false),
			new SortField("status", SortField.STRING, false),
			new SortField("label", SortField.STRING, false)
	};
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");

	@Override
	public IndexModel findIndexModel() {
		IndexModel model = new IndexModel();
		model.setRequirementIndexDate(findIndexDate(REQUIREMENT_INDEXING_DATE_KEY));
		model.setTestCaseIndexDate(findIndexDate(TESTCASE_INDEXING_DATE_KEY));
		model.setCampaignIndexDate(findIndexDate(CAMPAIGN_INDEXING_DATE_KEY));
		model.setRequirementIndexVersion(configurationService
				.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY));
		model.setTestcaseIndexVersion(configurationService
				.findConfiguration(TESTCASE_INDEXING_VERSION_KEY));
		model.setCampaignIndexVersion(configurationService
				.findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY));
		model.setCurrentSquashVersion(configurationService
				.findConfiguration(SQUASH_VERSION_KEY));
		return model;
	}

	private Date findIndexDate(String key) {
		String value = configurationService.findConfiguration(key);
		Date date = null;
		if (value != null) {
			try {
				date = dateFormat.parse(value);
			} catch (ParseException e) {

			}
		}
		return date;
	}

	@Override
	public void reindexTestCase(Long testCaseId){
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);
		Object testCase = ftSession.load(TestCase.class, testCaseId);
		ftSession.index(testCase);
	}

	@Override
	public void reindexTestCases(List<TestCase> testCaseList){
		for(TestCase testcase : testCaseList){
			 reindexTestCase(testcase.getId());
		}
	}
	
	@Override
	public void reindexRequirementVersion(Long requirementVersionId){
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);
		Object requirementVersion = ftSession.load(RequirementVersion.class, requirementVersionId);
		ftSession.index(requirementVersion);
	}

	@Override
	public void reindexRequirementVersions(List<RequirementVersion> requirementVersionList){
		for(RequirementVersion requirementVersion : requirementVersionList){
			 reindexRequirementVersion(requirementVersion.getId());
		}
	}
	
	@Override
	public void indexRequirementVersions(){

		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		List<Class> domains = new ArrayList<Class>();
		domains.add(RequirementVersion.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);

		ftSession.createIndexer(RequirementVersion.class).purgeAllOnStart(true)
					.threadsToLoadObjects(1).threadsForSubsequentFetching(1)
					.batchSizeToLoadObjects(10).cacheMode(CacheMode.NORMAL)
					.progressMonitor(monitor).start();
	}

	@Override
	public void indexAll(){
		AdvancedSearchIndexMonitoring.reset();
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		List<Class> domains = new ArrayList<Class>();
		domains.add(TestCase.class);
		domains.add(RequirementVersion.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);
		
		ftSession.createIndexer(TestCase.class, RequirementVersion.class).purgeAllOnStart(true)
					.threadsToLoadObjects(1).threadsForSubsequentFetching(1)
					.batchSizeToLoadObjects(10).cacheMode(CacheMode.IGNORE)
					.progressMonitor(monitor).start();
	}
	
	
	@Override
	public void indexTestCases(){

		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		List<Class> domains = new ArrayList<Class>();
		domains.add(TestCase.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);
		
		ftSession.createIndexer(TestCase.class).purgeAllOnStart(true)
					.threadsToLoadObjects(1).threadsForSubsequentFetching(1)
					.batchSizeToLoadObjects(10).cacheMode(CacheMode.NORMAL)
					.progressMonitor(monitor).start();
	}
		
	@Override
	public List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(
			BindableEntity entity) {

		Set<CustomField> result = new LinkedHashSet<CustomField>();

		List<Project> readableProjects = projectFinder.findAllReadable();
		for (Project project : readableProjects) {
			result.addAll(customFieldBindingFinderService
					.findBoundCustomFields(project.getId(), entity));
		}

		return new ArrayList<CustomField>(result);
	}

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
	public List<String> findAllUsersWhoCreatedRequirementVersions() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<Long>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoCreatedRequirementVersions(projectIds);
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

	@Override
	public List<String> findAllUsersWhoModifiedRequirementVersions() {
		List<Project> readableProjects = projectFinder.findAllReadable();
		List<Long> projectIds = new ArrayList<Long>(readableProjects.size());
		for (Project project : readableProjects) {
			projectIds.add(project.getId());
		}
		return projectDao.findUsersWhoModifiedRequirementVersions(projectIds);
	}
	
	private String padRawValue(Integer rawValue){
		StringBuilder builder = new StringBuilder();
		int length = Integer.toString(rawValue).length();
		int zeroesToAdd = EXPECTED_LENGTH - length;
		for(int i=0; i<zeroesToAdd; i++){
			builder.append("0");
		}
		builder.append(Integer.toString(rawValue));
		return builder.toString();
	}

	private org.apache.lucene.search.Query buildLuceneRangeQuery(
			QueryBuilder qb, String fieldName, Integer minValue,
			Integer maxValue) {

		org.apache.lucene.search.Query query = null;
		
		
		if (minValue == null) {

			String paddedMaxValue = padRawValue(maxValue);
					
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge()
							.below(paddedMaxValue).createQuery()).createQuery();

		} else if (maxValue == null) {

			String paddedMinValue = padRawValue(minValue);
			
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge()
							.above(paddedMinValue).createQuery()).createQuery();

		} else {

			String paddedMaxValue = padRawValue(maxValue);
			String paddedMinValue = padRawValue(minValue);
			
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).ignoreFieldBridge()
							.from(paddedMinValue).to(paddedMaxValue).createQuery()).createQuery();
		}

		return query;
	}

	private org.apache.lucene.search.Query buildLuceneValueInListQuery(
			QueryBuilder qb, String fieldName, List<String> values) {

		org.apache.lucene.search.Query mainQuery = null;

		for (String value : values) {
			
			if("".equals(value.trim())){
				value = "$NO_VALUE";
			}
			
			org.apache.lucene.search.Query query = qb
					.bool()
					.should(qb.keyword().onField(fieldName).ignoreFieldBridge()
							.matching(value).createQuery()).createQuery();

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().should(mainQuery).should(query)
						.createQuery();
			}
		}

		return qb.bool().must(mainQuery).createQuery();
	}

	private org.apache.lucene.search.Query buildLuceneSingleValueQuery(
			QueryBuilder qb, String fieldName, List<String> values,
			boolean ignoreBridge) {


		org.apache.lucene.search.Query mainQuery = null;
		
		for(String value : values){
			
			org.apache.lucene.search.Query query;

			if (ignoreBridge) {
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).ignoreFieldBridge()
								.sentence(value).createQuery()).createQuery();
			} else {
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).sentence(value)
								.createQuery()).createQuery();
			}
			
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}


		return mainQuery;
	}

	private org.apache.lucene.search.Query buildLuceneTextQuery(
			QueryBuilder qb, String fieldName, List<String> values,
			boolean ignoreBridge) {

		org.apache.lucene.search.Query mainQuery = null;
		
		for(String value : values){
			
			org.apache.lucene.search.Query query;
	
			if (ignoreBridge) {
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).ignoreFieldBridge().sentence(value).createQuery()).createQuery();
			} else {
				query = qb
						.bool()
						.must(qb.phrase().onField(fieldName).sentence(value).createQuery()).createQuery();
			}
			
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}
		return mainQuery;
	}

	private org.apache.lucene.search.Query buildLuceneTimeIntervalQuery(
			QueryBuilder qb, String fieldName, Date startdate, Date enddate) {

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.range()
						.onField(fieldName)
						.ignoreFieldBridge()
						.from(DateTools.dateToString(startdate,
								DateTools.Resolution.DAY))
						.to(DateTools.dateToString(enddate,
								DateTools.Resolution.DAY)).createQuery())
				.createQuery();

		return query;
	}

	private org.apache.lucene.search.Query buildLuceneTimeIntervalWithoutStartQuery(
			QueryBuilder qb, String fieldName, Date enddate) {

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.range()
						.onField(fieldName)
						.ignoreFieldBridge()
						.below(DateTools.dateToString(enddate,
								DateTools.Resolution.DAY)).createQuery())
				.createQuery();

		return query;
	}
	
	private org.apache.lucene.search.Query buildLuceneTimeIntervalWithoutEndQuery(
			QueryBuilder qb, String fieldName, Date startdate) {

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.range()
						.onField(fieldName)
						.ignoreFieldBridge()
						.above(DateTools.dateToString(startdate,
								DateTools.Resolution.DAY)).createQuery())
				.createQuery();

		return query;
	}
	
	private org.apache.lucene.search.Query buildQueryForSingleCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb, boolean ignoreBridge) {

		AdvancedSearchSingleFieldModel singleModel = (AdvancedSearchSingleFieldModel) fieldModel;
		if (singleModel.getValue() != null
				&& !"".equals(singleModel.getValue().trim())) {
			List<String> inputs = parseInput(singleModel.getValue());
	
			return buildLuceneSingleValueQuery(qb, fieldKey,
					inputs, ignoreBridge);
		}

		return null;
	}

	private org.apache.lucene.search.Query buildQueryForListCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {

		AdvancedSearchListFieldModel listModel = (AdvancedSearchListFieldModel) fieldModel;
		if (listModel.getValues() != null) {
			return buildLuceneValueInListQuery(qb, fieldKey,
					listModel.getValues());
		}

		return null;
	}

	private void addToTokens(List<String> tokens, String token){
		if(StringUtils.isNotBlank(token)){
			tokens.add(token);
		}

	}
	
	private List<String> parseInput(String textInput){
		
		List<String> tokens = new ArrayList<String>();
		boolean inDoubleQuoteContext = false;
		char[] input = textInput.toCharArray();
		int start = 0;

		for(int i=0; i<input.length; i++){
			
			//if we encounter a double quote at the very start 
			if(i == 0){
				if(input[i] == '"'){
					inDoubleQuoteContext = true;
					start = i+1;
				}
			} else {				
				//if we encounter a blank while NOT in the context of a double quote
				if(input[i] == ' ' && input[i-1] != ' ' && !inDoubleQuoteContext){
					addToTokens(tokens, textInput.substring(start, i).trim());
					start = i+1;
				}
				
				//if we encounter a double quote while in the context of a double quote
				else if(input[i] == '"' && input[i-1] != '\\' && inDoubleQuoteContext){	
					addToTokens(tokens, textInput.substring(start, i).trim());
					inDoubleQuoteContext = false;
					start = i+1;
				}
				
				//if we encounter a double quote while NOT in the context of a double quote
				else if(input[i] == '"' && input[i-1] != '\\' && !inDoubleQuoteContext){
					addToTokens(tokens, textInput.substring(start, i).trim());
					inDoubleQuoteContext = true;
					start = i+1;
				}
			}
		}
		
		if(input[input.length-1] != '"' && input[input.length-1] != ' '){
			addToTokens(tokens, textInput.substring(start, input.length).trim());
		}
		
		return tokens;
	}
	
	private org.apache.lucene.search.Query buildQueryForTextCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb, boolean ignoreBridge) {
		AdvancedSearchTextFieldModel textModel = (AdvancedSearchTextFieldModel) fieldModel;
		if (textModel.getValue() != null && !"".equals(textModel.getValue().trim())) {
			List<String> inputs = parseInput(textModel.getValue());
			return buildLuceneTextQuery(qb, fieldKey, inputs, ignoreBridge);
		}

		return null;
	}

	private org.apache.lucene.search.Query buildQueryForRangeCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {
		AdvancedSearchRangeFieldModel rangeModel = (AdvancedSearchRangeFieldModel) fieldModel;
		if (rangeModel.getMinValue() != null
				|| rangeModel.getMaxValue() != null) {
			return buildLuceneRangeQuery(qb, fieldKey,
					rangeModel.getMinValue(), rangeModel.getMaxValue());
		}

		return null;
	}

	private org.apache.lucene.search.Query buildQueryForTimeIntervalCriterium(
			String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {
		AdvancedSearchTimeIntervalFieldModel intervalModel = (AdvancedSearchTimeIntervalFieldModel) fieldModel;
		if (intervalModel.getStartDate() != null && intervalModel.getEndDate() != null) {
			return buildLuceneTimeIntervalQuery(qb, fieldKey, intervalModel.getStartDate(), intervalModel.getEndDate());
		} else if (intervalModel.getStartDate() == null && intervalModel.getEndDate() != null) {
			return buildLuceneTimeIntervalWithoutStartQuery(qb, fieldKey, intervalModel.getEndDate());
		} else if (intervalModel.getStartDate() != null && intervalModel.getEndDate() == null) {
			return buildLuceneTimeIntervalWithoutEndQuery(qb, fieldKey, intervalModel.getStartDate());
		}

		return null;
	}

	private org.apache.lucene.search.Query buildLuceneQuery(QueryBuilder qb,
			AdvancedSearchModel model) {

		org.apache.lucene.search.Query mainQuery = null;
		org.apache.lucene.search.Query query = null;

		Set<String> fieldKeys = model.getFields().keySet();

		for (String fieldKey : fieldKeys) {

			AdvancedSearchFieldModel fieldModel = model.getFields().get(
					fieldKey);
			String type = fieldModel.getType();
			boolean ignoreBridge = fieldModel.isIgnoreBridge();

			if (AdvancedSearchFieldModel.SINGLE.equals(type)) {
				query = buildQueryForSingleCriterium(fieldKey, fieldModel, qb, ignoreBridge);

			} else if (AdvancedSearchFieldModel.LIST.equals(type)) {
				query = buildQueryForListCriterium(fieldKey, fieldModel, qb);

			} else if (AdvancedSearchFieldModel.TEXT.equals(type)) {
				query = buildQueryForTextCriterium(fieldKey, fieldModel, qb, ignoreBridge);

			} else if (AdvancedSearchFieldModel.RANGE.equals(type)) {
				query = buildQueryForRangeCriterium(fieldKey, fieldModel, qb);

			} else if (AdvancedSearchFieldModel.TIME_INTERVAL.equals(type)) {
				query = buildQueryForTimeIntervalCriterium(fieldKey,
						fieldModel, qb);
			}

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}

		return mainQuery;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestCase> searchForTestCases(AdvancedSearchModel model) {

		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder()
				.forEntity(TestCase.class).get();

		org.apache.lucene.search.Query luceneQuery = buildLuceneQuery(qb, model);

		org.hibernate.Query hibQuery = ftSession.createFullTextQuery(
				luceneQuery, TestCase.class);

		return hibQuery.list();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model) {

		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder()
				.forEntity(RequirementVersion.class).get();

		org.apache.lucene.search.Query luceneQuery = buildLuceneQuery(qb, model);

		org.hibernate.Query hibQuery = ftSession.createFullTextQuery(
				luceneQuery, RequirementVersion.class);

		return hibQuery.list();

	}
	
	private Sort getRequirementVersionSort(List<Sorting> sortings, MessageSource source, Locale locale){
		boolean isReverse = true;
		Sort sort = null;

				
		if(sortings == null || sortings.size() == 0){
			
			sort = new Sort(DEFAULT_SORT_REQUIREMENTS);
		} else {
		
		SortField[] sortFieldArray = new SortField[sortings.size()];
		
		for (int i=0; i<sortings.size(); i++) {
	
			if (SortOrder.ASCENDING.equals(sortings.get(i).getSortOrder())) {
				isReverse = false;
			} 
	
				String fieldName = sortings.get(i).getSortedAttribute();
			
				if(fieldName.startsWith("RequirementVersion.")){
					fieldName = fieldName.replaceFirst("RequirementVersion.", "");
				} else if(fieldName.startsWith("Requirement.")){
					fieldName = fieldName.replaceFirst("Requirement.", "requirement.");
				} else if(fieldName.startsWith("Project.")){
					fieldName = fieldName.replaceFirst("Project.", "requirement.project.");
				}
				
				if("requirement.id".equals(fieldName) || "version".equals(fieldName) || 
				   "versions".equals(fieldName) || "testcases".equals(fieldName) || 
				   "attachments".equals(fieldName)){
					sortFieldArray[i] =  new SortField(fieldName, SortField.LONG, isReverse);
				} if("category".equals(fieldName)){
					sortFieldArray[i] =  new SortField(fieldName, new RequirementVersionCategoryComparatorSource(source, locale), isReverse);
				} else {
					sortFieldArray[i] = new SortField(fieldName, SortField.STRING, isReverse);
				}
			}
			
			sort = new Sort(sortFieldArray);
		}
		return sort;
	}
	
	private Sort getTestCaseSort(List<Sorting> sortings) {

		boolean isReverse = true;
		Sort sort = null;
		
		if(sortings == null || sortings.size() == 0){
			
			sort = new Sort(DEFAULT_SORT_TESTCASES);
			
		} else {
			SortField[] sortFieldArray = new SortField[sortings.size()];
			
			for (int i=0; i<sortings.size(); i++) {
		
				if (SortOrder.ASCENDING.equals(sortings.get(i).getSortOrder())) {
					isReverse = false;
				}
	
				String fieldName = sortings.get(i).getSortedAttribute();
	
				if (fieldName.startsWith("TestCase.")) {
					fieldName = fieldName.replaceFirst("TestCase.", "");
				} else if (fieldName.startsWith("Project.")) {
					fieldName = fieldName.replaceFirst("Project.", "project.");
				}
	
				if ("id".equals(fieldName) 
						|| "requirements".equals(fieldName)
						|| "steps".equals(fieldName)
						|| "iterations".equals(fieldName)
						|| "attachments".equals(fieldName)) {
					sortFieldArray[i] = new SortField(fieldName, SortField.LONG, isReverse);
				} else {
					sortFieldArray[i] = new SortField(fieldName, SortField.STRING, isReverse);
				}
			}
			
			sort = new Sort(sortFieldArray);
		}
		return sort;
	}
	
	@Override
	public PagedCollectionHolder<List<RequirementVersion>> searchForRequirementVersions(
			AdvancedSearchModel model, PagingAndMultiSorting sorting, MessageSource source, Locale locale) {
		
		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder()
				.forEntity(RequirementVersion.class).get();

		org.apache.lucene.search.Query luceneQuery = buildLuceneQuery(qb, model);

		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0 ;
		if(luceneQuery != null){
			Sort sort = getRequirementVersionSort(sorting.getSortings(), source, locale);
			org.hibernate.Query hibQuery = ftSession.createFullTextQuery(
					luceneQuery, RequirementVersion.class).setSort(sort);
	
			countAll = hibQuery.list().size();	
			
			
			result = hibQuery.setFirstResult(sorting.getFirstItemIndex())
					.setMaxResults(sorting.getPageSize()).list();
		}
		return new PagingBackedPagedCollectionHolder<List<RequirementVersion>>(sorting,
				countAll, result);
	}
	
	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCases(
			AdvancedSearchModel model, PagingAndMultiSorting sorting) {

		Session session = sessionFactory.getCurrentSession();

		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder()
				.forEntity(TestCase.class).get();

		org.apache.lucene.search.Query luceneQuery = buildLuceneQuery(qb, model);

		List<TestCase> result = Collections.emptyList();
		int countAll = 0 ;
		if(luceneQuery != null){
			Sort sort = getTestCaseSort(sorting.getSortings());
			org.hibernate.Query hibQuery = ftSession.createFullTextQuery(
					luceneQuery, TestCase.class).setSort(sort);
	
			countAll = hibQuery.list().size();	
			
			
			result = hibQuery.setFirstResult(sorting.getFirstItemIndex())
					.setMaxResults(sorting.getPageSize()).list();
		}
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(sorting,
				countAll, result);
	}

	@Override
	public SearchExportCSVModel exportTestCaseSearchResultsToCSV(AdvancedSearchModel searchModel) {

		TestCaseSearchExportCSVModelImpl model = testCaseSearchExportCSVModelProvider.get();

		List<TestCase> testCases = this.searchForTestCases(searchModel);
		model.setTestCases(testCases);
		model.setIterationService(iterationService);
		return model;
	}

	@Override
	public SearchExportCSVModel exportRequirementVersionSearchResultsToCSV(AdvancedSearchModel searchModel) {

		RequirementVersionSearchExportCSVModelImpl model = requirementVersionSearchExportCSVModelProvider.get();

		List<RequirementVersion> requirementVersions = this.searchForRequirementVersions(searchModel);
		model.setRequirementVersions(requirementVersions);
		return model;
	}
	
	@Override
	public Boolean isIndexedOnPreviousVersion() {
		String currentVersion = configurationService
				.findConfiguration(SQUASH_VERSION_KEY);
		String testcaseIndexVersion = configurationService
				.findConfiguration(TESTCASE_INDEXING_VERSION_KEY);
		String requirementIndexVersion = configurationService
				.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY);
		
		boolean result = currentVersion.equals(requirementIndexVersion) 
				&& currentVersion.equals(testcaseIndexVersion);

		// TODO uncomment when requirements and campains are added to advanced
		// search
		/*
		 * String campaignIndexVersion =
		 * configurationService
		 * .findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY);
		 * 
		 * boolean result = currentVersion.equals(requirementIndexVersion) &&
		 * currentVersion.equals(testcaseIndexVersion) &&
		 * currentVersion.equals(campaignIndexVersion);
		 */

		return !result;
	}
}
