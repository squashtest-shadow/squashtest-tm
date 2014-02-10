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
package org.squashtest.tm.web.internal.controller.search;

import javax.inject.Inject;
import javax.inject.Provider;

import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.web.internal.helper.InternationalisableLabelFormatter;
import org.squashtest.tm.web.internal.helper.InternationalizableComparator;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Common code factored out of subclasses
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class SearchInterfaceDescription {
	protected static final String TEXTFIELD = "textfield";
	protected static final String TEXTAREA = "textarea";
	protected static final String RANGE = "range";
	protected static final String EXISTS = "exists";
	protected static final String DATE = "date";
	protected static final String MULTISELECT = "multiselect";
	protected static final String MULTIAUTOCOMPLETE = "multiautocomplete";
	protected static final String RADIOBUTTON = "radiobutton";
	protected static final String ATLEASTONE = "1";
	protected static final String NONE = "0";
	protected static final String EMPTY = "";

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatter;
	@Inject
	private Provider<InternationalisableLabelFormatter> internationalizableLabelFormatter;

	

	@Inject
	private ProjectFilterModificationService projectFilterService;

	/**
	 * 
	 */
	public SearchInterfaceDescription() {
		super();
	}

	protected final <T extends Enum<?> & Level> EnumJeditableComboDataBuilder<T> levelComboBuilder(T[] values) {
		EnumJeditableComboDataBuilder<T> builder = new EnumJeditableComboDataBuilder<T>();
		builder.setLabelFormatter(levelLabelFormatter.get().plainText());
		builder.setModel(values);
		builder.setModelComparator(LevelComparator.getInstance());
		return builder;
	}

	protected final <T extends Enum<?> & Level> EnumJeditableComboDataBuilder<T> internationalizableComboBuilder(
			T[] values) {
		EnumJeditableComboDataBuilder<T> builder = new EnumJeditableComboDataBuilder<T>();
		builder.setLabelFormatter(internationalizableLabelFormatter.get().plainText());
		builder.setModel(values);
		builder.setModelComparator(new InternationalizableComparator(messageSource));
		return builder;
	}

	/**
	 * @return the messageSource
	 */
	protected final InternationalizationHelper getMessageSource() {
		return messageSource;
	}


	/**
	 * @return the projectFilterService
	 */
	protected final ProjectFilterModificationService getProjectFilterService() {
		return projectFilterService;
	}

}