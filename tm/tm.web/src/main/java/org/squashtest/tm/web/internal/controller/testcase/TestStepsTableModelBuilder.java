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
package org.squashtest.tm.web.internal.controller.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;

/**
 * Builds a DataTable model for TestSteps table.
 *
 * @author Gregory Fouquet
 *
 */
class TestStepsTableModelBuilder extends DataTableModelHelper<TestStep> implements TestStepVisitor {
	
	private final MessageSource messageSource;
	private final Locale locale;
	private Map<?, ?> lastBuiltItem;
	

	public TestStepsTableModelBuilder(MessageSource messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}

	public List<Map<?,?>> buildAllData(List<TestStep> source){
		List<Map<?,?>> result = new ArrayList<Map<?,?>>(source.size());
		for (TestStep step : source){
			incrementIndex();
			Map<?,?> itemData = buildItemData(step);
			result.add(itemData);
		}
		return result;
	}
	
	@Override
	public Map<?, ?> buildItemData(TestStep item) {
		item.accept(this);
		return lastBuiltItem;
	}
	

	/**
	 * Creates a model row from the visited item and stores it as {@link #lastBuiltItem}
	 */
	@Override
	public void visit(ActionTestStep visited) {
		
		Map<Object, Object> item = new HashMap<Object, Object>(11);
		
		item.put("step-id", visited.getId());
		item.put("step-index", getCurrentIndex());
		item.put("attach-list-id", visited.getAttachmentList().getId());
		item.put("step-action", visited.getAction());
		item.put("step-result", visited.getExpectedResult());
		item.put("nb-attachments", visited.getAttachmentList().size());
		item.put("step-type", "action");
		item.put("called-tc-id", null);
		item.put("empty-requirements-holder", null);
		item.put("empty-browse-holder", null);
		item.put("empty-delete-holder", null);
		item.put("has-requirements", !visited.getRequirementVersionCoverages().isEmpty());
		item.put("nb-requirements", visited.getRequirementVersionCoverages().size());
		decorateWithCustomFields(item);
		
		lastBuiltItem = item;

	}

	@Override
	public void visit(CallTestStep visited) {
		TestCase called = visited.getCalledTestCase();

		String action = messageSource.getMessage("test-case.call-step.action.template",	new Object[] { called.getName() }, locale);
						   

		Map<Object, Object> item = new HashMap<Object, Object>(11);
		
		item.put("step-id", visited.getId());
		item.put("step-index", getCurrentIndex());
		item.put("attach-list-id", null);
		item.put("step-action", action);
		item.put("step-result", null);
		item.put("nb-attachments", null);
		item.put("step-type", "call");
		item.put("called-tc-id", called.getId());
		item.put("empty-requirements-holder", null);
		item.put("empty-browse-holder", null);
		item.put("empty-delete-holder", null);
		item.put("has-requirements", false);
		item.put("nb-requirements", null);
		decorateWithCustomFields(item);
		
		lastBuiltItem = item;

	}
	

	protected void decorateWithCustomFields(Map<Object, Object> item){

		Map<String, ShortCUFValueModel> cufValues = getCustomFieldsFor((Long)item.get("step-id"));
		item.put("customFields", cufValues);		
		
	}
	

}
