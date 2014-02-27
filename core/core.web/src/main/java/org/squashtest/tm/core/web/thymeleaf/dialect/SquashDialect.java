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

package org.squashtest.tm.core.web.thymeleaf.dialect;

import java.util.HashSet;
import java.util.Set;

import org.squashtest.tm.core.web.thymeleaf.processor.attr.SquashCssAttrProcessor;
import org.squashtest.tm.core.web.thymeleaf.processor.attr.SquashTogglePanelAttrProcessor;
import org.squashtest.tm.core.web.thymeleaf.processor.attr.SquashUnsafeHtmlAttrProcessor;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * Squash dialect for Thmymeleaf
 *
 * @author Gregory Fouquet
 *
 */
public class SquashDialect extends AbstractDialect {

	/**
	 * @see org.thymeleaf.dialect.IDialect#getPrefix()
	 */
	@Override
	public String getPrefix() {
		return "sq";
	}

	/**
	 * @see org.thymeleaf.dialect.IDialect#isLenient()
	 */
	@Override
	public boolean isLenient() {
		return false;
	}

	/**
	 * @see org.thymeleaf.dialect.IDialect#getProcessors()
	 */
	@Override
	public Set<IProcessor> getProcessors() {
		Set<IProcessor> processors = new HashSet<IProcessor>(3);
		processors.add(new SquashUnsafeHtmlAttrProcessor());
		processors.add(new SquashCssAttrProcessor());
		processors.add(new SquashTogglePanelAttrProcessor());
		return processors;
	}


}
