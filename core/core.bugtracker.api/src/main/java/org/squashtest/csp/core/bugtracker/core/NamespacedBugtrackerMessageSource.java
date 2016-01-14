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
package org.squashtest.csp.core.bugtracker.core;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class NamespacedBugtrackerMessageSource implements MessageSource {

	private MessageSource source;

	private String namespace;

	public NamespacedBugtrackerMessageSource(MessageSource source, String namespace) {
		this.source = source;
		this.namespace = namespace;
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return source.getMessage(namespace + code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return source.getMessage(namespace + code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {

		String[] codes = resolvable.getCodes();

		for (int i = 0; i < codes.length; i++) {
			codes[i] = namespace + codes[i];
		}

		MessageSourceResolvable namespacedResolvable = new DefaultMessageSourceResolvable(codes,
				resolvable.getArguments(), resolvable.getDefaultMessage());

		return source.getMessage(namespacedResolvable, locale);
	}

}
