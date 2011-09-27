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
package org.squashtest.csp.tm.domain.softdelete;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Embeddable delegate for SoftDeletable entities. Not used yet because of bug :
 * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3957 (when deletedOn column is null, embedded coponent
 * is set to null)
 * 
 * @author Gregory Fouquet
 * 
 */
@Embeddable
public class SoftDeletableSupport {
	@Basic(fetch = FetchType.EAGER)
	@Temporal(TemporalType.TIMESTAMP)
	private Date deletedOn = null;

	public Date getDeletedOn() {
		return deletedOn;
	}

}
