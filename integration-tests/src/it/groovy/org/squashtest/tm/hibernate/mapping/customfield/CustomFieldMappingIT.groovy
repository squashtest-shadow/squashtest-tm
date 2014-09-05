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
package org.squashtest.tm.hibernate.mapping.customfield
 
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.csp.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldOption
import org.squashtest.tm.domain.customfield.InputType
import org.squashtest.tm.domain.customfield.SingleSelectField

/**
  * @author Gregory Fouquet
  */
class CustomFieldMappingIT extends HibernateMappingSpecification {
    def "should persist and retrieve a custom field"() {
        given:
        def cf = new CustomField();
        cf.name = "batman"
		cf.code="code1"
        cf.inputType = InputType.PLAIN_TEXT

        when:
        persistFixture cf
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession { it.get(CustomField, cf.id) }
        }

        then:
        res != null
    }

    def "should persist and retrieve a single select field"() {
        given:
        def cf = new SingleSelectField();
        cf.name = "batman"

        when:
        persistFixture cf
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession { it.get(CustomField, cf.id) }
        }

        then:
        res != null
    }

    def "should add options to a single select field"() {
        given:
        def cf = new SingleSelectField();
        cf.name = "batman"
		cf.code="code1"
        cf.addOption(new CustomFieldOption("leatherpants", "code2"))
        cf.addOption(new CustomFieldOption("batarang", "code3"))
        persistFixture cf

        when:
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession {
                def r = it.get(CustomField, cf.id)
                r.options.each { it.label }
                return r
            }
        }

        then:
        res.options*.label == ["leatherpants", "batarang"]
    }

    def "should remove options from a single select field"() {
        given:
        def cf = new SingleSelectField();
        cf.name = "batman"
		cf.code="code1"
        cf.addOption(new CustomFieldOption("leatherpants", "code2"))
        cf.addOption(new CustomFieldOption("batarang", "code3"))
        persistFixture cf

        when:
		def removeOption = {
            def res = it.get(CustomField, cf.id)
			res.removeOption("batarang")
		}
		
		def loadFixture = {
            def res = it.get(CustomField, cf.id)
            res.options.each { it.label }
            return res
		}
		
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession removeOption
            sessionFactory.doInSession(loadFixture)
       }

		then:
		res.options*.label == ["leatherpants"]
    }
	
	def "should change the label of a single select field's option"() {
		given:
		def cf = new SingleSelectField()
		cf.name="batman"
		cf.code = "code1"
        cf.addOption(new CustomFieldOption("leatherpants", "code2"))
        cf.addOption(new CustomFieldOption("batarang", "code3"))
		persistFixture cf

		when:
		def changeOptionLabel = {
			def r = it.get(CustomField, cf.id)
			r.options[1].label = "bataring"
		}
		
		def loadFixture = {
            def res = it.get(CustomField, cf.id)
            res.options.each { it.label }
            return res
		}

		def res = use (HibernateOperationCategory) {
			sessionFactory.doInSession changeOptionLabel
			sessionFactory.doInSession loadFixture
		}

		then:
		res.options*.label == ["leatherpants", "bataring"]
	}
	
}

