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
package test.hibernate.softdelete.jointablewhere



import java.util.ResourceBundle;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import spock.lang.Specification;

class SoftDeleteAssociationTest extends Specification {
	Configuration config = new Configuration()
	SessionFactory sf
	
	def setup() {
		config.configure("softdelete/softdelete-hibernate.cfg.xml")
		config.addAnnotatedClass Parent
		config.addAnnotatedClass Child
		
		sf = config.buildSessionFactory()
	}
	
	def "should soft delete association when child removed from parent"() {
		given:
		Parent p = aParentWithThreeChildren()
		
		and:
		Child lastChild = p.children[2]
		doInTransaction { it.get(Parent, p.id).children.remove(2) }
		
		
		when:
		def deletedAssociations = doInTransaction { Session s ->
			s.createSQLQuery("select count(*) from parent_child where child_id = ${lastChild.id} and parent_id = ${p.id} and deleted = 'true'").uniqueResult()
		}
		
		then:
		deletedAssociations == 1
	}
	
	def aParentWithThreeChildren() {
		Parent p = new Parent()
		p.children << new Child()
		p.children << new Child()
		p.children << new Child()
		
		doInTransaction { it.persist p }
		
		return p
	}
	
	def "should not join fetch deleted associations"() {
		given:
		Parent p = aParentWithThreeChildren()
		
		and:
		doInTransaction { it.get(Parent, p.id).children.remove(2) }
		
		when:
		def res = doInTransaction { 
			it.createQuery("from Parent p join fetch p.children where p.id = " + p.id).uniqueResult()
		}
		
		then:
		res.children.size() == 2
	}
	
	def "should not navigate deleted associations"() {
		given:
		Parent p = aParentWithThreeChildren()
		
		and:
		doInTransaction { it.get(Parent, p.id).children.remove(2) }
		
		when:
		def res = doInTransaction { 
			def pp = it.get(Parent, p.id)
			Hibernate.initialize(pp.children)
			return pp
		}
		
		then:
		res.children.size() == 2
	}
	
	def doInTransaction(def closure) {
		Session s = sf.openSession()
		Transaction tx = s.beginTransaction()
		def res = null;
		
		try {
			res = closure(s)
			
			s.flush()
			tx.commit()
		} finally {
			s?.close()
		}
		
		return res
	}
	
	def cleanup() {
		sf.close()
	}
}
