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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchIndexMonitoring;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.configuration.ConfigurationService;


public class AdvancedSearchIndexingMonitor implements MassIndexerProgressMonitor {

	private ConfigurationService configurationService;
	private Class indexedDomain;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");

	public AdvancedSearchIndexingMonitor(Class clazz, ConfigurationService configurationService){
		AdvancedSearchIndexMonitoring.reset();
		this.configurationService = configurationService;
		this.indexedDomain = clazz;
	}
	
	@Override
	public void documentsAdded(long arg0) {
		AdvancedSearchIndexMonitoring.setDocumentsAdded(arg0);	
	}

	@Override
	public void addToTotalCount(long arg0) {
		AdvancedSearchIndexMonitoring.setAddToTotalCount(arg0);	
	}

	@Override
	public void documentsBuilt(int arg0) {
		AdvancedSearchIndexMonitoring.setDocumentsBuilt(arg0);	
	}

	@Override
	public void entitiesLoaded(int arg0) {
		AdvancedSearchIndexMonitoring.setEntitiesLoaded(arg0);	
	}

	@Override
	public void indexingCompleted() {
		AdvancedSearchIndexMonitoring.setIndexingOver(true);
		
		if(this.indexedDomain.equals(TestCase.class)){
			this.updateTestCaseIndexingDateAndVersion();
		} else if (this.indexedDomain.equals(RequirementVersion.class)){
			this.updateRequirementVersionIndexingDateAndVersion();
		}			

	}

	private void updateRequirementVersionIndexingDateAndVersion(){
		Date indexingDate = new Date();
		this.configurationService.updateConfiguration(AdvancedSearchServiceImpl.REQUIREMENT_INDEXING_DATE_KEY, dateFormat.format(indexingDate));
		String currentVersion = this.configurationService.findConfiguration(AdvancedSearchServiceImpl.SQUASH_VERSION_KEY);
		this.configurationService.updateConfiguration(AdvancedSearchServiceImpl.REQUIREMENT_INDEXING_VERSION_KEY, currentVersion);
	}
	
	private void updateTestCaseIndexingDateAndVersion(){
		Date indexingDate = new Date();
		this.configurationService.updateConfiguration(AdvancedSearchServiceImpl.TESTCASE_INDEXING_DATE_KEY, dateFormat.format(indexingDate));
		String currentVersion = this.configurationService.findConfiguration(AdvancedSearchServiceImpl.SQUASH_VERSION_KEY);
		this.configurationService.updateConfiguration(AdvancedSearchServiceImpl.TESTCASE_INDEXING_VERSION_KEY, currentVersion);
	}
}
