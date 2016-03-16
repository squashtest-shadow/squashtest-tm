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
package org.squashtest.tm.service.internal.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * @author http://springtips.blogspot.fr/2007/07/changing-log4j-logging-levels.html
 * 
 */
@Component
public class Log4jLoggerLevelModifier {
	public void setLogLevel(String loggerName, String level) {
		if ("trace".equalsIgnoreCase(level)) {
			Logger.getLogger(loggerName).setLevel(Level.TRACE);
		} else if ("debug".equalsIgnoreCase(level)) {
			Logger.getLogger(loggerName).setLevel(Level.DEBUG);
		} else if ("info".equalsIgnoreCase(level)) {
			Logger.getLogger(loggerName).setLevel(Level.INFO);
		} else if ("error".equalsIgnoreCase(level)) {
			Logger.getLogger(loggerName).setLevel(Level.ERROR);
		} else if ("fatal".equalsIgnoreCase(level)) {
			Logger.getLogger(loggerName).setLevel(Level.FATAL);
		} else if ("warn".equalsIgnoreCase(level)) {
			Logger.getLogger(loggerName).setLevel(Level.WARN);
		}
	}

	public void setTraceLevel(String logger) {
		Logger.getLogger(logger).setLevel(Level.TRACE);
	}

	public void setDebugLevel(String logger) {
		Logger.getLogger(logger).setLevel(Level.DEBUG);
	}

	public void setInfoLevel(String logger) {
		Logger.getLogger(logger).setLevel(Level.INFO);
	}

	public void setWarnLevel(String logger) {
		Logger.getLogger(logger).setLevel(Level.WARN);
	}

	public void setErrorLevel(String logger) {
		Logger.getLogger(logger).setLevel(Level.ERROR);
	}
}
