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
package org.squashtest.csp.tm.internal.infrastructure.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.inject.Inject;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.audit.AuditableSupport;

/**
 * This interceptor transparently logs creation / last modification data of any {@link Auditable} entity.
 *
 * @author Gregory Fouquet
 *
 */
@Component("squashtest.tm.persistence.hibernate.AuditLogInterceptor")
@SuppressWarnings("serial")
public class AuditLogInterceptor extends EmptyInterceptor {
	@Inject
	private UserContextService userContextService;

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		if (isAuditable(entity)) {
			logModificationData(entity, currentState);
			return true;
		}
		return false;
	}

	private boolean isAuditable(Object entity) {
		return AnnotationUtils.findAnnotation(entity.getClass(), Auditable.class) != null;
	}

	private void logModificationData(Object entity, Object[] currentState) {
		try {
			AuditableSupport audit = findAudit(currentState);
			audit.setLastModifiedBy(getCurrentUser());
			audit.setLastModifiedOn(new Date());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Non Auditable entity is : " + entity, e);
		}
	}

	private AuditableSupport findAudit(Object[] state) {
		for (Object field : state) {
			if (field != null && field.getClass().isAssignableFrom(AuditableSupport.class)) {
				return (AuditableSupport) field;
			}
		}
		throw new IllegalArgumentException("Could not find property of type '" + AuditableSupport.class + "'");
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (isAuditable(entity)) {
			logCreationData(entity, state);
			return true;
		}
		return false;
	}

	private void logCreationData(Object entity, Object[] state) {
		try {
			AuditableSupport audit = findAudit(state);
			
			//one sets defaults only if they aren't provided at creation time
			if ( (audit.getCreatedBy() ==null) && (audit.getCreatedOn()==null)){
				
				audit.setCreatedBy(getCurrentUser());
				audit.setCreatedOn(new Date());
				
			}
			
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Non Auditable entity is : " + entity, e);
		}
	}

	private String getCurrentUser() {
		return userContextService.getUsername();
	}
}
