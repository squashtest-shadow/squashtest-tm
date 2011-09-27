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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.service.WorkspaceService;
import org.squashtest.csp.tm.web.internal.controller.generic.WorkspaceController;

@Controller
@RequestMapping("/requirement-workspace")
public class RequirementWorkspaceController extends WorkspaceController<RequirementLibrary> {
	private WorkspaceService<RequirementLibrary> workspaceService;

	@Inject
	private MessageSource messageSource;

	@Override
	protected WorkspaceService<RequirementLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "page/requirement-workspace";
	}

	@ServiceReference(serviceBeanName="squashtest.tm.service.RequirementsWorkspaceService")
	public final void setWorkspaceService(WorkspaceService<RequirementLibrary> requirementsWorkspaceService) {
		this.workspaceService = requirementsWorkspaceService;
	}

	/***
	 * This method returns the criticality options tag to the jsp
	 *
	 * @param locale
	 *            the locale
	 * @return the html code for the criticality(String)
	 */
	@RequestMapping(value = "/criticality-options", method = RequestMethod.GET)
	@ResponseBody
	String getCriticitySelectionList(Locale locale) {
		return buildCriticitySelectionList(locale);
	}

	/***
	 * Method which returns the criticality select options in the chosen language
	 *
	 * @param locale
	 *            the Locale
	 * @return the html select object
	 */
	private String buildCriticitySelectionList(Locale locale) {
		StringBuilder toReturn = new StringBuilder("<select id=\"add-requirement-criticality\" cssClass=\"combobox\">");
		for (RequirementCriticality criticality : RequirementCriticality.values()) {
			toReturn.append("<option value = \"");
			toReturn.append(criticality.toString());
			toReturn.append("\">" + formatCriticality(criticality, locale) + "</option>");
		}
		toReturn.append("</select>");
		return toReturn.toString();
	}

	/***
	 * Method which returns criticality in the chosen language
	 *
	 * @param criticity
	 *            the criticality
	 * @param locale
	 *            the locale with the chosen language
	 * @return the criticality in the chosen language
	 */
	private String formatCriticality(RequirementCriticality criticity, Locale locale) {
		String toReturn;

		switch (criticity) {
		case MAJOR:
			toReturn = messageSource.getMessage("requirement.criticality.MAJOR", null, locale);
			break;

		case CRITICAL:
			toReturn = messageSource.getMessage("requirement.criticality.CRITICAL", null, locale);
			break;

		case MINOR:
			toReturn = messageSource.getMessage("requirement.criticality.MINOR", null, locale);
			break;

		case UNDEFINED:
			toReturn = messageSource.getMessage("requirement.criticality.UNDEFINED", null, locale);
			break;

		default:
			toReturn = messageSource.getMessage("requirement.criticality.UNKNOWN", null, locale);
			break;
		}
		return toReturn;
	}

}
