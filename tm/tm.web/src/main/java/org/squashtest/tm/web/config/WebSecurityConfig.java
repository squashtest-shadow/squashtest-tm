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

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager;
import org.squashtest.tm.web.internal.filter.HtmlSanitizationFilter;

/**
 * This configures Spring Security
 * <p/>
 * #configure(AuthenticationManagerBuilder) should not be overriden ! When it is overriden, it supersedes any "global"
 * AuthenticationManager. This means any third party authentication provider will be ignored.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class WebSecurityConfig {
	/**
	 * Defines a global internal (dao based) authentication manager. This is the default authentication manager.
	 */
	@Configuration
	@ConditionalOnProperty(name = "authentication.provider", matchIfMissing = true, havingValue = "internal")
	@Order(0) // WebSecurityConfigurerAdapter default order is 100, we need to init this before
	public static class InternalAuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {
		@Inject
		private SquashUserDetailsManager squashUserDetailsManager;

		@Inject
		private PasswordEncoder passwordEncoder;

		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(squashUserDetailsManager).passwordEncoder(passwordEncoder);
			auth.eraseCredentials(false);
		}
	}

	@Configuration
	@Order(10)
	public static class SquashTAWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.csrf().disable()

				.antMatcher("/automated-executions/**")
					.authorizeRequests()
						.anyRequest().access("hasRole('ROLE_TA_API_CLIENT')")
				.and()
				.httpBasic();
			// @formatter:on
		}
	}

	@Configuration
	@Order(20)
	public static class StandardWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Value("${squash.security.filter.debug.enabled:false}")
		private boolean debugSecurityFilter;

		@Override
		public void configure(WebSecurity web) throws Exception {
			web.debug(debugSecurityFilter)
				.ignoring().antMatchers("/scripts/**");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				// When CSRF is on, a CSRF token is to be included in any POST/PUT/DELETE/PATCH request. This would require massive changes, so it's deactivated for now.
				.csrf().disable()

				.headers()
				.defaultsDisabled()
				// w/o cache control, some browser's cache policy is too aggressive
				.cacheControl()
				.and().frameOptions().sameOrigin()
				.and()
				//.addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))

				.authorizeRequests()
					// Administration namespace. Some of which can be accessed by PMs
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


					// Namespace reserved for other use
					.antMatchers("/management/**").denyAll()

					.anyRequest().authenticated()

				.and()
					.formLogin()
						.permitAll()
						.loginPage("/login")
						.failureUrl("/login?error")
						.defaultSuccessUrl("/home-workspace")

				.and()
					.logout()
						.permitAll()
						.invalidateHttpSession(true)
						.logoutSuccessUrl("/")

				.and()
				.addFilterAfter(new HttpPutFormContentFilter(), SecurityContextPersistenceFilter.class)
				.addFilterAfter(new HtmlSanitizationFilter(), SecurityContextPersistenceFilter.class);
			//@formatter:on
		}
	}
}

