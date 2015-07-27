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
package org.squashtest.tm.web.internal.controller.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.model.builder.ListJeditableComboDataBuilder;

/**
 * Jeditable combo data builder which model is {@link --} Useless for the moment but could be used if the specification
 * morphs. It was written that we could change assignment user but, after thinking, it's absurd and means nothing
 * 
 * @author MF
 * @param -
 * 
 */
@Component
@Scope("prototype")
public class ExecutionAssignmentComboDataBuilder extends
		ListJeditableComboDataBuilder<List<User>, ExecutionAssignmentComboDataBuilder> {

	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;

	public ExecutionAssignmentComboDataBuilder() {
		super();

		List<String> userList = new ArrayList<String>();

		// Get all users
		// userList = campaignAdvancedSearchService.findAllAuthorizedUsersForACampaign();

		setModel(userList);

		setModelComparator(LevelComparator.getInstance());
	}

	@Inject
	public void setLabelFormatter(LevelLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

}