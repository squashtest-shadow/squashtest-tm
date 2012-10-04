/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.csp.tm.domain.customfield.CustomField
import org.squashtest.csp.tools.unittest.hibernate.HibernateOperationCategory
import org.squashtest.csp.tm.domain.campaign.Iteration
import org.squashtest.csp.tm.domain.customfield.InputType
import org.squashtest.csp.tm.domain.customfield.SingleSelectField
import org.hibernate.Hibernate

/**
  * @author Gregory Fouquet
  */
class CustomFieldMappingIT extends HibernateMappingSpecification {
    def "should persist and retrieve a custom field"() {
        given:
        def cf = new CustomField();
        cf.name = "batman"
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
        cf.inputType = InputType.DROPDOWN_LIST

        when:
        persistFixture cf
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession { it.get(CustomField, cf.id) }
        }

        then:
        res != null
    }

    def "should add option to a single select field"() {
        given:
        def cf = new SingleSelectField();
        cf.name = "batman"
        cf.inputType = InputType.DROPDOWN_LIST
        cf.addOption("leatherpants")
        persistFixture cf

        when:
        def res = use (HibernateOperationCategory) {
            sessionFactory.doInSession {
                it.get(CustomField, cf.id)
                Hibernate.initialize(cf)
                return cf
            }
        }

        then:
        res.options*.label == ["leatherpants"]
    }
}

