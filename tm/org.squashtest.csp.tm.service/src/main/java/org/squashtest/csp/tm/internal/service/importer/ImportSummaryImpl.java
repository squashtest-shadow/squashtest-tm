/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer;

import org.squashtest.csp.tm.service.importer.ImportSummary;

public class ImportSummaryImpl implements ImportSummary {

	private int totalTestCases=0;
	private int success=0;
	private int warnings=0;
	private int failures=0;
	
	public ImportSummaryImpl(){
		
	}

	public void incrTotal(){
		totalTestCases++;
	}
	
	
	public void incrWarnings(){
		warnings++;
	}
	
	public void incrFailures(){
		failures++;
	}
	
	public void addToTotalTestCases(int delta){
		totalTestCases+=delta;
	}
	
	
	public void addToWarnings(int delta){
		warnings+=delta;
	}
	
	public void addToFailures(int delta){
		failures+=delta;
	}
	
	@Override
	public int getTotalTestCases() {
		return totalTestCases;
	}

	@Override
	public int getSuccess() {
		return totalTestCases - failures;
	}

	@Override
	public int getWarnings() {
		return warnings;
	}

	@Override
	public int getFailures() {
		return failures;
	}

	@Override
	public void add(ImportSummary summary){
		addToTotalTestCases(summary.getTotalTestCases());
		addToWarnings(summary.getWarnings());
		addToFailures(summary.getFailures());
	}
	

}
