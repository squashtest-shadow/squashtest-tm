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
import java.util.LinkedList;


public final class CampaignProgressionStatistics {
	
	private Collection<String> i18nErrors;
	
	private Collection<ScheduledIteration> scheduledIterations = new LinkedList<ScheduledIteration>();


	public Collection<ScheduledIteration> getScheduledIterations() {
		return scheduledIterations;
	}
	
	public void addi18nErrorMessage(String i18nErrorMessage){
		if (i18nErrors==null){
			i18nErrors = new LinkedList<String>();
		}
		i18nErrors.add(i18nErrorMessage);
	}
	
	public Collection<String> getErrors(){
		return i18nErrors;
	}

	public void setScheduledIterations(
			Collection<ScheduledIteration> scheduledIterations) {
		this.scheduledIterations = scheduledIterations;
	}

	
	
}
