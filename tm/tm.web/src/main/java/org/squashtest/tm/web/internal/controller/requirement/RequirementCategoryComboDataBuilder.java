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
package org.squashtest.tm.web.internal.controller.requirement;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.web.internal.helper.InternationalizableComparator;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Jeditable combo data builder which model is {@link RequirementCategory}
 * 
 * @author mpagnon
 * 
 */
@Component
@Scope("prototype")
public class RequirementCategoryComboDataBuilder extends EnumJeditableComboDataBuilder<RequirementCategory, RequirementCategoryComboDataBuilder> {
	
	private InternationalizableComparator comparator = new InternationalizableComparator();
	
	
	public RequirementCategoryComboDataBuilder() {
		super();
		setModel(RequirementCategory.values());
		setModelComparator(comparator);
	}

	@Inject
	public void setLabelFormatter(InternationalizableLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

	@Inject
	public void setInternationalizationHelper(InternationalizationHelper helper) {
		comparator.setHelper(helper);
	}
	
}
