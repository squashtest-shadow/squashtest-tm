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

package org.squashtest.core.api.report;

import org.squashtest.csp.api.infrastructure.Internationalizable;

/**
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public enum ReportType implements Internationalizable {
	PROGRESS_FOLLOWUP("report.type.progressfollowup.name"), 
	GENERIC("report.type.progressfollowup.name");

	private final String i18nKey;

	/**
	 * @param i18nKey
	 */
	private ReportType(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	/**
	 * @see org.squashtest.csp.api.infrastructure.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return i18nKey;
	}

}
