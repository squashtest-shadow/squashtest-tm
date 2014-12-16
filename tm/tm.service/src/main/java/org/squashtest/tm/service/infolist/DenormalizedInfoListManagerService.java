/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.infolist;

import org.squashtest.tm.domain.infolist.DenormalizedInfoListItem;
import org.squashtest.tm.domain.infolist.DenormalizedListItemReference;

public interface DenormalizedInfoListManagerService extends DenormalizedInfoListFinderService{


	/**
	 * Returns or create a persistent instance of that DenormalizedInfoListItem.
	 * If the parameter is already persistent, will return the persistent instance that
	 * matches the reference. If such persistent instance doesn't exist, will denormalize
	 * the whole list then return the item created that way.
	 * 
	 * @param item
	 * @return
	 */
	DenormalizedInfoListItem findOrDenormalize(DenormalizedListItemReference reference);
}
