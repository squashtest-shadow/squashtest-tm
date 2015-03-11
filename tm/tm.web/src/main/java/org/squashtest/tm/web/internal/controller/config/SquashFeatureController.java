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
package org.squashtest.tm.web.internal.controller.config;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.listener.SquashConfigContextExposer;

/**
 * Controller for Squash TM app wide features
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/features")
public class SquashFeatureController {
	@Inject
	private ConfigurationService configurationService;

	@Inject
	private ServletContext applicationScope;

	@RequestMapping(value = "/milestones", method = RequestMethod.POST, params = "enabled")
	@ResponseBody
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Secured("ROLE_ADMIN")
	public void setMilestonesFeature(@RequestParam("enabled") boolean enabled) {
		// XXX featuresSetvice.enableMilestones which should delegate
		configurationService.set(ConfigurationService.MILESTONE_FEATURE_ENABLED, enabled);
		applicationScope.setAttribute(SquashConfigContextExposer.MILESTONE_FEATURE_ENABLED, enabled);

	}

}
