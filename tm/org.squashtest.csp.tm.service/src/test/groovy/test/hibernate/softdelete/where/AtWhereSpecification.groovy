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
package test.hibernate.softdelete.where

import java.util.ResourceBundle;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import spock.lang.Specification;

class AtWhereSpecification extends Specification {
	Configuration config = new Configuration()
	SessionFactory sf
	
	def setup() {
		config.configure("softdelete/softdelete-hibernate.cfg.xml")
		config.addAnnotatedClass Parent
		config.addAnnotatedClass Child
		
		sf = config.buildSessionFactory()
	}
	
	def "should not navigate down to deleted children"() {
		given:
		Parent p = new Parent()
		p.children << new Child()
		p.children << new Child(deleted: true)	
		p.children << new Child()
		
		doInTransaction { it.persist p }
		
		when:
		def res = doInTransaction { 
			def pp = it.get(Parent, p.id); 
			for (Child c : pp.getChildren()) {
			}
			return pp.getChildren();
		}
		
		then:
		res.size() == 2
	}
	
	def "should not join fetch deleted children"() {
		given:
		Parent p = new Parent()
		p.children << new Child()
		p.children << new Child(deleted: true)	
		p.children << new Child()
		
		doInTransaction { it.persist p }
		
		when:
		def res = doInTransaction { 
			it.createQuery("from Parent p join fetch p.children where p.id = " + p.id).uniqueResult()
		}
		
		then:
		res.children.size() == 2
	}
	
	def "should fail to join fetch deleted children"() {
		given:
		Parent p = new Parent()
		p.children << new Child()
		p.children << new Child(deleted: true)	
		p.children << new Child()
		
		doInTransaction { it.persist p }
		
		when: "we try to fetch parent and deleted children"
		def res = doInTransaction { 
			def q = it.createQuery("from Parent p join fetch p.children c where c.deleted = true and p.id = " + p.id).uniqueResult()
		}
		
		then: "we wont get any result"
		res == null
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
