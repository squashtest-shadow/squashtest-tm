/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.service.requirement.RequirementVersionLinkTypeManagerService;
import org.squashtest.tm.web.internal.http.ContentTypes;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jlor on 14/06/2017.
 */
@Controller
@RequestMapping("/requirement-link-type")
public class RequirementVersionLinkTypeController {

	@Inject
	private RequirementVersionLinkTypeManagerService linkTypeManagerService;

	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST)
	public void addLinkType(@Valid @ModelAttribute RequirementVersionLinkType newLinkType) {
		linkTypeManagerService.addLinkType(newLinkType);
	}

	@ResponseBody
	@RequestMapping(value = "/check-codes", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public Map<String, Object> doesLinkTypeCodesExist(@Valid @ModelAttribute RequirementVersionLinkType linkType) {
		Map<String, Object> resultMap = new HashMap<>(2);
		resultMap.put("code1Exists", linkTypeManagerService.doesLinkTypeCodeAlreadyExist(linkType.getRole1Code()));
		resultMap.put("code2Exists", linkTypeManagerService.doesLinkTypeCodeAlreadyExist(linkType.getRole2Code()));
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.POST, params = { "id=requirement-link-type-default" })
	public void changeDefault(@PathVariable Long linkTypeId) {
		linkTypeManagerService.changeDefault(linkTypeId);
	}
}
