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
package org.squashtest.tm.service.internal.infolist;

import javax.inject.Inject;

import org.squashtest.tm.domain.infolist.DenormalizedInfoListItem;
import org.squashtest.tm.domain.infolist.DenormalizedListItemReference;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.service.infolist.DenormalizedInfoListItemManagerService;
import org.squashtest.tm.service.internal.repository.DenormalizedInfoListItemDao;

public class DenormalizedInfoListItemManagerServiceImpl implements DenormalizedInfoListItemManagerService{

	@Inject
	private DenormalizedInfoListItemDao itemDao;

	@Override
	public DenormalizedInfoListItem findById(Long id) {
		return itemDao.findById(id);
	}

	@Override
	public DenormalizedInfoListItem findByReference(InfoListItem item) {
		return itemDao.findByReference(item);
	}

	@Override
	public DenormalizedInfoListItem findByReference(DenormalizedListItemReference reference) {
		return itemDao.findByReference(reference);
	}



}
