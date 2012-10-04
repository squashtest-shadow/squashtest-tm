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
package org.squashtest.csp.tm.web.internal.controller.testcase;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.domain.testcase.TestStepVisitor;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;

/**
 * Builds a DataTable model for TestSteps table.
 *
 * @author Gregory Fouquet
 *
 */
class TestStepsTableModelBuilder extends DataTableModelHelper<TestStep> implements TestStepVisitor {
	private final MessageSource messageSource;
	private final Locale locale;
	private Object[] lastBuiltItem;

	public TestStepsTableModelBuilder(MessageSource messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	@Override
	public Object[] buildItemData(TestStep item) {
		item.accept(this);
		return lastBuiltItem;
	}

	/**
	 * Creates a model row from the visited item and stores it as {@link #lastBuiltItem}
	 */
	@Override
	public void visit(ActionTestStep visited) {
		lastBuiltItem = new Object[] { 
				"", 
				getCurrentIndex(), 
				visited.getId(), 
				visited.getAttachmentList().getId(),
				visited.getAction(), 
				visited.getExpectedResult(), 
				"",
				"",
				visited.getAttachmentList().size(),
				"action",
				null
		};

	}

	@Override
	public void visit(CallTestStep visited) {
		TestCase called = visited.getCalledTestCase();

		String action = messageSource.getMessage("test-case.call-step.action.template",
				new Object[] { called.getName() }, locale);

		lastBuiltItem = new Object[] { 
				"", 
				getCurrentIndex(), 
				visited.getId(), 
				"", 
				action, 
				"", 
				"", 
				"", 
				null,
				"call",
				called.getId()
		};

	}

		
	

}
