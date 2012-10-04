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
package org.squashtest.csp.tm.internal.service.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestStep;

/**
 * Factored out from {@link ExcelTestCaseParserImpl}
 * @author Benoit Siri
 * @author Gregory
 *
 */
/* package-private */class PseudoTestCase {
	public Date createdOnDate = null;
	public String createdBy = null;
	public String createdOn = null;

	public String importance = "";

	// the first element of the list is the description itself
	// others are complementary elements
	public final List<String[]> descriptionElements = new ArrayList<String[]>();

	public final List<String> prerequisites = new ArrayList<String>();

	public final List<String[]> stepElements = new LinkedList<String[]>();

	/* ***************************** formatters *********************************** */

	public String formatDescription() {

		StringBuilder builder = new StringBuilder();

		List<String[]> elements = descriptionElements;

		if (elements.size() > 0) {
			// appending the description
			builder.append("<p>").append(elements.get(0)[1]).append("</p>");
		}

		// appending supplementary material if any;

		if (elements.size() > 1) {

			builder.append("<hr/>");
			builder.append("<ul>");

			for (int i = 1; i < elements.size(); i++) {
				String[] elt = elements.get(i);
				builder.append("<li>").append("<strong>" + elt[0] + " :</strong> ").append(elt[1]).append("</li>");
			}

			builder.append("</ul>");

		}

		return builder.toString();

	}

	public TestCaseImportance formatImportance() {
		return TestCaseImportance.valueOf(importance);
	}

	public String formatPreRequisites() {
		StringBuilder builder = new StringBuilder();

		if (prerequisites.size() > 0) {

			builder.append("<ol>");

			for (String string : prerequisites) {
				builder.append("<li>").append(string).append("</li>");
			}

			builder.append("</ol>");
		}

		return builder.toString();
	}

	public List<TestStep> formatSteps() {

		List<TestStep> steps = new LinkedList<TestStep>();

		for (String[] pseudoStep : stepElements) {
			ActionTestStep step = new ActionTestStep();
			step.setAction("<p>" + pseudoStep[0] + "</p>");
			step.setExpectedResult("<p>" + pseudoStep[1] + "</p>");
			steps.add(step);
		}

		return steps;

	}
}
