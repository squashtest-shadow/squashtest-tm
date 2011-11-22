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
package org.squashtest.csp.tm.domain.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;
import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.requirement.Requirement;

/**
 * This aspect advises a Requirement to raise an event when a Requirement's
 * intrinsic property is modified.
 * 
 * @author Gregory Fouquet
 * 
 */
public aspect RequirementModificationEventPublisherAspect {
	@Inject
	private RequirementAuditor auditor;
	
	@Inject
	private UserContextService userContext;

	public UserContextService getUserContext() {
		return userContext;
	}

	public void setUserContext(UserContextService userContext) {
		this.userContext = userContext;
	}

	public RequirementAuditor getAuditor() {
		return auditor;
	}

	public void setAuditor(RequirementAuditor auditor) {
		this.auditor = auditor;
	}

	private pointcut executeLargePropertySetter() : execution(public void org.squashtest.csp.tm.domain.requirement.Requirement.setDescription(*));

	private pointcut executeSimplePropertySetter() : execution(public void org.squashtest.csp.tm.domain.requirement.Requirement.set*(*)) && !executeLargePropertySetter();

	/**
	 * Advises setters of a Requirement and raises an modification event after
	 * the setter is used to change the requirement's state. If the aspect is
	 * disabled, does nothing.
	 * 
	 * @param req
	 * @param newValue
	 */
	void around(Requirement req, Object newValue) : executeSimplePropertySetter() && target(req) && args(newValue) {
		if (aspectIsEnabled()) {
			String propertyName = extractModifiedPropertyName(thisJoinPoint);
			Object oldValue = readOldValue(req, propertyName);

			// this statement cannot be factored out
			proceed(req, newValue);

			if (requirementWasModified(oldValue, newValue)) {
				raiseSimplePropertyEvent(req, propertyName, oldValue, newValue);
			}
		} else {
			// this statement cannot be factored out
			proceed(req, newValue);
		}
	}

	private boolean requirementWasModified(Object oldValue, Object newValue) {
		return !ObjectUtils.equals(ObjectUtils.toString(oldValue),
				ObjectUtils.toString(newValue));
	}

	private void raiseSimplePropertyEvent(Requirement req, String propertyName,
			Object oldValue, Object newValue) {
		RequirementPropertyChange event = RequirementPropertyChange.builder()
				.setSource(req)
				.setModifiedProperty(propertyName)
				.setOldValue(oldValue)
				.setNewValue(newValue)
				.setAuthor(currentUser())
				.build();
		
		auditor.notify(event);
	}

	private Object readOldValue(Requirement req, String propertyName) {
		Method propertyGetter = null;
		try {
			propertyGetter = Requirement.class.getMethod("get"
					+ WordUtils.capitalize(propertyName));
		} catch (NoSuchMethodException e) {
			ReflectionUtils.handleReflectionException(e);
		}

		return ReflectionUtils.invokeMethod(propertyGetter, req);
	}

	private String extractModifiedPropertyName(JoinPoint setterJoinPoint) {
		String methodName = setterJoinPoint.getSignature().getName();
		String propertyName = methodName.substring(3); // method is assumed to
														// be "setXxx"

		return WordUtils.uncapitalize(propertyName);
	}

	/**
	 * 
	 * @return <code>true</code> if the aspect is enabled and should raise
	 *         events.
	 */
	private boolean aspectIsEnabled() {
		return auditor != null;
	}

	/**
	 * Advises setters of a Requirement and raises an modification event after
	 * the setter is used to change the requirement's state. If the aspect is
	 * disabled, does nothing.
	 * 
	 * @param req
	 * @param newValue
	 */
	void around(Requirement req, Object newValue) : executeLargePropertySetter() && target(req) && args(newValue) {
		if (aspectIsEnabled()) {
			String propertyName = extractModifiedPropertyName(thisJoinPoint);
			Object oldValue = readOldValue(req, propertyName);

			// this statement cannot be factored out
			proceed(req, newValue);

			if (requirementWasModified(oldValue, newValue)) {
				raiseLargePropertyEvent(req, propertyName, oldValue, newValue);
			}
		} else {
			// this statement cannot be factored out
			proceed(req, newValue);
		}
	}	
	
	private void raiseLargePropertyEvent(Requirement req, String propertyName,
			Object oldValue, Object newValue) {
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
				.setSource(req)
				.setModifiedProperty(propertyName)
				.setOldValue(oldValue)
				.setNewValue(newValue)
				.setAuthor(currentUser())
				.build();
		
		auditor.notify(event);
	}
	
	private String currentUser() {
		if (userContext != null) {
			return userContext.getUsername();
		} else {
			return "unknown";
		}
	}
}
