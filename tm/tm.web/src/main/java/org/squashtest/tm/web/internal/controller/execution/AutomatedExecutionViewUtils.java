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
package org.squashtest.tm.web.internal.controller.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.lang.MathsUtils;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;

public final class AutomatedExecutionViewUtils {
	private AutomatedExecutionViewUtils() {

	}

	public static AutomatedSuiteOverview buildExecInfo(AutomatedSuite suite, Locale locale, MessageSource messageSource) {
		Collection<AutomatedExecutionExtender> executions = suite.getExecutionExtenders();
		List<ExecutionAutoView> executionsViews = new ArrayList<ExecutionAutoView>(executions.size());
		int totalExec = executions.size();
		int totalTerminated = 0;
		for (AutomatedExecutionExtender aee : executions) {
			Execution execution = aee.getExecution();
			if(execution.getExecutionStatus().isTerminatedStatus()){
				totalTerminated ++;
			}
			ExecutionAutoView execView = translateExecutionInView(execution, locale, messageSource);
			executionsViews.add(execView);
		}
		int percentage = percentProgression(totalTerminated, totalExec);
		return new AutomatedSuiteOverview(percentage, suite.getId(), executionsViews);

	}

	private static int percentProgression(int totalTerminated, int totalExec) {
		int percentage = MathsUtils.percent(totalTerminated, totalExec);
		return percentage;
	}

	public static ExecutionAutoView translateExecutionInView(Execution execution, Locale locale,
			MessageSource messageSource) {
		String localisedStatus = messageSource.getMessage(execution.getExecutionStatus().getI18nKey(), null, locale);
		String htmlEscapedLocalizedStatus = HtmlUtils.htmlEscape(localisedStatus);
		ExecutionAutoView execView = new ExecutionAutoView(execution.getId(), execution.getName(),
				execution.getExecutionStatus(), htmlEscapedLocalizedStatus);
		return execView;
	}

	public static class AutomatedSuiteOverview {
		private String suiteId;
		private List<ExecutionAutoView> executions;
		private int percentage = 0;

		public AutomatedSuiteOverview(int percentage, String suiteId, List<ExecutionAutoView> executions) {
			this.suiteId = suiteId;
			this.executions = executions;
			this.percentage = percentage;

		}

		public String getSuiteId() {
			return suiteId;
		}

		public void setSuiteId(String suiteId) {
			this.suiteId = suiteId;
		}

		public List<ExecutionAutoView> getExecutions() {
			return executions;
		}

		public void setExecutions(List<ExecutionAutoView> executions) {
			this.executions = executions;
		}

		public int getPercentage() {
			return percentage;
		}

		public void setPercentage(int percentage) {
			this.percentage = percentage;
		}



	}

	public static class ExecutionAutoView {
		private Long id;
		private String name;
		private ExecutionStatus status;
		private String localizedStatus;

		public ExecutionAutoView(Long id, String name, ExecutionStatus status, String localizedStatus) {
			this.id = id;
			this.name = name;
			this.status = status;
			this.localizedStatus = localizedStatus;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ExecutionStatus getStatus() {
			return status;
		}

		public void setStatus(ExecutionStatus status) {
			this.status = status;
		}

		public String getLocalizedStatus() {
			return localizedStatus;
		}

		public void setLocalizedStatus(String localizedStatus) {
			this.localizedStatus = localizedStatus;
		}

	}

}
