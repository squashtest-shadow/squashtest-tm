/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.service.security

import java.util.List

import org.springframework.security.acls.model.ObjectIdentity;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.service.security.acls.PermissionGroup;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

class StubObjectAclService implements ObjectAclService {

	@Override
	public List<PermissionGroup> findAllPermissionGroupsByNamespace(String namespace) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.model.ObjectAclService#retriveUserAndAclGroupNameFromIdentityAndClass(long, java.lang.Class, org.squashtest.tm.core.foundation.collection.Sorting, org.squashtest.tm.core.foundation.collection.Filtering)
	 */
	@Override
	public List<Object[]> retriveUserAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass,
			Sorting sorting, Filtering filtering) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.squashtest.tm.service.security.acls.model.ObjectAclService#removeAllResponsibilitiesForParty(long)
	 */
	@Override
	public void removeAllResponsibilitiesForParty(long partyId) {
		
	}

	@Override
	public void removeAllResponsibilities(String userLogin, ObjectIdentity entityRef) {

	}

	@Override
	public void removeAllResponsibilities(ObjectIdentity entityRef) {

	}

	@Override
	public List<Object[]> retrieveClassAclGroupFromUserLogin(String userLogin, String qualifiedClassName) {
		return Collections.emptyList();
	}

	@Override
	public List<Long> findObjectWithoutPermissionByLogin(String userLogin, String qualifiedClass) {
		return Collections.emptyList();
	}

	@Override
	public void addNewResponsibility(String userLogin, ObjectIdentity entityRef, String qualifiedName) {

	}

	@Override
	public List<String> findUsersWithWritePermission(List<ObjectIdentity> entityRefs) {
		return Collections.emptyList();
	}

	@Override
	public List<Object[]> retriveUserAndAclGroupNameFromIdentityAndClass(long entityId, Class<?> entityClass) {
		return Collections.emptyList();
	}

	@Override
	public List<Long> findUsersWithoutPermissionByObject(long objectId, String qualifiedClassName) {
		return Collections.emptyList();
	}

	@Override
	public List<String> findUsersWithExecutePermission(List<ObjectIdentity> entityRefs) {
		return Collections.emptyList();
	}

}
