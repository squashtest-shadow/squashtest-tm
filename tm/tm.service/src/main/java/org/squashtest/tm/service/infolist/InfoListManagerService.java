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
package org.squashtest.tm.service.infolist;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.squashtest.tm.domain.infolist.InfoList;

import static org.squashtest.tm.service.security.Authorizations.*;

public interface InfoListManagerService extends InfoListFinderService {

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeDescription(long infoListId, String newDescription);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeLabel(long infoListId, String newLabel);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeCode(long infoListId, String newCode);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeItemsPositions(long infoListId, int newIndex, List<Long> itemsIds);

	boolean isUsedByOneOrMoreProject(long infoListId);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void remove(long infoListId);

	/**
	 * Removes all the info lists matching the given ids.
	 * 
	 * @param ids
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void remove(List<Long> ids);

	/**
	 * @param infoList
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	InfoList persist(InfoList infoList);
}
