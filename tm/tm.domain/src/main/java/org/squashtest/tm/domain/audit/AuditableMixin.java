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
package org.squashtest.tm.domain.audit;

import java.util.Date;

/**
 * Defines interface for @Auditable entities. Any @Auditable entity will be modified to implement this interface.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface AuditableMixin {
	
	Date getCreatedOn();
	
	String getCreatedBy();

	Date getLastModifiedOn();

	String getLastModifiedBy();
	
	void setCreatedBy(String createdBy);

	void setCreatedOn(Date createdOn);

	void setLastModifiedBy(String lastModifiedBy);

	void setLastModifiedOn(Date lastModifiedOn);
		
}
