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
package test.hibernate.softdelete.jointablefilter




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
		config.configure("softdelete-association/softdelete-hibernate.cfg.xml")
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
	
	def "should reindex children upon association deletion"() {
		given:
		Parent p = new Parent()
		p.children << new Child(name: "foo")
		p.children << new Child(name: "bar")	
		p.children << new Child(name: "baz")
		
		doInTransaction { it.persist p }
		
		when:
		doInTransaction { 
			def pp = it.get(Parent, p.id)
			pp.children.remove(1)
		}
		
		doInTransaction {
			def pp = it.get(Parent, p.id)
			def c = new Child(name: "gurdyroot")
			it.persist c
			pp.children << c
		}
		
		def res = doInTransaction { 
			def pp = it.get(Parent, p.id); 
			for (Child c : pp.getChildren()) {
			}
			return pp.getChildren();
		}
		
		then:
		res[0].name == "foo"
		res[1].name == "baz"
		res[2].name == "gurdyroot"
		res.size() == 3
	}
	
	def "unfiltered lists of deleted elements should not be trusted !"() {
		given:
		Parent p = new Parent()
		p.children << new Child(name: "snorkack")
		
		doInTransaction { it.persist p }
		
		when:
		doInTransaction {
			def pp = it.get(Parent, p.id)
			pp.children.remove(0)
		}
		
		doInTransaction {
			def pp = it.get(Parent, p.id)
			def c = new Child(name: "gurdyroot")
			it.persist c
			pp.children << c
		}
		
		def res = doInTransaction {
			it.disableFilter "deletedAssociation"
			def pp = it.get(Parent, p.id);
			for (Child c : pp.getChildren()) {
			}
			
			Session s = it
			println (s.createSQLQuery("select parent_id, child_id, ord, convert(deleted, char) from parent_child").list())
			return pp.getChildren();
		}
		
		then:
		res.size() == 1
		// might return "snorkack" instead of "gurdyroot" !
	}
	
	
	def doInTransaction(def closure) {
		Session s = sf.openSession()
		s.enableFilter "deletedAssociation"
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
