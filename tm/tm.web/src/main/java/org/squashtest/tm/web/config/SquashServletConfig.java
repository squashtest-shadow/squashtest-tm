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
package org.squashtest.tm.web.config;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.springframework.util.StringUtils.trimAllWhitespace;

import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.Order;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.web.BugTrackerContextPersistenceFilter;
import org.squashtest.tm.api.config.SquashPathProperties;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.context.ReloadableSquashTmMessageSource;
import org.squashtest.tm.web.internal.fileupload.MultipartResolverDispatcher;
import org.squashtest.tm.web.internal.fileupload.SquashMultipartResolver;
import org.squashtest.tm.web.internal.filter.AjaxEmptyResponseFilter;
import org.squashtest.tm.web.internal.filter.UserConcurrentRequestLockFilter;
import org.squashtest.tm.web.internal.listener.HttpSessionLifecycleLogger;
import org.squashtest.tm.web.internal.listener.OpenedEntitiesLifecycleListener;
import org.thymeleaf.spring4.resourceresolver.SpringResourceResourceResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

/**
 * Servlet context config (mostly). Not in SquashServletInitializer becauses it delays the servlet context initialization for
 * too long.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@EnableConfigurationProperties({MessagesProperties.class})
@Configuration
public class SquashServletConfig {
	@Inject
	private MessagesProperties messagesProperties;
	@Inject
	private ThymeleafProperties thymeleafProperties;
	@Inject
	private SquashPathProperties squashPathProperties;

	/**
	 * Message source which takes into account messages from "fragments"
	 * Overrides spring-boot default
	 * @return the message-source
	 */
	@Bean
	public MessageSource messageSource() {
		ReloadableSquashTmMessageSource bean = new ReloadableSquashTmMessageSource();
		bean.setSquashPathProperties(squashPathProperties);
		bean.setBasenames(commaDelimitedListToStringArray(trimAllWhitespace(messagesProperties.getBasename())));
		bean.setDefaultEncoding(messagesProperties.getEncoding());
		bean.setCacheSeconds(messagesProperties.getCacheSeconds());
		return bean;
	}




	/**
	 * This is a template resolver for fragments. Same as the std template resolver but has a ".html" suffix instead of empty suffix.
	 * Reminder : when we want a view name to be resolved as a thymeleaf view, we have to end it with ".html" already. This
	 * std view resolver is auto-configured by spring boot.
	 *
	 * @return thymeleaf template resolver for fragments
	 */
	@Bean(name = "thymeleaf.templateResolver.fragment")
	public ITemplateResolver fragmentTemplateResolver(SpringResourceResourceResolver resourceResolver) {
		TemplateResolver res = new TemplateResolver();
		res.setResourceResolver(resourceResolver);
		res.setPrefix(thymeleafProperties.getPrefix());
		res.setSuffix(".html");
		res.setTemplateMode(thymeleafProperties.getMode());
		res.setCharacterEncoding(thymeleafProperties.getEncoding().name());
		res.setCacheable(thymeleafProperties.isCache());
		return res;
	}

	@Bean(name = "thymeleaf.templateResolver.plugins")
	public ITemplateResolver thymeleafClasspathTemplateResolver(SpringResourceResourceResolver resourceResolver) {
		TemplateResolver res = new TemplateResolver();
		res.setResourceResolver(resourceResolver);
		res.setPrefix("classpath:/templates/");
		res.setSuffix("");
		res.setTemplateMode(thymeleafProperties.getMode());
		res.setCharacterEncoding(thymeleafProperties.getEncoding().name());
		res.setCacheable(thymeleafProperties.isCache());
		return res;
	}

	@Inject
	private BugTrackerContextHolder bugTrackerContextHolder;



	@Bean
	@Role(BeanDefinition.ROLE_SUPPORT)
	public CommonsMultipartResolver filterMultipartResolver()

	{
			MultipartResolverDispatcher bean = new MultipartResolverDispatcher();
			bean.setDefaultResolver(defaultMultipartResolver());
			HashMap<String, SquashMultipartResolver> resolverMap = new HashMap<>();
		    resolverMap.put(".*/import/upload.*", importMultipartResolver());
			resolverMap.put(".*/importer/.*", importMultipartResolver());
			bean.setResolverMap(resolverMap);
			return bean;
		}


	@Role(BeanDefinition.ROLE_SUPPORT)
	public SquashMultipartResolver defaultMultipartResolver() {
		return new SquashMultipartResolver();
	}


	@Role(BeanDefinition.ROLE_SUPPORT)
	public SquashMultipartResolver importMultipartResolver() {
		SquashMultipartResolver bean = new SquashMultipartResolver();
		bean.setMaxUploadSizeKey(ConfigurationService.Properties.IMPORT_SIZE_LIMIT);
		return bean;
	}

	@Bean
	@Order(0)
	public MultipartFilter multipartFilter() {
		return new MultipartFilter();
	}

	@Bean
	@Order(1)
	public FilterRegistrationBean bugTrackerContextPersister() {

		BugTrackerContextPersistenceFilter filter = new BugTrackerContextPersistenceFilter();
		filter.setContextHolder(bugTrackerContextHolder);
		filter.setExcludePatterns("/isSquashAlive");

		FilterRegistrationBean bean = new FilterRegistrationBean(filter);
		bean.setDispatcherTypes(DispatcherType.REQUEST);
		return bean;
	}

	@Bean
	@Order(1000)
	public FilterRegistrationBean ajaxEmptyResponseFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean(new AjaxEmptyResponseFilter());
		bean.setDispatcherTypes(DispatcherType.REQUEST);
		return bean;
	}

	@Bean @Order(500)
	public FilterRegistrationBean userConcurrentRequestLockFilter() {
		// This filter prevents a user from creating race conditions with himself. It prevents most concurrency-related
		// bugs (see #5748) but probably slows up the app.
		FilterRegistrationBean bean  = new FilterRegistrationBean(new UserConcurrentRequestLockFilter());
		bean.addInitParameter("excludePatterns", "(/|/isSquashAlive|/opened-entity)");
		bean.setDispatcherTypes(DispatcherType.REQUEST);

		return bean;
	}

	@Bean
	public HttpSessionLifecycleLogger httpSessionLifecycleLogger() {
		return new HttpSessionLifecycleLogger();
	}

	@Bean
	public OpenedEntitiesLifecycleListener openedEntitiesLifecycleListener() {
		return new OpenedEntitiesLifecycleListener();
	}

	@Bean
	public Hibernate4Module hibernate4JacksonModule() {
		Hibernate4Module bean = new Hibernate4Module();
		//Setting jackson tu eager on hibernate proxy... take care to your Mixins to avoid massive request ^^
		bean.configure(Hibernate4Module.Feature.FORCE_LAZY_LOADING, true);
		return bean;
	}
}
