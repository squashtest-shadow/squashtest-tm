/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm;

import org.hibernate.SessionFactory;
import org.jhades.JHadesServletListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.orm.hibernate4.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.request.Log4jNestedDiagnosticContextInterceptor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.config.MessagesProperties;
import org.squashtest.tm.web.config.ResourceResolverProperties;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver;
import org.squashtest.tm.web.internal.context.ReloadableSquashTmMessageSource;
import org.squashtest.tm.web.internal.fileupload.MultipartResolverDispatcher;
import org.squashtest.tm.web.internal.fileupload.SquashMultipartResolver;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.interceptor.openedentity.*;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * Application bootstrapper. Uses spring boot to start a Spring MVC webapp.
 * This class should only contain Spring MVC related configuration.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableAutoConfiguration(exclude = HibernateJpaAutoConfiguration.class)
@ComponentScan
@EnableConfigurationProperties({ResourceResolverProperties.class, MessagesProperties.class})
@ImportResource({"classpath*:META-INF/spring/dynamicdao-context.xml", "classpath*:META-INF/spring/dynamicmanager-context.xml"})
public class SquashTm extends WebMvcConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashTm.class);

	public static void main(String[] args) {
		new SpringApplication(SquashTm.class).run(args);
	}

	@Value("${info.app.version}")
	private String appVersion;

	@Inject
	private ThymeleafProperties thymeleafProperties;

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private ResourceProperties resourceProperties;

	@Inject
	private ResourceResolverProperties resourceResolverProperties;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	MessagesProperties messagesProperties;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// OSIV
		OpenSessionInViewInterceptor osiv = new OpenSessionInViewInterceptor();
		osiv.setSessionFactory(sessionFactory);
		registry.addWebRequestInterceptor(osiv);

		// Log4j output enhancement
		Log4jNestedDiagnosticContextInterceptor ndc = new Log4jNestedDiagnosticContextInterceptor();
		ndc.setIncludeClientInfo(true);
		registry.addWebRequestInterceptor(ndc);

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
		argumentResolvers.add(new MilestoneConfigResolver());
	}

	/**
	 * This is a template resolver for fragments. Same as the std template resolver but has a ".html" suffix instead of empty suffix.
	 * Reminder : when we want a view name to be resolved as a thymeleaf view, we have to end it with ".html" already. This
	 * std view resolver is auto-configured by spring boot.
	 *
	 * @return thymeleaf template resolver for fragments
	 */
	@Bean(name = "thymeleaf.templateResolver.fragment")
	public ITemplateResolver fragmentTemplateResolver() {
		ServletContextTemplateResolver res = new ServletContextTemplateResolver();
		res.setPrefix(thymeleafProperties.getPrefix());
		res.setSuffix(".html");
		res.setTemplateMode(thymeleafProperties.getMode());
		res.setCharacterEncoding(thymeleafProperties.getEncoding());
		res.setCacheable(thymeleafProperties.isCache());
		return res;
	}

	@Bean
	public static ServletListenerRegistrationBean<JHadesServletListener> jhadesServletListener() {
		ServletListenerRegistrationBean<JHadesServletListener> bean = new ServletListenerRegistrationBean<>(new JHadesServletListener());
		bean.setOrder(1);
		return bean;
	}

	/**
	 * This overrides spring boot's default (servlet 3 based) multipart resolver.
	 * @return the delegating multipart resolver
	 */
	@Bean
	public MultipartResolver multipartResolver() {
		MultipartResolverDispatcher bean = new MultipartResolverDispatcher();
		bean.setDefaultResolver(defaultMultipartResolver());
		HashMap<String, SquashMultipartResolver> resolverMap = new HashMap<>();
		resolverMap.put(".*/import/upload.*", importMultipartResolver());
		resolverMap.put(".*/importer/.*", importMultipartResolver());
		bean.setResolverMap(resolverMap);
		return bean;
	}

	@Bean
	public SquashMultipartResolver defaultMultipartResolver() {
		return buildSquashMultipartResolver();
	}

	@Bean
	public SquashMultipartResolver importMultipartResolver() {
		SquashMultipartResolver bean = buildSquashMultipartResolver();
		bean.setMaxUploadSizeKey(ConfigurationService.Properties.IMPORT_SIZE_LIMIT);
		return bean;
	}

	private SquashMultipartResolver buildSquashMultipartResolver() {
		SquashMultipartResolver bean = new SquashMultipartResolver();
		bean.setConfig(configurationService);
		return bean;
	}

	/**
	 * Message source which takes into account messages from "fragments"
	 * Overrides spring-boot default
	 * @return the message-source
	 */
	@Bean
	public MessageSource messageSource() {
		ReloadableSquashTmMessageSource bean = new ReloadableSquashTmMessageSource();
		bean.setBasenames(commaDelimitedListToStringArray(trimAllWhitespace(messagesProperties.getBasename())));
		bean.setDefaultEncoding(messagesProperties.getEncoding());
		bean.setCacheSeconds(messagesProperties.getCacheSeconds());
		return bean;
	}
}
