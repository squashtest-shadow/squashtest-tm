/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain.testautomation;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;


/**
 * Unlike the rest of the squashtm.testautomation.domain.TestAutomation X entities, 
 *  
 * 
 * @author bsiri
 *
 */

@NamedQueries({
	@NamedQuery(name="testAutomationExecution.findById", query="from TestAutomationExecution where id = :executionId")
})
@Auditable
@Entity
public class TestAutomationExecution extends Execution {

	private static final Set<ExecutionStatus> AUTOMATED_EXEC_STATUS;
	
	static {
		Set<ExecutionStatus> set = new HashSet<ExecutionStatus>();
		set.add(ExecutionStatus.SUCCESS);
		set.add(ExecutionStatus.WARNING);
		set.add(ExecutionStatus.ERROR);
		set.add(ExecutionStatus.FAILURE);
		set.add(ExecutionStatus.RUNNING);
		set.add(ExecutionStatus.READY);
		AUTOMATED_EXEC_STATUS = Collections.unmodifiableSet(set);
	}
	
	@ManyToOne()
	@JoinColumn(name = "TEST_ID", referencedColumnName="TEST_ID")
	private TestAutomationTest automatedTest;
	
	
	@Column
	private URL resultURL;
	
	@ManyToOne
	@JoinColumn(name="")
	private TestAutomationSuite automatedSuite;
	
	
	@Lob
	private String resultSummary;
	
	
	/* ******************** constructors ***********************************/
	
	public TestAutomationExecution() {
		super();
		executionMode = TestCaseExecutionMode.AUTOMATED;
	}


	public TestAutomationExecution(TestCase testCase) {
		super(testCase);
		executionMode = TestCaseExecutionMode.AUTOMATED;
		this.automatedTest = testCase.getTestAutomationTest();
	}
	
	public TestAutomationExecution(TestCase testCase, TestAutomationSuite suite){
		this(testCase);
		automatedSuite = suite;
	}
	
	/* ******************** accessors *************************************/
	

	public TestAutomationTest getAutomatedTest() {
		return automatedTest;
	}


	public void setAutomatedTest(TestAutomationTest automatedTest) {
		this.automatedTest = automatedTest;
	}


	public URL getResultURL() {
		return resultURL;
	}


	public void setResultURL(URL resultURL) {
		this.resultURL = resultURL;
	}


	public TestAutomationSuite getAutomatedSuite() {
		return automatedSuite;
	}


	public void setAutomatedSuite(TestAutomationSuite automatedSuite) {
		this.automatedSuite = automatedSuite;
	}


	public String getResultSummary() {
		return resultSummary;
	}


	public void setResultSummary(String resultSummary) {
		this.resultSummary = resultSummary;
	}	
	
	
	/* ********** override for HasExecutionStatus#getLegalStatusSet(); ****/
	
	@Override
	public Set<ExecutionStatus> getLegalStatusSet(){
		return AUTOMATED_EXEC_STATUS;
	}



	
}
