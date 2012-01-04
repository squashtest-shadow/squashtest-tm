package org.squashtest.csp.tm.web.internal.fileuploadutils;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


/**
 * <p>Will redirect a request to a specific MultipartResolver, with specific settings, with respect to the matched URL. This chain is
 * completely dumb and will pick the first match it finds, or the default if none was found.</p>
 * 
 * @author bsiri
 *
 */
public class MultipartResolverDispatcher extends CommonsMultipartResolver {
	
	private CommonsMultipartResolver defaultResolver;
	
	private Map<String, CommonsMultipartResolver> resolverMap;
	
	public void setResolverMap(Map<String, CommonsMultipartResolver> chain){
		this.resolverMap = chain;
	}
	
	public void setDefaultResolver(CommonsMultipartResolver defaultResolver){
		this.defaultResolver = defaultResolver;
	}
	
	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		
		String target = request.getRequestURI();
		
		for (String matcher : resolverMap.keySet()){
			if (target.matches(matcher)){
				return resolverMap.get(matcher).resolveMultipart(request);
			}
		}
		
		//else
		return defaultResolver.resolveMultipart(request);
	}
	
	
}
