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
package org.squashtest.tm.service.internal.requirement;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.exception.execution.RunExecutionException;
import org.squashtest.tm.exception.requirement.link.LinkTypeCodeAlreadyExistsException;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkTypeDao;
import org.squashtest.tm.service.requirement.RequirementVersionLinkTypeManagerService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by jlor on 14/06/2017.
 */
@Transactional
@Service("squashtest.tm.service.RequirementVersionLinkTypeManagerService")
public class RequirementVersionLinkTypeManagerServiceImpl implements RequirementVersionLinkTypeManagerService {

	@Inject
	private RequirementVersionLinkTypeDao linkTypeDao;

	@Override
	public void addLinkType(RequirementVersionLinkType newLinkType) {
		List<RequirementVersionLinkType> typeList = linkTypeDao.getAllRequirementVersionLinkTypes();
		if(linkTypeDao.doesCodeAlreadyExist(newLinkType.getRole1Code())
			|| linkTypeDao.doesCodeAlreadyExist(newLinkType.getRole2Code())) {

			throw new LinkTypeCodeAlreadyExistsException();
		}
		linkTypeDao.save(newLinkType);
	}

	@Override
	public boolean doesLinkTypeCodeAlreadyExist(String code) {
		return linkTypeDao.doesCodeAlreadyExist(code);
	}
}
