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
package org.squashtest.csp.tm.domain.testcase;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.squashtest.csp.core.security.annotation.InheritsAcls;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@InheritsAcls(constrainedClass = TestCase.class, collectionName = "steps")
public abstract class TestStep {
	@Id
	@GeneratedValue
	@Column(name = "TEST_STEP_ID")
	private Long id;

	public Long getId() {
		return id;
	}

	/**
	 * Should create a transient copy of this {@link TestStep} according to business rules. Business rules should be
	 * described by implementor in a unit test case.
	 *
	 * @return copy, should never return <code>null</code>.
	 */
	public abstract TestStep createCopy();

	public abstract void accept(TestStepVisitor visitor);
	
	public abstract List<ExecutionStep> createExecutionSteps();

}
