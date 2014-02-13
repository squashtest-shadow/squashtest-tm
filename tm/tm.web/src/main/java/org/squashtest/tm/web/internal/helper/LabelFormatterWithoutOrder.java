/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.web.internal.helper;

import java.util.Locale;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;

@Component
@Scope("prototype")
@Deprecated
/**
 * 
 * @deprecated looks like a strict duplicate of InternationalisableLabelFormatter
 *
 */
public class LabelFormatterWithoutOrder implements LabelFormatter<Internationalizable> {

	private final MessageSource messageSource;
	private Locale locale = Locale.getDefault();
	private boolean escapeHtml = true;

	/**
	 * @param messageSource
	 */
	@Inject
	public LabelFormatterWithoutOrder(@NotNull MessageSource messageSource) {
		super();
		this.messageSource = messageSource;
	}

	@Override
	public LabelFormatter<Internationalizable> useLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	@Override
	public String formatLabel(Internationalizable toFormat) {
		String label = messageSource.getMessage(toFormat.getI18nKey(), null, locale);
		return escapeHtml ? StringEscapeUtils.escapeHtml(label) : label;
	}

	/**
	 * @see org.squashtest.tm.web.internal.helper.LabelFormatter#escapeHtml()
	 */
	@Override
	public LabelFormatter<Internationalizable> escapeHtml() {
		escapeHtml = true;
		return this;
	}

	/**
	 * @see org.squashtest.tm.web.internal.helper.LabelFormatter#plainText()
	 */
	@Override
	public LabelFormatter<Internationalizable> plainText() {
		escapeHtml = false;
		return this;
	}
}
