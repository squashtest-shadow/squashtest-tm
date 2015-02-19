/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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


package org.squashtest.tm.web.internal.controller.requirement;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.requirement.RequirementVersionResolverService;
import org.squashtest.tm.web.internal.controller.RequestParams;


/**
 * This class will resolve which version of a requirement the user wants to browse according to whether he uses milestones mode or not.
 */

@Controller
@RequestMapping("/requirements/{requirementId}")
public class RequirementVersionResolverController {


	@Inject
	private RequirementVersionResolverService versionResolver;


	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String resolveRequirementInfo(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, @CookieValue(required=false, value="milestones") List<Long> milestoneIds) {
		Long milestoneId = null;
		if (milestoneIds != null && (! milestoneIds.isEmpty())){
			milestoneId = milestoneIds.get(0);
		}
		RequirementVersion version = versionResolver.resolveByRequirementId(requirementId, milestoneId);
		return "redirect:/requirement-versions/"+version.getId()+"/info";

	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String resolveRequirement(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, @CookieValue(required=false, value="milestones") List<Long> milestoneIds) {
		Long milestoneId = null;
		if (milestoneIds != null && (! milestoneIds.isEmpty())){
			milestoneId = milestoneIds.get(0);
		}
		RequirementVersion version = versionResolver.resolveByRequirementId(requirementId, milestoneId);
		return "redirect:/requirement-versions/"+version.getId();
	}


}
