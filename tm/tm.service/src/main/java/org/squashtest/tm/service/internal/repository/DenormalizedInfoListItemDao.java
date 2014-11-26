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
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.domain.infolist.DenormalizedInfoList;
import org.squashtest.tm.domain.infolist.DenormalizedInfoListItem;
import org.squashtest.tm.domain.infolist.DenormalizedListItemReference;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.domain.infolist.UserListItem;

public interface DenormalizedInfoListItemDao extends EntityDao<DenormalizedInfoListItem>{

	/**
	 * <p>
	 * 	Given a {@link InfoListItem}, will
	 * 	find the corresponding denormalized item if exists. Note that the right denormalized item must
	 * 	match the code, but also the right denormalized list (which is identified by its original ID and
	 * 	version, see {@link DenormalizedInfoList}.
	 * </p>
	 * 
	 * <p>
	 * 	Note that such lookup might fail when :
	 * 	<ul>
	 * 		<li>the list of the item given as parameter hasn't been denormalized yet</li>
	 * 		<li>there is no such denormalized item that matches the code of the item and the id/version of item.infoList</li>
	 * 	</ul>
	 * 
	 * 	Also, one should not provide a {@link ListItemReference} as parameter, because they don't always reference a list.
	 * You should provide either a {@link SystemListItem} or a {@link UserListItem}.
	 * 
	 * </p>
	 * 
	 * @param reference
	 * @return
	 */
	DenormalizedInfoListItem findByReference(InfoListItem item);


	/**
	 * <p>
	 * 	Same than for {@link #findByReference(InfoListItem)}, except that the 3-plet code/listid/listversion are
	 * held by a {@link DenormalizedListItemReference}.
	 * </p>
	 * 
	 * @param reference
	 * @return
	 */
	DenormalizedInfoListItem findByReference(DenormalizedListItemReference item);
}
