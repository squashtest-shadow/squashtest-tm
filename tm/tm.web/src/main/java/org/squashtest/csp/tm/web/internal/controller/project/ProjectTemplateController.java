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
package org.squashtest.csp.tm.web.internal.controller.project;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.csp.tm.domain.project.ProjectTemplate;
import org.squashtest.csp.tm.service.project.ProjectTemplateFinder;

@Controller
@RequestMapping("/project-templates")
public class ProjectTemplateController {

	@Inject
	private ProjectTemplateFinder projectFinder;
	

	@RequestMapping(value="/combo", method = RequestMethod.GET)
	@ResponseBody
	public Combo getProjectPickerModel() {
		StringBuilder toReturn = new StringBuilder("<select>");
		for (ProjectTemplate template : projectFinder.findAll()) {
			toReturn.append("<option value = \"");
			toReturn.append(template.getId());
			toReturn.append("\">" +  template.getLabel() + "</option>");
		}
		toReturn.append("</select>");
		
		return new Combo(toReturn.toString());
	}
	private static class Combo {
		private String templates ;

		public Combo(String templates) {
			super();
			this.templates = templates;
		}

		public String getTemplates() {
			return templates;
		}

		public void setTemplates(String templates) {
			this.templates = templates;
		}
		
	}
}
