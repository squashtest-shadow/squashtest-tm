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

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager;
import org.squashtest.tm.web.internal.filter.HtmlSanitizationFilter;

/**
 * This configures Spring Security
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableConfigurationProperties(SquashManagementProperties.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Inject private Environment environment;

	@Inject
	private SquashManagementProperties managementProperties;

	@Inject
	private ServerProperties serverProperties;

	@Inject
	private SquashUserDetailsManager squashUserDetailsManager;

	@Inject
	private PasswordEncoder passwordEncoder;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(squashUserDetailsManager).passwordEncoder(passwordEncoder);
		// don't call super otherwise spring-sec will totally ignore what we just did !
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		boolean debug = environment.acceptsProfiles("dev", "debug");
		web.debug(debug);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http// When CSRF is on, a CSRF token is to be included in any POST/PUT/DELETE/PATCH request. This would require
			// massive changes, so it's deactivated for now.
		.csrf().disable()

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
			.antMatchers("/accessDenied").permitAll()
			.antMatchers("/management/**").denyAll()
			.antMatchers("/resultUpdate/**").access("hasRole('ROLE_TA_API_CLIENT')")
			.anyRequest().authenticated()

		.and().formLogin()
			.permitAll()
			.loginPage("/login")
			.failureUrl("/login?error")
			.defaultSuccessUrl("/home-workspace")

		.and().logout()
			.permitAll()
			.invalidateHttpSession(true)
			.logoutSuccessUrl("/")

//		.and().exceptionHandling()
//			.accessDeniedPage("/squash/accessDenied")
		.and()
			.addFilterAfter(new HttpPutFormContentFilter(), SecurityContextPersistenceFilter.class)
			.addFilterAfter(new HtmlSanitizationFilter(), SecurityContextPersistenceFilter.class)
			.headers()
		    .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN));
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
//		http.antMatcher("/management/**")
//			// all http ports remapped to management port
//			.portMapper()
//				.http(80).mapsTo(managementProperties.getPort())
//				.http(8080).mapsTo(managementProperties.getPort())
//				.http(serverProperties.getPort() == null ? 8080 : serverProperties.getPort()).mapsTo(managementProperties.getPort())
//			// all requests are secured
//			.and().requiresChannel()
//				.requestMatchers(managementRequestMatcher)
//				.requires(managementProperties.getRequiredChannel())
//			.and().authorizeRequests().anyRequest().permitAll();
		// @formatter:on
	}
}
