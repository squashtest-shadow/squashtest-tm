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
package org.squashtest.tm.service.internal.advancedsearch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.Search;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.library.IndexModel;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchIndexMonitoring;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.internal.library.AdvancedSearchIndexingMonitor;

@Service("squashtest.tm.service.IndexationService")
public class IndexationServiceImpl extends AdvancedSearchServiceImpl implements IndexationService {

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private ConfigurationService configurationService;

	public final static String SQUASH_VERSION_KEY = "squashtest.tm.database.version";

	public final static String REQUIREMENT_INDEXING_DATE_KEY = "lastindexing.requirement.date";
	public final static String TESTCASE_INDEXING_DATE_KEY = "lastindexing.testcase.date";
	public final static String CAMPAIGN_INDEXING_DATE_KEY = "lastindexing.campaign.date";

	public final static String REQUIREMENT_INDEXING_VERSION_KEY = "lastindexing.requirement.version";
	public final static String TESTCASE_INDEXING_VERSION_KEY = "lastindexing.testcase.version";
	public final static String CAMPAIGN_INDEXING_VERSION_KEY = "lastindexing.campaign.version";

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	@Override
	public IndexModel findIndexModel() {
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
	public void indexAll() {
		AdvancedSearchIndexMonitoring.reset();
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		@SuppressWarnings("rawtypes")
		List<Class> domains = new ArrayList<Class>();
		domains.add(TestCase.class);
		domains.add(RequirementVersion.class);
		domains.add(Execution.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);

		ftSession.createIndexer(TestCase.class, RequirementVersion.class, Execution.class).purgeAllOnStart(true)
				.threadsToLoadObjects(1)
				.threadsForSubsequentFetching(1).batchSizeToLoadObjects(10).cacheMode(CacheMode.IGNORE)
				.progressMonitor(monitor).start();
	}

	@Override
	public Boolean isIndexedOnPreviousVersion() {
		String currentVersion = configurationService.findConfiguration(SQUASH_VERSION_KEY);
		String testcaseIndexVersion = configurationService.findConfiguration(TESTCASE_INDEXING_VERSION_KEY);
		String requirementIndexVersion = configurationService.findConfiguration(REQUIREMENT_INDEXING_VERSION_KEY);
		String campaignIndexVersion = configurationService.findConfiguration(CAMPAIGN_INDEXING_VERSION_KEY);

		boolean result = currentVersion.equals(requirementIndexVersion) && currentVersion.equals(testcaseIndexVersion)
				&& currentVersion.equals(campaignIndexVersion);

		return !result;
	}

	/* ----------------------------REQUIREMENTS-------------------------------- */
	@Override
	public void reindexRequirementVersion(Long requirementVersionId) {
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);
		Object requirementVersion = ftSession.load(RequirementVersion.class, requirementVersionId);
		ftSession.index(requirementVersion);
	}

	@Override
	public void reindexRequirementVersions(List<RequirementVersion> requirementVersionList) {
		for (RequirementVersion requirementVersion : requirementVersionList) {
			reindexRequirementVersion(requirementVersion.getId());
		}
	}

	@Override
	public void reindexRequirementVersionsByIds(List<Long> requirementVersionsIds) {
		for (Long id : requirementVersionsIds) {
			reindexRequirementVersion(id);
		}

	}

	@Override
	public void indexRequirementVersions() {

		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		@SuppressWarnings("rawtypes")
		List<Class> domains = new ArrayList<Class>();
		domains.add(RequirementVersion.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);

		configure(ftSession.createIndexer(RequirementVersion.class)).progressMonitor(monitor).start();
	}

	/* ----------------------------TEST CASES-------------------------------- */
	@Override
	public void reindexTestCase(Long testCaseId) {
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);
		Object testCase = ftSession.load(TestCase.class, testCaseId);
		ftSession.index(testCase);
	}

	@Override
	public void reindexTestCases(List<TestCase> testCaseList) {
		for (TestCase testcase : testCaseList) {
			reindexTestCase(testcase.getId());
		}
	}

	@Override
	public void indexTestCases() {

		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		@SuppressWarnings("rawtypes")
		List<Class> domains = new ArrayList<Class>();
		domains.add(TestCase.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);

		configure(ftSession.createIndexer(TestCase.class)).progressMonitor(monitor).start();
	}

	private MassIndexer configure(MassIndexer indexer) {
		// formatter:off
		indexer.purgeAllOnStart(true)
			.threadsToLoadObjects(1)
			.threadsForSubsequentFetching(1)
			.batchSizeToLoadObjects(10)
			.cacheMode(CacheMode.NORMAL);
		// formatter:on
		return indexer;
	};

	@Override
	public void batchReindexTc(List<Long> tcIdsToIndex) {
		batchReindex(TestCase.class, tcIdsToIndex);	
	}
	
	private <T> void batchReindex(Class<T> entity, List<Long> ids){
		Session session = sessionFactory.getCurrentSession();
		
		session.flush();
		session.clear();
		
		// get FullText session
		FullTextSession ftSession = Search.getFullTextSession(session);
		ftSession.setFlushMode(FlushMode.MANUAL);
		ftSession.setCacheMode(CacheMode.IGNORE);
		//define criteria to load entities
		Criteria query = session.createCriteria(entity);
		query.add(Restrictions.in("id", ids));
		// update index going through the search results
	
		int batch = 0;
		ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY); 
		while (scroll.next()) {
		   ftSession.index(scroll.get(0)); //indexing of a single entity
		       if (batch % 20 == 0) { // commit batch                
		           ftSession.flushToIndexes();                
		           ftSession.clear();   
		        
		      }
		}

	}
	/* ----------------------------EXECUTIONS -------------------------------- */
	@Override
	public void indexCampaigns() {
		// It's called Campaigns but we need first to index Executions only // Maybe more later, will see
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);

		@SuppressWarnings("rawtypes")
		List<Class> domains = new ArrayList<Class>();
		domains.add(Execution.class);
		MassIndexerProgressMonitor monitor = new AdvancedSearchIndexingMonitor(domains, this.configurationService);

		ftSession.createIndexer(Execution.class).purgeAllOnStart(true).threadsToLoadObjects(1)
				.threadsForSubsequentFetching(1).batchSizeToLoadObjects(10).cacheMode(CacheMode.NORMAL)
				.progressMonitor(monitor).start();
	}

	@Override
	public void reindexCampaign(Long campaignId) {
		Session session = sessionFactory.getCurrentSession();
		FullTextSession ftSession = Search.getFullTextSession(session);
		Object execution = ftSession.load(Execution.class, campaignId);
		ftSession.index(execution);
	}

	@Override
	public void reindexCampaigns(List<Execution> campaignList) {
		for (Execution execution : campaignList) {
			reindexTestCase(execution.getId());
		}
	}

	@Override
	public void batchReindexReqVersion(List<Long> reqVersionIdsToIndex) {
		batchReindex(RequirementVersion.class, reqVersionIdsToIndex);
		
	}

}
