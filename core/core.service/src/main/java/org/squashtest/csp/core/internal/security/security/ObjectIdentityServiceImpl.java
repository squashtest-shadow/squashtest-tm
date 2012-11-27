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
package org.squashtest.csp.core.internal.security.security;

import javax.inject.Inject;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.security.acls.jdbc.JdbcManageableAclService;
import org.squashtest.csp.core.service.security.ObjectIdentityService;

@Service("squashtest.core.security.ObjectIdentityService")
@Transactional
public class ObjectIdentityServiceImpl implements ObjectIdentityService {
	
	@Inject
	private JdbcManageableAclService aclService;


	@Override
	public void addObjectIdentity(long objectId, Class<?> entityClass) {
		ObjectIdentity objectIdentity =  new ObjectIdentityImpl(entityClass, objectId);
		aclService.createObjectIdentity(objectIdentity);
	}

	/**
	 * @see org.squashtest.csp.core.service.security.ObjectIdentityService#removeObjectIdentity(long, java.lang.Class)
	 */
	@Override
	public void removeObjectIdentity(long entityId, Class<?> entityType) {
		ObjectIdentity objectIdentity =  new ObjectIdentityImpl(entityType, entityId);
		aclService.removeObjectIdentity(objectIdentity);
	}

}
