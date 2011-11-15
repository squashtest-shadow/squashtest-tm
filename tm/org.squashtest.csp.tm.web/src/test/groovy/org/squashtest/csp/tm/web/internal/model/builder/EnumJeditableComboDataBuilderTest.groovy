/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.web.internal.model.builder;

import static org.squashtest.csp.tm.web.internal.model.builder.DummyEnum.*

import java.util.Locale;

import org.apache.tools.ant.taskdefs.Javac.ImplementationSpecificArgument;
import org.squashtest.csp.tm.web.internal.helper.LabelFormatter;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class EnumJeditableComboDataBuilderTest extends Specification {
	EnumJeditableComboDataBuilder builder = new EnumJeditableComboDataBuilder();
	Locale locale = Locale.default
	
	def setup() {
		builder.model = DummyEnum.values()
		builder.labelFormatter = new DummyLabelFormatter()
		builder.modelComparator = new DummyEnumComparator()
	}
	
	def "should build ordered map using given comparator"() {
		when:
		def res = builder
			.useLocale(locale)
			.buildMap()

		then:
		res == ["ONE": "un", "TWO" : "deux"]
	}
	
	def "should build unordered map"() {
		given:
		builder.modelComparator = null

		when:
		def res = builder
			.useLocale(locale)
			.buildMap()

		then:
		res == ["TWO": "deux", "ONE" : "un"]
	}
	
	def "should build map containing the selected item"() {
		given:
		builder.modelComparator = null

		when:
		def res = builder
			.useLocale(locale)
			.selectItem(TWO)
			.buildMap()

		then:
		res == ["ONE": "un", "TWO" : "deux", "selected": "TWO"]
	}
	
	def "should build json using given comparator"() {
		when:
		def res = builder
			.useLocale(locale)
			.buildMarshalled()

		then:
		res == '{"ONE":"un","TWO":"deux"}';
	}
	
	def "labels should be formatted using the given locale"() {
		given:
		builder.modelComparator = null
		
		and: 
		LabelFormatter formatter = Mock()
		builder.labelFormatter = formatter

		when:
		def res = builder
			.useLocale(locale)
			.selectItem(TWO)
			.buildMarshalled()

		then:
		1 * formatter.useLocale(locale)
		1 * formatter.formatLabel(TWO)
		1 * formatter.formatLabel(ONE)
	}


}

private enum DummyEnum {
	TWO(2),
	ONE(1);

	final int order;

	DummyEnum(int order) {
		this.order = order
	}
}

private class DummyEnumComparator implements Comparator {
	public int compare(Object a,  Object b) {
		return a.order.compareTo(b.order)
	}
}

private class DummyLabelFormatter implements LabelFormatter {

	@Override
	public LabelFormatter useLocale(Locale locale) {
		// NOOP
		return this;
	}

	@Override
	public String formatLabel(Object toFormat) {
		switch (toFormat) {
			case ONE : return "un"
			case TWO : return "deux"
			default : return "default"
		}		
	}
	
} 