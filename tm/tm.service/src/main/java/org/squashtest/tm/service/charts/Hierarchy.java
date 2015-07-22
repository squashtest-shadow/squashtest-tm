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
package org.squashtest.tm.service.charts;


/**
 * <p>TODO : some columns may be aggregated as a hierarchy. A hierarchy represents a special aggregation that a database cannot know of
 * because it is closely tied to the business domain. As such, there will be as many hierarchy as there are Datatypes specific to Squash.</p>
 * 
 * <p>for instance, an ExecutionStatus may have the following hierarchy : normal (each status enumerated as usual), canonical
 * (see canonical status in the definition of ExecutionStatus), Terminated and non terminated (PASSED, BBLOCKED and FAILURE
 * against READY and RUNNING) etc</p>
 * 
 * @author bsiri
 *
 */
public enum Hierarchy {

}
