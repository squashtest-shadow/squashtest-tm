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
package org.squashtest.tm.web.config;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.hibernate4.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.request.Log4jNestedDiagnosticContextInterceptor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver;
import org.squashtest.tm.web.internal.interceptor.SecurityExpressionResolverExposerInterceptor;
import org.squashtest.tm.web.internal.interceptor.openedentity.*;

import javax.inject.Inject;
import java.util.List;

/**
 * This class configures Spring MVC.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
	@Value("${info.app.version}")
	private String appVersion;

	@Inject
	@Lazy
	private SessionFactory sessionFactory;

	@Inject
	private ResourceProperties resourceProperties;

	@Inject
	private ResourceResolverProperties resourceResolverProperties;

	@Inject
	private MilestoneConfigResolver milestoneConfigResolver;

	@Inject private SecurityExpressionResolverExposerInterceptor securityExpressionResolverExposerInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Log4j output enhancement
		Log4jNestedDiagnosticContextInterceptor ndc = new Log4jNestedDiagnosticContextInterceptor();
		ndc.setIncludeClientInfo(true);
		registry.addWebRequestInterceptor(ndc);

		// OSIV
		OpenSessionInViewInterceptor osiv = new OpenSessionInViewInterceptor();
		osiv.setSessionFactory(sessionFactory);
		registry.addWebRequestInterceptor(osiv);

		// #sec in thymeleaf
		registry.addInterceptor(securityExpressionResolverExposerInterceptor)
			.excludePathPatterns("/", "/login");

		// Opened test cases handling
		registry.addWebRequestInterceptor(new TestCaseViewInterceptor())
			.addPathPatterns(
				"/test-cases/*",
				"/test-cases/*/info",
				"/test-cases/*/verified-requirement-versions/manager"
			);

		// Opened requirements handling
		registry.addWebRequestInterceptor(new RequirementViewInterceptor())
			.addPathPatterns(
				"/requirement-versions/*",
				"/requirement-versions/*/info",
				"/requirement-versions/*/verifying-test-cases/manager"
			);

		// Opened campaigns handling
		registry.addWebRequestInterceptor(new CampaignViewInterceptor())
			.addPathPatterns(
				"/campaigns/*",
				"/campaigns/*/info",
				"/campaigns/*/test-plan/manager"
			);

		// Opened iterations handling
		registry.addWebRequestInterceptor(new IterationViewInterceptor())
			.addPathPatterns(
				"/iterations/*",
				"/iterations/*/info",
				"/iterations/*/test-plan/manager"
			);

		// Opened test-suites handling
		registry.addWebRequestInterceptor(new TestSuiteViewInterceptor())
			.addPathPatterns(
				"/test-suites/*",
				"/test-suites/*/info",
				"/test-suites/*/test-plan/manager"
			);

		// Opened executions handling
		registry.addWebRequestInterceptor(new ExecutionViewInterceptor())
			.addPathPatterns(
				"/executions/*",
				"/executions/*/info"
			);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/home-workspace");
		super.addViewControllers(registry);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/**")
			.addResourceLocations("/imgages/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(new VersionResourceResolver().addContentVersionStrategy("/images/**/*.png", "/images/**/*.gif", "/images/**/*.jpg"))
			.addTransformer(new CssLinkResourceTransformer());

		registry.addResourceHandler("/static/**")
			.addResourceLocations("/static/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(new VersionResourceResolver().addContentVersionStrategy("/images/**/*.png", "/images/**/*.gif", "/images/**/*.jpg"))
			.addTransformer(new CssLinkResourceTransformer());

		registry.addResourceHandler("/css/**")
			.addResourceLocations("/css/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(new VersionResourceResolver().addContentVersionStrategy("/css/**/*.css"))
			.addTransformer(new CssLinkResourceTransformer());

		registry.addResourceHandler("/scripts/**")
			.addResourceLocations("/scripts/")
			.setCachePeriod(resourceProperties.getCachePeriod())
			.resourceChain(resourceResolverProperties.isCache())
			.addResolver(new VersionResourceResolver().addFixedVersionStrategy(appVersion));
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(milestoneConfigResolver);
	}

}
