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

package org.squashtest.csp.tm.domain.customfield;

import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;

/**
 * Enumerates the entities which can be bounded to custom fields.
 * 
 * @author Gregory Fouquet
 *
 */
public enum BindableEntity implements Internationalizable {
	
	TEST_CASE(){
		@Override
		public Class<?> getReferencedClass() {
			return TestCase.class;
		}
	},
	
	CAMPAIGN(){
		@Override
		public Class<?> getReferencedClass() {
			return Campaign.class;
		} 
	},
	ITERATION(){
		@Override
		public Class<?> getReferencedClass() {
			return Iteration.class;
		}
	},
	TEST_SUITE(){
		@Override
		public Class<?> getReferencedClass() {
			return TestSuite.class;
		}
	},
	REQUIREMENT_VERSION(){
		@Override
		public Class<?> getReferencedClass() {
			return RequirementVersion.class;
		}
	};
	
	private static final String I18N_NAMESPACE = "label.customField.bindableEntity.";

	/**
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return I18N_NAMESPACE + name();
	}
	
	public abstract Class<?> getReferencedClass();
}
