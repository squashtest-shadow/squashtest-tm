package org.squashtest.csp.core.internal.security.web.authentication;

import org.springframework.dao.DataAccessException;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

/**
 * <p>
 * 	This class will be injected in a bean a declared in bundle context A, and serve as a proxy for a bean b declared in
 * bundle context b.  
 * </p>
 * 
 * <p> Let's be explicit for the sake of clarity : </p>
 * <ul>
 * 		<li>bundle context A = core.web</li>
 * 		<li>bundle context B = core.service</li>
 * 		<li>a = authentication-provider, which itself belongs to the authentication-manager, which belongs to the security chain.</li>
 * 		<li>b = the service that allows the authentication-provider to search the database.</li>
 * </ul>
 * 
 * 
 * @author bsiri
 *
 */

public class OSGIProxyUserDetailsService implements UserDetailsService {

	
	private JdbcUserDetailsManager delegate;
	private AuthenticationManager manager;
	
	
	//the two following methods are coupled somehow. The operation is ugly but the logic is simple.
	public void setAuthenticationManager(AuthenticationManager manager){
		this.manager=manager;
		if (delegate != null){
			delegate.setAuthenticationManager(manager);
		}
	}
	
	@ServiceReference
	public void setJdbcUserDetailsManager(JdbcUserDetailsManager service){
		this.delegate=service;
		if (manager!=null){
			delegate.setAuthenticationManager(manager); 
		}
	}
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		return delegate.loadUserByUsername(username);
	}

}
