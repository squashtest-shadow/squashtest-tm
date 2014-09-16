/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.library;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchIndexMonitoring;
import org.squashtest.tm.domain.search.AdvancedSearchIndexMonitoringForRequirementVersions;
import org.squashtest.tm.domain.search.AdvancedSearchIndexMonitoringForTestcases;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.internal.advancedsearch.IndexationServiceImpl;


public class AdvancedSearchIndexingMonitor implements MassIndexerProgressMonitor {

	private ConfigurationService configurationService;
	private List<Class> indexedDomains;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public AdvancedSearchIndexingMonitor(List<Class> classes, ConfigurationService configurationService){
		this.configurationService = configurationService;
		this.indexedDomains = classes;
		
		if(this.indexedDomains.contains(TestCase.class)){
			AdvancedSearchIndexMonitoringForTestcases.reset();
		}
		
		if(this.indexedDomains.contains(RequirementVersion.class)){
			AdvancedSearchIndexMonitoringForRequirementVersions.reset();
		}
	}
	
	@Override
	public void documentsAdded(long arg0) {
		
		AdvancedSearchIndexMonitoring.setDocumentsAdded(arg0);	
		
		if(this.indexedDomains.contains(TestCase.class)){
			AdvancedSearchIndexMonitoringForTestcases.setDocumentsAdded(arg0);
		}
		
		if(this.indexedDomains.contains(RequirementVersion.class)){
			AdvancedSearchIndexMonitoringForRequirementVersions.setDocumentsAdded(arg0);
		}
	}

	@Override
	public void addToTotalCount(long arg0) {
		
		AdvancedSearchIndexMonitoring.setAddToTotalCount(arg0);	
		
		if(this.indexedDomains.contains(TestCase.class)){
			AdvancedSearchIndexMonitoringForTestcases.setAddToTotalCount(arg0);	
		}
		
		if(this.indexedDomains.contains(RequirementVersion.class)){
			AdvancedSearchIndexMonitoringForRequirementVersions.setAddToTotalCount(arg0);	
		}
	}

	@Override
	public void documentsBuilt(int arg0) {
		
		AdvancedSearchIndexMonitoring.setDocumentsBuilt(arg0);	
		
		if(this.indexedDomains.contains(TestCase.class)){
			AdvancedSearchIndexMonitoringForTestcases.setDocumentsBuilt(arg0);		
		}
		
		if(this.indexedDomains.contains(RequirementVersion.class)){
			AdvancedSearchIndexMonitoringForRequirementVersions.setDocumentsBuilt(arg0);		
		}
	}

	@Override
	public void entitiesLoaded(int arg0) {
		
		AdvancedSearchIndexMonitoring.setEntitiesLoaded(arg0);	
		
		
		if(this.indexedDomains.contains(TestCase.class)){
			AdvancedSearchIndexMonitoringForTestcases.setEntitiesLoaded(arg0);	
		}
		
		if(this.indexedDomains.contains(RequirementVersion.class)){
			AdvancedSearchIndexMonitoringForRequirementVersions.setEntitiesLoaded(arg0);		
		}
	}

	@Override
	public void indexingCompleted() {
		
		AdvancedSearchIndexMonitoring.setIndexingOver(true);
		
		if(this.indexedDomains.contains(TestCase.class)){
			AdvancedSearchIndexMonitoringForTestcases.setIndexingOver(true);
			this.updateTestCaseIndexingDateAndVersion();
		}
		
		if (this.indexedDomains.contains(RequirementVersion.class)){
			AdvancedSearchIndexMonitoringForRequirementVersions.setIndexingOver(true);
			this.updateRequirementVersionIndexingDateAndVersion();
		} 	
	}

	private void updateRequirementVersionIndexingDateAndVersion(){
		Date indexingDate = new Date();
		this.configurationService.updateConfiguration(IndexationServiceImpl.REQUIREMENT_INDEXING_DATE_KEY, dateFormat.format(indexingDate));
		String currentVersion = this.configurationService.findConfiguration(IndexationServiceImpl.SQUASH_VERSION_KEY);
		this.configurationService.updateConfiguration(IndexationServiceImpl.REQUIREMENT_INDEXING_VERSION_KEY, currentVersion);
	}
	
	private void updateTestCaseIndexingDateAndVersion(){
		Date indexingDate = new Date();
		this.configurationService.updateConfiguration(IndexationServiceImpl.TESTCASE_INDEXING_DATE_KEY, dateFormat.format(indexingDate));
		String currentVersion = this.configurationService.findConfiguration(IndexationServiceImpl.SQUASH_VERSION_KEY);
		this.configurationService.updateConfiguration(IndexationServiceImpl.TESTCASE_INDEXING_VERSION_KEY, currentVersion);
	}
}
