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

package org.squashtest.csp.tm.web.internal.report.criteria;

import static org.junit.Assert.*;

import org.spockframework.compiler.model.Spec;
import org.squashtest.plugin.api.report.form.InputType;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class FormToCriteriaConverterTest extends Specification {
	FormToCriteriaConverter converter = new FormToCriteriaConverter()
	
	def "should build string criteria"() {
		when:
		Map criteria = converter.convert([batman: [value: "leatherpants", type: "TEXT"]])
		
		then:
		criteria.batman.name == "batman"
		criteria.batman.value == "leatherpants"
		criteria.batman.sourceInput == InputType.TEXT
	} 
	def "should build checkbox criteria"() {
		when:
		Map criteria = converter.convert([cbx: [value: "", selected: "true",  type: "CHECKBOX"]])
		
		then:
		criteria.cbx.name == "cbx"
		criteria.cbx.value == true
		criteria.cbx.sourceInput == InputType.CHECKBOX
	} 
	def "should build radio criteria"() {
		when:
		Map criteria = converter.convert([outfit: [[value: "spandex tights", type: "RADIO_BUTTONS_GROUP", selected: "true"], [value: "leatherpants", type: "RADIO_BUTTONS_GROUP", selected: false]]])
		
		then:
		criteria.outfit.name == "outfit"
		criteria.outfit.value == "spandex tights"
		criteria.outfit.sourceInput == InputType.RADIO_BUTTONS_GROUP
	} 
	def "should build dropdown criteria"() {
		when:
		Map criteria = converter.convert([outfit: [[value: "spandex tights", type: "DROPDOWN_LIST", selected: "true"], [value: "leatherpants", type: "DROPDOWN_LIST", selected: false]]])
		
		then:
		criteria.outfit.name == "outfit"
		criteria.outfit.value == "spandex tights"
		criteria.outfit.sourceInput == InputType.DROPDOWN_LIST
	} 
	def "should build checkboxes group criteria"() {
		when:
		Map criteria = converter.convert([equipment: [
			[value: "batarang", type: "CHECKBOXES_GROUP", selected: "true"], 
			[value: "webshooters", type: "CHECKBOXES_GROUP", selected: "false"], 
			[value: "utility-belt", type: "CHECKBOXES_GROUP", selected: "true"], 
		]])
		
		then:
		criteria.equipment.name == "equipment"
		criteria.equipment.value == ["batarang", "utility-belt"]
		
		criteria.equipment.sourceInput == InputType.CHECKBOXES_GROUP
		criteria.equipment.isSelected("batarang") == true
		criteria.equipment.isSelected("webshooters") == false
		criteria.equipment.isSelected("utility-belt") == true
	} 
}
