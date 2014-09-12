/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.model.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

/**
 * @author Gregory Fouquet
 * 
 */
@Component
@Scope("prototype")
public class JsonTestCaseBuilder {
	public interface ListBuilder {
		List<JsonTestCase> toJson();
	}

	@Inject
	private InternationalizationHelper internationalizationHelper;
	private Locale locale;

	private List<TestCase> entities;

	public JsonTestCaseBuilder locale(@NotNull Locale locale) {
		this.locale = locale;
		return this;
	}

	public ListBuilder entities(@NotNull List<TestCase> entities) {
		this.entities = entities;
		return new ListBuilder() {

			@Override
			public List<JsonTestCase> toJson() {
				return buildList();
			}

		};
	}

	/**
	 * Simplistic implementation - we could cache projets and other referencial data.
	 * 
	 * @return
	 */
	private List<JsonTestCase> buildList() {
		if (entities.size() == 0) {
			return Collections.emptyList();
		}

		List<JsonTestCase> res = new ArrayList<JsonTestCase>(entities.size());

		for (TestCase tc : entities) {
			res.add(build(tc));
		}

		return res;
	}

	private JsonTestCase build(TestCase tc) {
		JsonTestCase res = new JsonTestCase();
		res.setId(tc.getId());
		res.setName(tc.getName());
		res.setRef(tc.getReference());
		res.setProject(JsonProject.toJson(tc.getProject()));
		res.setType(buildType(tc.getType()));

		return res;
	}

	/**
	 * @param type
	 * @return
	 */
	private JsonInternationalizableEnum<TestCaseType> buildType(TestCaseType type) {
		JsonInternationalizableEnum<TestCaseType> res = new JsonInternationalizableEnum<TestCaseType>();
		res.setValue(type);
		res.setLabel(internationalizationHelper.internationalize(type, locale));
		return res;
	}
}
