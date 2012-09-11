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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;

/**
 * this was meant to be a subclass of Execution; that's what the business says. But Hibernate says that doing so would
 * trigger a bug. So we came with an extender instead.
 * 
 * 
 * @author bsiri
 * 
 */
@Entity
public class AutomatedExecutionExtender {

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

	@Id
	@Column(name = "EXTENDER_ID")
	@GeneratedValue
	private Long id;

	@ManyToOne()
	@JoinColumn(name = "TEST_ID", referencedColumnName = "TEST_ID")
	private AutomatedTest automatedTest;

	@OneToOne
	@JoinColumn(name = "MASTER_EXECUTION_ID", referencedColumnName = "EXECUTION_ID")
	private Execution execution;

	private URL resultURL;

	@ManyToOne
	@JoinColumn(name = "SUITE_ID")
	private AutomatedSuite automatedSuite;

	@Lob
	private String resultSummary = "";

	/* ******************** constructors ********************************** */

	public AutomatedExecutionExtender() {
		super();
	}

	/* ******************** accessors ************************************ */

	public Long getId() {
		return id;
	}

	public Execution getExecution() {
		return execution;
	}

	public void setExecution(Execution execution) {
		this.execution = execution;
	}

	public AutomatedTest getAutomatedTest() {
		return automatedTest;
	}

	public void setAutomatedTest(AutomatedTest automatedTest) {
		this.automatedTest = automatedTest;
	}

	public URL getResultURL() {
		return resultURL;
	}

	public void setResultURL(URL resultURL) {
		this.resultURL = resultURL;
	}

	public AutomatedSuite getAutomatedSuite() {
		return automatedSuite;
	}

	public void setAutomatedSuite(AutomatedSuite automatedSuite) {
		this.automatedSuite = automatedSuite;
	}

	public String getResultSummary() {
		return resultSummary;
	}

	public void setResultSummary(String resultSummary) {
		this.resultSummary = resultSummary;
	}

	public Set<ExecutionStatus> getLegalStatusSet() {
		return AUTOMATED_EXEC_STATUS;
	}

	public void setExecutionStatus(ExecutionStatus status) {
		execution.setExecutionStatus(status);
	}

}
