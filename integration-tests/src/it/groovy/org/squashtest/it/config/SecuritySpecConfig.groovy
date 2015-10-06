package org.squashtest.it.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl
import org.springframework.security.acls.domain.PermissionFactory
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.squashtest.it.stub.security.StubPermissionFactory
import org.squashtest.it.stub.security.StubUserDetailsManager

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan("org.squashtest.tm.service.security.acls.domain")
class SecuritySpecConfig {
	@Bean PermissionFactory permissionFactory() {
		new StubPermissionFactory()
	}

	@Bean StubUserDetailsManager userDetailsManager() {
		new StubUserDetailsManager()
	}

}