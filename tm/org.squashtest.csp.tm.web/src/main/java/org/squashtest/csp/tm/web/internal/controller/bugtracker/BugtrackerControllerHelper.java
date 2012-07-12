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
package org.squashtest.csp.tm.web.internal.controller.bugtracker;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.squashtest.csp.core.web.utils.HTMLCleanupUtils;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;

public class BugtrackerControllerHelper {
	private BugtrackerControllerHelper(){
		
	}
	/**
	 * Will build a string that shows all steps before the bugged step + the bugged step itself.<br>
	 * The string will look like this : <br/>
	 * <br>
	 * <em>
	 * 	[Step x/N :]<br>
	 * 	=============<br>
	 * 	action description<br>
	 * <br>
	 * 	==> expected result description<br>
	 * <br>
	 * <br>
	 * 	[Step x+1/N :]<br>
	 * 	=============<br>
	 * 	...<br></em>
	 * 
	 * @param buggedStep
	 *            the bugged step where the issue will be declared
	 * @param locale
	 * @param messageSource
	 * @return the built string as described
	 */
	public static String getDefaultAdditionalInformations(ExecutionStep buggedStep, Locale locale,
			MessageSource messageSource) {
		Execution execution = buggedStep.getExecution();
		List<ExecutionStep> steps = execution.getSteps();
		int totalStepNumber = steps.size();
		StringBuilder builder = new StringBuilder();
		for (ExecutionStep step : steps) {
			String actionText = HTMLCleanupUtils.htmlToText(step.getAction());
			String expectedResult = HTMLCleanupUtils.htmlToText(step.getExpectedResult());
			builder.append("[");
			builder.append(messageSource.getMessage("issue.default.additionalInformation.step", null, locale));
			builder.append(" ");
			builder.append(step.getExecutionStepOrder()+1);
			builder.append("/");
			builder.append(totalStepNumber);
			builder.append(" :]\n=============\n");
			builder.append(actionText);
			builder.append("\n\n==>");
			builder.append(expectedResult);
			builder.append("\n\n\n\n");
			if (step.getId().equals(buggedStep.getId())) {
				break;
			}
		}
		return builder.toString();
	}

	/**
	 * Will build a default description String that will look like this : <br/>
	 * <br/>
	 * <em># Test Case : [Reference] test case name <br/>
	 * # Execution : execution link <br/>
	 * <br/>
	 * # Issue description :<br/></em>
	 * 
	 * @param execution
	 *            an execution where the issue will be declared
	 * @param locale
	 * @param messageSource
	 * @return the description string
	 */
	public static String getDefaultDescription(Execution execution, Locale locale, MessageSource messageSource, String executionUrl) {
		StringBuffer description = new StringBuffer();
		appendTestCaseDesc(execution.getReferencedTestCase(), description, locale, messageSource);
		appendExecutionDesc(description, locale, messageSource, executionUrl);
		appendDescHeader(description, locale, messageSource);
		return description.toString();
	}

	/**
	 * Will build a default description String that will look like this : <br/>
	 * <br/>
	 * <em># Test Case : [Reference] test case name <br/>
	 * # Execution : execution link <br/>
	 * # Concerned Step : step n°/total step nb<br/>
	 * <br/>
	 * # Issue description :<br/></em>
	 * 
	 * @param step
	 *            an execution step where the issue will be declared
	 * @param locale
	 * @param messageSource
	 * @param executionUrl 
	 * @return the string built as described
	 */
	public static String getDefaultDescription(ExecutionStep step, Locale locale, MessageSource messageSource, String executionUrl) {
		StringBuffer description = new StringBuffer();
		appendTestCaseDesc(step.getExecution().getReferencedTestCase(), description, locale, messageSource);
		appendExecutionDesc(description, locale, messageSource, executionUrl);
		appendStepDesc(step, description, locale, messageSource);
		appendDescHeader(description, locale, messageSource);
		return description.toString();
	}
	
	/**
	 * build the url of the execution 
	 * @param request
	 * @param step
	 * @return <b>"http://</b>serverName<b>:</b>serverPort/contextPath<b>/executions/</b>executionId<b>/info"</b>
	 */
	public static String buildExecutionUrl(HttpServletRequest request, Execution execution) {
		StringBuffer requestUrl = new StringBuffer("http://");
		requestUrl.append(request.getServerName());
		requestUrl.append(':');
		requestUrl.append(request.getServerPort());
		requestUrl.append(request.getContextPath());
		requestUrl.append("/executions/");
		requestUrl.append(execution.getId());
		requestUrl.append("/info");
		String executionUrl = requestUrl.toString();
		return executionUrl;
	}
	private static void appendDescHeader(StringBuffer description, Locale locale, MessageSource messageSource) {
		description.append("\n# ");
		description.append(messageSource.getMessage("issue.default.description.description", null, locale));
		description.append(" :\n");
	}

	private static void appendStepDesc(ExecutionStep step, StringBuffer description, Locale locale,
			MessageSource messageSource) {
		description.append("# ");
		description.append(messageSource.getMessage("issue.default.description.concernedStep", null, locale));
		description.append(" : ");
		description.append(step.getExecutionStepOrder()+1);
		description.append("/");
		description.append(step.getExecution().getSteps().size());
		description.append("\n");
	}

	private static void appendExecutionDesc(StringBuffer description, Locale locale, MessageSource messageSource, String executionUrl) {
		description.append("# ");
		description.append(messageSource.getMessage("issue.default.description.execution", null, locale));
		description.append(": ");
		description.append(executionUrl);
		description.append("\n");
	}

	private static void appendTestCaseDesc(TestCase testCase, StringBuffer description, Locale locale,
			MessageSource messageSource) {
		if (testCase != null) {
			description.append("# ");
			description.append(messageSource.getMessage("issue.default.description.testCase", null, locale));
			description.append(": [");
			description.append(testCase.getReference());
			description.append("] ");
			description.append(testCase.getName());
			description.append("\n");
		}
	}
}
