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

package org.squashtest.csp.tm.web.internal.controller.testcase;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportanceLevelComparator;
import org.squashtest.csp.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Jeditable combo data builder which model is {@link TestCaseImportance}
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
public class TestCaseImportanceJeditableComboDataBuilder extends EnumJeditableComboDataBuilder<TestCaseImportance> {
	public TestCaseImportanceJeditableComboDataBuilder() {
		super();
		setModel(TestCaseImportance.values());
	}

	@ServiceReference
	public void setModelComparator(TestCaseImportanceLevelComparator comparator) {
		super.setModelComparator(comparator);
	}

	@Inject
	public void setLabelFormatter(TestCaseImportanceLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

}
