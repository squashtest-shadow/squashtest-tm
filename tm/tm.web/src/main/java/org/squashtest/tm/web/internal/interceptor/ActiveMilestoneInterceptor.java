package org.squashtest.tm.web.internal.interceptor;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;
import org.squashtest.tm.annotation.WebComponent;

@WebComponent
public class ActiveMilestoneInterceptor implements HandlerInterceptor {

	private static final String MILESTONE = "milestones";

	@Inject
	private ActiveMilestoneHolder milestoneHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		Cookie cookie = WebUtils.getCookie(request, MILESTONE);
		// it's under 9000 ! just a fake id in case we don't find cookie.
		String cookieId = (cookie != null && (!StringUtils.isBlank(cookie.getValue()))) ? cookie.getValue() : "-9000";
		final Long milestoneId = Long.parseLong(cookieId);
		milestoneHolder.setActiveMilestone(milestoneId);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		milestoneHolder.clearContext();

	}

}
