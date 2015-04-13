/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

public final class MilestoneModelUtils {

	public static String timeIntervalToString(Collection<Milestone> milestones, InternationalizationHelper i18nHelper, Locale locale){

		if (milestones.isEmpty()){
			return "--";
		}

		Date minDate = null;
		Date maxDate = null;

		Iterator<Milestone> iter = milestones.iterator();
		while(iter.hasNext()){
			Milestone m  = iter.next();
			Date date = m.getEndDate();
			if (minDate == null || date.before(minDate)){
				minDate = date;
			}
			if (maxDate == null || date.after(maxDate)){
				maxDate = date;
			}
		}

		String strMindate = i18nHelper.localizeShortDate(minDate, locale);
		String strMaxdate = i18nHelper.localizeShortDate(maxDate, locale);

		return strMindate + " - " + strMaxdate;
	}

}
