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
package org.squashtest.tm.service.internal.importer;

import java.util.HashSet;
import java.util.Set;

import org.squashtest.tm.service.importer.ImportRequirementTestCaseLinksSummary;

public class ImportRequirementTestCaseLinksSummaryImpl implements ImportRequirementTestCaseLinksSummary {

	private int total=0;
	private int failures=0;
	private Set<Integer> testCaseAccessRejected = new HashSet<Integer>();
	private Set<Integer> requirementAccessRejected = new HashSet<Integer>();
	private Set<Integer> requirementNotFound = new HashSet<Integer>();
	private Set<Integer> testCaseNotFound = new HashSet<Integer>();
	private Set<Integer> versionNotFound = new HashSet<Integer>();
	private Set<Integer> obsoletes = new HashSet<Integer>();
	private Set<Integer> linkAlreadyExist = new HashSet<Integer>();
	private Set<String>  missingColumnHeaders = new HashSet<String>();
	
	
	
	public ImportRequirementTestCaseLinksSummaryImpl(){
		
	}
	
	public void incrTotal(){
		total++;
	}
	
	public void incrFailures(){
		failures++;
	}
	
	public void addObsolete(int number){
		this.obsoletes.add(number);
	}
	
	public void addRequirementAccessRejected(int number){
		this.requirementAccessRejected.add(number);
	}
	
	public void addRequirementNotFound(int number){
		this.requirementNotFound.add(number);
	}
	
	public void addTestCaseAccessRejected(int number){
		this.testCaseAccessRejected.add(number);
	}
	
	public void addTestCaseNotFound(int number){
		this.testCaseNotFound.add(number);
	}
	
	public void addVersionNotFound(int number){
		this.versionNotFound.add(number);
	}
	
	public void addLinkAlreadyExist(int number){
		this.linkAlreadyExist.add(number);
	}
	
	public void addMissingColumnHeader(String headerName){
		this.missingColumnHeaders.add(headerName);
	}
	
	@Override
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public int getFailures() {
		return failures;
	}

	public void setFailures(int failures) {
		this.failures = failures;
	}

	@Override
	public int getSuccess() {
		return this.total - this.failures;
	}

	@Override
	public Set<Integer> getRequirementNotFound() {
		return this.requirementNotFound;
	}

	@Override
	public Set<Integer> getTestCaseNotFound() {
		return this.testCaseNotFound;
	}

	@Override
	public Set<Integer> getVersionNotFound() {
		return this.versionNotFound;
	}

	@Override
	public Set<Integer> getObsoletes() {
		return this.obsoletes;
	}

	@Override
	public Set<Integer> getRequirementAccessRejected() {
		return this.requirementAccessRejected;
	}

	@Override
	public Set<Integer> getTestCaseAccessRejected() {
			return this.testCaseAccessRejected;
	}

	@Override
	public void add(ImportRequirementTestCaseLinksSummary summary) {
		this.total+=summary.getTotal();
		this.failures+=summary.getFailures();
		this.obsoletes.addAll(summary.getObsoletes());
		this.requirementAccessRejected.addAll(summary.getRequirementAccessRejected());
		this.requirementNotFound.addAll(summary.getRequirementNotFound());
		this.testCaseAccessRejected.addAll(summary.getTestCaseAccessRejected());
		this.testCaseNotFound.addAll(summary.getTestCaseNotFound());
		this.versionNotFound.addAll(summary.getVersionNotFound());
		this.linkAlreadyExist.addAll(summary.getLinkAlreadyExist());
		this.missingColumnHeaders.addAll(summary.getMissingColumnHeaders());
	}

	@Override
	public Set<Integer> getLinkAlreadyExist() {
		return this.linkAlreadyExist;
	}

	@Override
	public Set<String> getMissingColumnHeaders() {
		return this.missingColumnHeaders;
	}
	
	@Override
	//jsp + javabean conventions wouldn't accept 'hasCriticalErrors'
	public boolean isCriticalErrors(){
		return missingColumnHeaders.size()>0;
	}
	

}
