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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.jmx.IndexingProgressMonitor;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.library.IndexModel;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseSearchExportCSVModel;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.configuration.ConfigurationService;
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

	@Inject
	private ConfigurationService configurationService;

	private final static String REQUIREMENT_INDEXING_DATE_KEY = "lastindexing.requirement.date";
	private final static String TESTCASE_INDEXING_DATE_KEY = "lastindexing.testcase.date";
	private final static String CAMPAIGN_INDEXING_DATE_KEY = "lastindexing.campaign.date";
	
	private final static String REQUIREMENT_INDEXING_VERSION_KEY = "lastindexing.requirement.version";
	private final static String TESTCASE_INDEXING_VERSION_KEY = "lastindexing.testcase.version";
	private final static String CAMPAIGN_INDEXING_VERSION_KEY = "lastindexing.campaign.version";
	
	private final static String SQUASH_VERSION_KEY = "squashtest.tm.database.version";
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
	
	@Override
	public IndexModel findIndexModel(){
		IndexModel model = new IndexModel();
		model.setRequirementIndexDate(findIndexDate(REQUIREMENT_INDEXING_DATE_KEY));
		model.setTestCaseIndexDate(findIndexDate(TESTCASE_INDEXING_DATE_KEY));
		model.setCampaignIndexDate(findIndexDate(CAMPAIGN_INDEXING_DATE_KEY));
		model.setRequirementIndexVersion(configurationService.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY));
		model.setTestcaseIndexVersion(configurationService.findConfiguration(TESTCASE_INDEXING_VERSION_KEY));
		model.setCampaignIndexVersion(configurationService.findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY));		
		model.setCurrentSquashVersion(configurationService.findConfiguration(SQUASH_VERSION_KEY));	
		return model;
	}
	
	private Date findIndexDate(String key){
		String value = configurationService.findConfiguration(key);
		Date date = null;
		if(value != null){
			try {
				date = dateFormat.parse(value);
			} catch (ParseException e) {
	
			}
		}
		return date;
	}
	
	@Override
	public void indexTestCases(){
		
		Session session = sessionFactory.getCurrentSession();
 		
		FullTextSession ftSession = Search.getFullTextSession(session);
		
		MassIndexerProgressMonitor monitor = new IndexingProgressMonitor();
		
		try {

			ftSession 
			 .createIndexer(TestCase.class)
			 .purgeAllOnStart(true)
			 .batchSizeToLoadObjects(25)
			 .cacheMode(CacheMode.NORMAL)
			 .progressMonitor(monitor)
			 .startAndWait();

		} catch (InterruptedException e) {

		}

		Date indexingDate = new Date();
		this.configurationService.updateConfiguration(TESTCASE_INDEXING_DATE_KEY, dateFormat.format(indexingDate));
		String currentVersion = this.configurationService.findConfiguration(SQUASH_VERSION_KEY);
		this.configurationService.updateConfiguration(TESTCASE_INDEXING_VERSION_KEY, currentVersion);
	}
		
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

	private org.apache.lucene.search.Query buildLuceneRangeQuery(QueryBuilder qb, String fieldName, Integer minValue, Integer maxValue){

		org.apache.lucene.search.Query query = null;
		
		if(minValue == null){
		
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).below(maxValue).createQuery())
					.createQuery();
			
		} else if(maxValue == null){
			
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).above(minValue).createQuery())
					.createQuery();
		
		} else {
			
			query = qb
					.bool()
					.must(qb.range().onField(fieldName).above(minValue).createQuery())
					.must(qb.range().onField(fieldName).below(maxValue).createQuery())
					.createQuery();
		}
		
		return query;
	}

	private org.apache.lucene.search.Query buildLuceneValueInListQuery(QueryBuilder qb, String fieldName, List<String> values){

		StringBuilder builder = new StringBuilder();
		for(String value : values){
			builder.append(value+" ");
		}

		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.keyword().onField(fieldName).matching(builder.toString()).createQuery())
				.createQuery();

		return query;
	}

	private org.apache.lucene.search.Query buildLuceneSingleValueQuery(QueryBuilder qb, String fieldName, String value){
		
		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.keyword().onField(fieldName).matching(value).createQuery())
				.createQuery();

		return query;
	}
	
	private org.apache.lucene.search.Query buildLuceneTextQuery(QueryBuilder qb, String fieldName, String value){
		
		org.apache.lucene.search.Query query = qb
				.bool()
				.must(qb.phrase().onField(fieldName).sentence(value).createQuery())
				.createQuery();

		return query;
	}
	
	private org.apache.lucene.search.Query buildLuceneTimeIntervalQuery(QueryBuilder qb, String fieldName, Date startdate, Date enddate){
	
		org.apache.lucene.search.Query query = qb
					.bool()
					.must(qb
			    		.range()
			    		.onField(fieldName)
			    		.ignoreFieldBridge()
			    		.from(DateTools.dateToString(startdate, DateTools.Resolution.DAY))
			    		.to(DateTools.dateToString(enddate, DateTools.Resolution.DAY))
			    		.createQuery())
			    	.createQuery();
	
		return query;
	}


	@Override
	public List<TestCase> searchForTestCases() {
		
		Session session = sessionFactory.getCurrentSession();
 		
		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity( TestCase.class ).get();
		
		BooleanQuery query = new BooleanQuery();
		
		//ID

		//Reference
				
		//Label
				
		//Description
				
		//Prerequisite
			
		//Importance
		query.add(new BooleanClause(buildLuceneValueInListQuery(qb,"importance",new ArrayList<String>()),BooleanClause.Occur.MUST));
		
		//Nature
		query.add(new BooleanClause(buildLuceneValueInListQuery(qb,"nature",new ArrayList<String>()),BooleanClause.Occur.MUST));
		
		//Type
		query.add(new BooleanClause(buildLuceneValueInListQuery(qb,"type",new ArrayList<String>()),BooleanClause.Occur.MUST));
		
		//Status
		query.add(new BooleanClause(buildLuceneValueInListQuery(qb,"status",new ArrayList<String>()),BooleanClause.Occur.MUST));
		
		//Projects
		query.add(new BooleanClause(buildLuceneValueInListQuery(qb,"project.id",new ArrayList<String>()),BooleanClause.Occur.MUST));
		
		
		//Test steps
				
		//Parameters
		query.add(new BooleanClause(buildLuceneRangeQuery(qb,"parameters",0,10),BooleanClause.Occur.MUST));
		
		//Datasets
		query.add(new BooleanClause(buildLuceneRangeQuery(qb,"datasets",0,10),BooleanClause.Occur.MUST));
		
		//Call Test steps
				
		//Attachments
		query.add(new BooleanClause(buildLuceneRangeQuery(qb,"attachmentList",0,10),BooleanClause.Occur.MUST));
		
		//Requirements
				
		//Iterations
				
		//Executions
				
		//Bugs
				
		//CreatedBy
				
		//CreatedOn
				
		//ModifiedBy
			
		//ModifiedOn
				
		//CUFs
				
			
		org.hibernate.Query hibQuery = ftSession.createFullTextQuery(query, TestCase.class);
		 
		List result = hibQuery.list();

		return result;
	}
	
	@Override
	public PagedCollectionHolder<List<TestCase>> searchForTestCases(PagingAndSorting sorting) {

		Session session = sessionFactory.getCurrentSession();
		 		
		FullTextSession ftSession = Search.getFullTextSession(session);

		QueryBuilder qb = ftSession.getSearchFactory().buildQueryBuilder().forEntity( TestCase.class ).get();
		 		
		org.apache.lucene.search.Query query = qb.keyword().onFields("prerequisite").matching("Batman").createQuery();
		 
		 org.hibernate.Query hibQuery = ftSession.createFullTextQuery(query, TestCase.class);
		 
		List result = hibQuery.list();
			 			
		List<TestCase> testCases = testCaseDao.findAll();
		long countAll = testCases.size();
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

	@Override
	public Boolean isIndexedOnPreviousVersion() {
		String currentVersion = configurationService.findConfiguration(SQUASH_VERSION_KEY);
		String testcaseIndexVersion = configurationService.findConfiguration(TESTCASE_INDEXING_VERSION_KEY);
		
		boolean result = currentVersion.equals(testcaseIndexVersion); 
				
		//TODO uncomment when requirements and campains are added to advanced search
		/*String requirementIndexVersion = configurationService.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY);
		String campaignIndexVersion = configurationService.findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY);
		
		boolean result = currentVersion.equals(requirementIndexVersion)
							&& currentVersion.equals(testcaseIndexVersion) 
							&& currentVersion.equals(campaignIndexVersion);*/
		
		return !result;
	}
}
