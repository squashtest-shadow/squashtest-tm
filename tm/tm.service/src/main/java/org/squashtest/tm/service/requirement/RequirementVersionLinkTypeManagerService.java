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
package org.squashtest.tm.service.requirement;

import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;

/**
 * Created by jlor on 14/06/2017.
 */
public interface RequirementVersionLinkTypeManagerService {

	void addLinkType(RequirementVersionLinkType linkType);

	boolean doesLinkTypeCodeAlreadyExist(String code);
	boolean doesLinkTypeCodeAlreadyExist(String code, Long linkTypeId);

	void changeDefault(Long linkTypeId);

	void changeRole1(Long linkTypeId, String newRole1);
	void changeRole2(Long linkTypeId, String newRole2);

	void changeCode1(Long linkTypeId, String newCode1);
	void changeCode2(Long linkTypeId, String newCode2);

	boolean isLinkTypeDefault(Long linkTypeId);
	boolean isLinkTypeUsed(Long linkTypeId);

	void deleteLinkType(Long linkTypeId);
}
