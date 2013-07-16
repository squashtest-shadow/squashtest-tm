/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.web.internal.helper.InternationalizableComparator;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Jeditable combo data builder which model is {@link RequirementCriticality}
 * 
 * Note the weird way to inject an comparator for internationalized values and its message source. This class doesn't have an attribute "internationalizationHelper", 
 * but it still has a setter for one. When invoked this setter will actually set the helper for another collaborator : the comparator. 
 * To summarize, events happen in the following order :
 * <ol>
 * 	<li>this class is instancied by a Spring factory.</li>
 * 	<li>immediately an instance of the comparator is implicitly created in the init segment.</li>
 * 	<li>the constructor is then invoked and sets the comparator as the modelComparator</li>
 *  <li>when the constructor is done for, collaborators are injected using @Inject, and that's how the comparator is supplied with the helper.</li>
 *  <li>the object is now fully created and can finally execute the main code.</li>
 * </ol>
 * 
 * @author Gregory Fouquet, bsiri
 * 
 */
@Component
@Scope("prototype")
public class RequirementCriticalityComboDataBuilder extends EnumJeditableComboDataBuilder<RequirementCriticality> {
	
	private InternationalizableComparator comparator = new InternationalizableComparator();
	
	
	public RequirementCriticalityComboDataBuilder() {
		super();
		setModel(RequirementCriticality.values());
		setModelComparator(LevelComparator.getInstance());
	}
	

	@Inject
	public void setLabelFormatter(LevelLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}


}
