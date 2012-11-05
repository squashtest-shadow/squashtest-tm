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

package org.squashtest.csp.tm.service.customfield;

import javax.validation.constraints.NotNull;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.domain.customfield.CustomFieldOption;
import org.squashtest.csp.tm.domain.customfield.SingleSelectField;

/**
 * Facade service for custom fields management.
 * 
 * @author Gregory Fouquet
 * 
 */
@Transactional
public interface CustomFieldManagerService extends CustomCustomFieldManagerService {
	static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";

	/**
	 * @param name
	 * @return
	 */
	CustomField findByName(@NotNull String name);
	
	public CustomField findById(Long customFieldId);

	@PreAuthorize(HAS_ROLE_ADMIN)
	public void changeLabel(long customFieldId, String label);
	
	@PreAuthorize(HAS_ROLE_ADMIN)	
	public void changeDefaultValue(long customFieldId, String defaultValue);

}
