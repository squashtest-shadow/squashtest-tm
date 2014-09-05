/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.batchimport;

import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.importer.ImportStatus;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;

public class LogTrain {

	private List<LogEntry> entries = new LinkedList<LogEntry>();
	private boolean criticalErrors = false;

	void addEntry(LogEntry entry){
		if (entry != null){
			entries.add(entry);
			if (entry.getStatus() == ImportStatus.FAILURE){
				criticalErrors = true;
			}
		}
	}

	void addEntries(List<LogEntry> entries){
		for (LogEntry entry : entries){
			addEntry(entry);
		}
	}

	void append(LogTrain train){
		addEntries(train.entries);
	}


	public List<LogEntry> getEntries(){
		return entries;
	}


	boolean hasCriticalErrors(){
		return criticalErrors;
	}

	boolean hasNoErrorWhatsoever(){
		return entries.isEmpty();
	}


	void setForAll(int lineNumber){
		for (LogEntry entry : entries){
			entry.setLine(lineNumber);
		}
	}

	void setForAll(ImportMode mode){
		for (LogEntry entry : entries) {
			entry.setMode(mode);
		}
	}

	void setForAll(Target target){
		for (LogEntry entry : entries){
			entry.setTarget(target);
		}
	}


}
