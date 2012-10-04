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
//CHECKSTYLE:OFF
package org.squashtest.csp.tm.domain.softdelete;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.squashtest.csp.tm.domain.softdelete.SoftDeletableSupport;

import org.squashtest.csp.tm.domain.audit.AuditableSupport;

/**
 * Provides default implementation for @SoftDeletable entities
 * 
 * @author Gregory Fouquet
 * 
 */
public interface SoftDeletableMixin {
	boolean isDeleted();

	Date getDeletedOn();

	static aspect Impl {
		@Basic(fetch = FetchType.EAGER)
		@Temporal(TemporalType.TIMESTAMP)
		@Column(name="DELETED_ON") // required because introduced field name is not "deletedOn"
		private Date SoftDeletableMixin.deletedOn = null;

		public boolean SoftDeletableMixin.isDeleted() {
			return this.getDeletedOn() != null;
		}

		public Date SoftDeletableMixin.getDeletedOn() {
			return this.deletedOn;
		}
	}

}
// CHECKSTYLE:ON