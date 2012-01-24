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

package org.squashtest.csp.tm.web.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic report after an action has been processed.
 * 
 * @author Gregory Fouquet
 * 
 */
public class BatchActionSummary {
	private boolean hasRejections;

	private List<EntitySummary> rejections = new ArrayList<EntitySummary>();

	public BatchActionSummary() {
		super();
	}

	public void addRejectedEntity(long id) {
		rejections.add(new EntitySummary(id));
		hasRejections = true;
	}

	public boolean isHasRejections() {
		return hasRejections;
	}

	public List<EntitySummary> getRejections() {
		return rejections;
	}
}
