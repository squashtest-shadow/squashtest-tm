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
package org.squashtest.tm.web.internal.argumentresolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.MilestoneFinderService;

/**
 * Use this to know what current milestone instead of @CookieValue. It's check if the milestone in the cookie exist
 * and if the current user can see this milestone.  Return the milestone if ok else return null.
 * @author jsimon
 *
 */
public class MilestoneConfigResolver   implements HandlerMethodArgumentResolver  {

	@Inject
	private MilestoneFinderService milestoneFinderService;


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	public static @interface CurrentMilestone {

	}

	private static final String MILESTONE = "milestones";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentMilestone.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest();
		Cookie cookie = WebUtils.getCookie(servletRequest, MILESTONE);
		//it's under 9000 ! just a fake id in case we don't find cookie.
		String cookieId = (cookie != null && (!StringUtils.isBlank(cookie.getValue()))) ? cookie.getValue() : "-9000";
		final Long milestoneId = Long.parseLong(cookieId);
		List<Milestone> visibles = milestoneFinderService.findAllVisibleToCurrentUser();
		Milestone milestone = (Milestone) CollectionUtils.find(visibles, new Predicate() {
			@Override
			public boolean evaluate(Object milestone) {
				return ((Milestone)milestone).getId().equals(milestoneId);
			}
		});
		return milestone != null ? milestone : null;
	}
}
