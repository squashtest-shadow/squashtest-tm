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
package org.squashtest.tm.service.statistics.campaign;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;


public final class ScheduledIteration{
	
	private long id;
	private String name;
	private Date scheduledStart;
	private Date scheduledEnd;
	
	// an entry = { Date, int }
	private Collection<Object[]> cumulativeTestsByDate = new LinkedList<Object[]>();
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getScheduledStart() {
		return scheduledStart;
	}
	
	public void setScheduledStart(Date scheduledStart) {
		this.scheduledStart = scheduledStart;
	}
	
	public Date getScheduledEnd() {
		return scheduledEnd;
	}
	
	public void setScheduledEnd(Date scheduledEnd) {
		this.scheduledEnd = scheduledEnd;
	}

	public Collection<Object[]> getCumulativeTestsByDate() {
		return cumulativeTestsByDate;
	}
	
	public void addCumulativeTestByDate(Object[] testByDate) {
		cumulativeTestsByDate.add(testByDate);
	}
	
}