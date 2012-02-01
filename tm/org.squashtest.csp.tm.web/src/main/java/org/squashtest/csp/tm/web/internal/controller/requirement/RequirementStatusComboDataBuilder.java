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

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.LevelComparator;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.csp.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Jeditable combo data builder which model is {@link RequirementStatus}
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
public class RequirementStatusComboDataBuilder extends EnumJeditableComboDataBuilder<RequirementStatus> {
	public RequirementStatusComboDataBuilder() {
		super();
		setModel(RequirementStatus.values());
		setModelComparator(LevelComparator.getInstance());
	}

	@Inject
	public void setLabelFormatter(LevelLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

	/**
	 * @see org.squashtest.csp.tm.web.internal.model.builder.EnumJeditableComboDataBuilder#itemKey(java.lang.Enum)
	 */
	@Override
	protected String itemKey(RequirementStatus item) {
		String defaultKey = super.itemKey(item);
		RequirementStatus selected = getSelectedItem();
		
		if (selected != null) {
			if(selected.getDisabledStatus().contains(item)) {
				defaultKey = "disabled." + defaultKey;
			}
		}
		return defaultKey;
	}
}
