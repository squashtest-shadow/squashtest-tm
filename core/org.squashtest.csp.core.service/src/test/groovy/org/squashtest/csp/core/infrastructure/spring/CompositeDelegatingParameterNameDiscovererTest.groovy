package org.squashtest.csp.core.infrastructure.spring

import java.lang.reflect.Method;

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.mock.web.portlet.MockActionRequest;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
@RunWith(Sputnik)
class CompositeDelegatingParameterNameDiscovererTest extends Specification {
	ParameterNameDiscoverer firstDiscoverer = Mock()
	ParameterNameDiscoverer secondDiscoverer = Mock()
	CompositeDelegatingParameterNameDiscoverer discoverer

	def setup() {
		discoverer = new CompositeDelegatingParameterNameDiscoverer([firstDiscoverer, secondDiscoverer])
	}

	def "should resolve method param names using first discoverer"() {
		given:
		firstDiscoverer.getParameterNames(_) >> ["foo"]
		
		and:
		Method method = String.getMethod("substring", int.class)

		when:
		String[] names = discoverer.getParameterNames(method)

		then:
		names == ["foo"]
		
	}
	def "should resolve method param names using second discoverer"() {
		given:
		secondDiscoverer.getParameterNames(_) >> ["bar"]
		
		and:
		Method method = String.getMethod("substring", int.class)

		when:
		String[] names = discoverer.getParameterNames(method)

		then:
		names == ["bar"]
		
	}
	def "should not resolve method param names"() {
		given:
		Method method = String.getMethod("substring", int.class)

		when:
		String[] names = discoverer.getParameterNames(method)

		then:
		names == null
		
	}
}
