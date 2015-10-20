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

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.squashtest.tm.web.internal.filter.HtmlSanitizationFilter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SquashManagementProperties.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Inject
	private SquashManagementProperties managementProperties;
	@Inject
	private ServerProperties serverProperties;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		super.configure(auth);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.formLogin()
			.failureUrl("/login.jsp?login-error")
		.and().logout()
			.invalidateHttpSession(true).logoutSuccessUrl("/home-workspace").logoutUrl("/logout")
		.and().exceptionHandling().accessDeniedPage("/squash/accessDenied")
		.and().addFilter(htmlSanitizationFilter())
		.authorizeRequests()
			.antMatchers(
				"/administration",
				"/administration/milestones",
				"/administration/milestones/**",
				"/administration/info-lists",
				"/administration/info-lists/**",
				"/administration/projects",
				"/administration/projects/**",
				"/milestone/**"
			).access(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
			.antMatchers(
				"/admin",
				"/admin/**",
				"/administration/**",
				"/configuration",
				"/configuration/**",
				"/platform/**"
			).access(HAS_ROLE_ADMIN)
			.antMatchers("/login").permitAll()
			.antMatchers("/management/**").denyAll()
			.antMatchers("/resultUpdate/**").access("hasRole('ROLE_TA_API_CLIENT')")
			.antMatchers("/**").authenticated();
		// @formatter:on


		RequestMatcher managementRequestMatcher = new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				return request.getLocalPort() == managementProperties.getPort();
			}
		};

		// @formatter:off
		// Secured namespace for the management api
		// There is no authentication because this namespace collects system operation which cannot be done otherwise.
		// As a consequence, is supposed to be secured at the app server / host level
		http.antMatcher("/management/**")
			// all http ports remapped to management port
			.portMapper()
				.http(80).mapsTo(managementProperties.getPort())
				.http(8080).mapsTo(managementProperties.getPort())
				.http(serverProperties.getPort()).mapsTo(managementProperties.getPort())
			// all requests are secured
			.and().requiresChannel()
				.requestMatchers(managementRequestMatcher)
				.requires(managementProperties.getRequiredChannel())
			.and().authorizeRequests().anyRequest().permitAll();
		// @formatter:on
	}

	@Bean
	public HtmlSanitizationFilter htmlSanitizationFilter() {
		return new HtmlSanitizationFilter();
	}
}
