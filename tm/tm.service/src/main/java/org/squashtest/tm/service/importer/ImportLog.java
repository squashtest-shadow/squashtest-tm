/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.importer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;

import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.service.internal.batchimport.EntityType;
import org.squashtest.tm.service.internal.batchimport.LogEntry;

public class ImportLog {

	//key : EntityType, values : LogEntry
	private MultiValueMap logEntriesPerType = MultiValueMap.decorate(new HashMap(), SortedSet.class);
	
	// the pointer to the excel workbook that contains the error log
	private File fullReport = null;
	
	private int testCaseSuccess=0;
	private int testCaseWarnings=0;
	private int testCaseFailures=0;
	
	private int testStepSuccess=0;
	private int testStepWarnings=0;
	private int testStepFailures=0;
	
	private int parametersSuccess=0;
	private int parametersWarnings=0;
	private int parametersFailures=0;
	
	private int datasetSuccess=0;
	private int datasetWarnings=0;
	private int datasetFailures=0;
	

	public void addLogEntry(LogEntry logEntry){
		logEntriesPerType.put(logEntry.getTarget().getType(), logEntry);
	}


	public Collection<LogEntry> findAllFor(EntityType type){
		return logEntriesPerType.getCollection(type);
	}

	
	public void recompute(){
		
		for ( LogEntry entry : findAllFor(EntityType.TEST_CASE) ){
			switch(entry.getStatus()){
			case OK : testCaseSuccess++; break;
			case WARNING : testCaseWarnings++; break;
			case FAILURE : testCaseFailures++; break;
			default : break;
			}
		}
		
		for ( LogEntry entry : findAllFor(EntityType.TEST_STEP)){
			switch(entry.getStatus()){
			case OK : testStepSuccess++; break;
			case WARNING : testStepWarnings++; break;
			case FAILURE : testStepFailures++; break;
			default : break;
			}
		}
		
		for ( LogEntry entry : findAllFor(EntityType.PARAMETER)){
			switch(entry.getStatus()){
			case OK : parametersSuccess++; break;
			case WARNING : parametersWarnings++; break;
			case FAILURE : parametersFailures++; break;
			default : break;
			}
		}
		
		for ( LogEntry entry : findAllFor(EntityType.DATASET)){
			switch(entry.getStatus()){
			case OK : datasetSuccess++; break;
			case WARNING : datasetWarnings++; break;
			case FAILURE : datasetFailures++; break;
			default : break;
			}
		}

		
	}
	
	// that getter doesn't comply with the Java bean convention purposely. 
	public File obtainFullReport() {
		return fullReport;
	}

	public void setFullReport(File fullReport) {
		this.fullReport = fullReport;
	}

	public int getTestCaseSuccess() {
		return testCaseSuccess;
	}


	public int getTestCaseWarnings() {
		return testCaseWarnings;
	}


	public int getTestCaseFailures() {
		return testCaseFailures;
	}


	public int getTestStepSuccess() {
		return testStepSuccess;
	}

	public int getTestStepWarnings() {
		return testStepWarnings;
	}

	public int getTestStepFailures() {
		return testStepFailures;
	}

	public int getParametersSuccess() {
		return parametersSuccess;
	}

	public int getParametersWarnings() {
		return parametersWarnings;
	}

	public int getParametersFailures() {
		return parametersFailures;
	}

	public int getDatasetSuccess() {
		return datasetSuccess;
	}

	public int getDatasetWarnings() {
		return datasetWarnings;
	}

	public int getDatasetFailures() {
		return datasetFailures;
	}
	
	
}
