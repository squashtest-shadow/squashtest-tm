/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.domain

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.bridge.LuceneOptions;
import org.squashtest.tm.service.internal.repository.hibernate.DbunitDaoSpecification;

/**
 * Superclass for a hibernate search bridge specification.
 * 
 * 
 * @author Gregory Fouquet
 *
 */
abstract class BridgeSpecification extends DbunitDaoSpecification {
	@Inject SessionFactory sessionFactory
	LuceneOptions lucene = Mock()
	Document doc = new Document()

	Session getSession() {
		sessionFactory.currentSession
	}	
	
	def setup() {
		lucene.getStore() >> Mock(Store)
		lucene.getIndex() >> Mock(Index)
		lucene.getTermVector() >> Mock(TermVector)

	}
	
}
