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
package org.squashtest.tm.service.internal.customfield;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.id.uuid.Helper;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.testcase.ActionStepCollector;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;


/**
 * Read the definition of {@link Helper} instead
 * 
 * 
 * @author bsiri
 *
 */

@Service("squashtest.tm.service.CustomFieldHelperService")
public class CustomFieldHelperServiceImpl implements CustomFieldHelperService {

	@Inject
	private CustomFieldBindingFinderService cufBindingService;

	@Inject
	private CustomFieldValueManagerService cufValuesService;
	

	/* (non-Javadoc)
	 * @see org.squashtest.tm.web.internal.service.ICustomFieldHelperService#hasCustomFields(org.squashtest.tm.domain.customfield.BoundEntity)
	 */
	@Override
	public boolean hasCustomFields(BoundEntity entity){
		return cufValuesService.hasCustomFields(entity);
	}
	
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.web.internal.service.ICustomFieldHelperService#newHelper(X)
	 */
	@Override
	public <X extends BoundEntity> CustomFieldHelper<X> newHelper(X entity){
		CustomFieldHelperImpl<X> helper =  new CustomFieldHelperImpl<X>(entity);
		helper.setCufBindingService(cufBindingService);
		helper.setCufValuesService(cufValuesService);
		return helper;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.web.internal.service.ICustomFieldHelperService#newHelper(java.util.List)
	 */
	@Override
	public <X extends BoundEntity> CustomFieldHelper<X> newHelper(List<X> entities){
		CustomFieldHelperImpl<X> helper =  new CustomFieldHelperImpl<X>(entities);
		helper.setCufBindingService(cufBindingService);
		helper.setCufValuesService(cufValuesService);
		return helper;
	}
	
	/* (non-Javadoc)
	 * @see org.squashtest.tm.web.internal.service.ICustomFieldHelperService#newStepsHelper(java.util.List)
	 */
	@Override
	public CustomFieldHelper<ActionTestStep> newStepsHelper(List<TestStep> steps){
		CustomFieldHelperImpl<ActionTestStep> helper =   new CustomFieldHelperImpl<ActionTestStep> (new ActionStepCollector().collect(steps));
		helper.setCufBindingService(cufBindingService);
		helper.setCufValuesService(cufValuesService);
		return helper;		
	}
	

	
}
