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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.InfoListItemDao;

@Transactional
@Service("squashtest.tm.service.InfoListItemManagerService")
public class InfoListItemManagerServiceImpl implements InfoListItemManagerService {

	@Inject
	private InfoListItemDao itemDao;

	@Inject
	private GenericProjectDao projectDao;

	// ************* "Finder" methods **************** \\

	@Override
	public SystemListItem getSystemRequirementCategory() {
		return itemDao.getSystemRequirementCategory();
	}

	@Override
	public SystemListItem getSystemTestCaseNature() {
		return itemDao.getSystemTestCaseNature();
	}

	@Override
	public SystemListItem getSystemTestCaseType() {
		return itemDao.getSystemTestCaseType();
	}

	@Override
	public InfoListItem findById(Long id){
		return itemDao.findById(id);
	}

	@Override
	public InfoListItem findByCode(String code){
		return itemDao.findByCode(code);
	}

	@Override
	public InfoListItem findReference(ListItemReference reference){
		return itemDao.findReference(reference);
	}

	@Override
	public InfoListItem findDefaultRequirementCategory(long projectId) {
		return itemDao.findDefaultRequirementCategory(projectId);
	}

	@Override
	public InfoListItem findDefaultTestCaseNature(long projectId) {
		return itemDao.findDefaultTestCaseNature(projectId);
	}

	@Override
	public InfoListItem findDefaultTestCaseType(long projectId) {
		return itemDao.findDefaultTestCaseType(projectId);
	}

	@Override
	public boolean isCategoryConsistent(long projectId, String itemCode) {
		return itemDao.isCategoryConsistent(projectId, itemCode);
	}

	@Override
	public boolean isNatureConsistent(long projectId, String itemCode) {
		return itemDao.isNatureConsistent(projectId, itemCode);
	}

	@Override
	public boolean isTypeConsistent(long projectId, String itemCode) {
		return itemDao.isTypeConsistent(projectId, itemCode);
	}


}
