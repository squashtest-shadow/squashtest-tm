package org.squashtest.csp.core.infrastructure.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.core.ParameterNameDiscoverer;

/**
 * ParameterNameDiscoverer which parameter names according to their position in the method's signature. In other words,
 * given a method's signature <code>void foo(int bar, String baz)</code>, this method's parameters will be resolved with
 * the names <code>[arg0, arg1]</code>
 * 
 * @author Gregory Fouquet
 * 
 */
public class ArgumentPositionParameterNameDiscoverer implements ParameterNameDiscoverer {

	@Override
	public String[] getParameterNames(Method method) {
		return getArgsNames(method.getParameterTypes().length);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String[] getParameterNames(Constructor ctor) {
		return getArgsNames(ctor.getParameterTypes().length);
	}

	private String[] getArgsNames(int length) {
		String[] argsNames = new String[length];

		for (int i = 0; i < argsNames.length; i++) {
			argsNames[i] = "arg" + i;
		}
		return argsNames;
	}
}
