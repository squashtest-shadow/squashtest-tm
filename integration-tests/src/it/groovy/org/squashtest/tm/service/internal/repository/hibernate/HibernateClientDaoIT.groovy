/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import static org.junit.Assert.*;

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.oauth2.Client;
import org.squashtest.tm.service.internal.repository.ClientDao;
import org.unitils.dbunit.annotation.DataSet;
import spock.unitils.UnitilsSupport;


@UnitilsSupport
class HibernateClientDaoIT extends DbunitDaoSpecification {

	@Inject
	ClientDao clientDao;

	def "should create a client"(){
		given:
		Client client = new Client("client1", "secret");

		when:
		clientDao.persist(client);


		then:
		def result = clientDao.findClientByName("client1");
		result.clientId == "client1";
		result.clientSecret == "secret";
	}

	@DataSet("HibernateClientDaoIT.should return list of clients.xml")
	def "should return list of clients"(){
		when:
		def result =  clientDao.findClientsOrderedByName()

		then:
		result.size() == 3
	}

	@DataSet("HibernateClientDaoIT.should remove clients.xml")
	def "should remove a client"(){
		when:
		def client = clientDao.findClientByName("client1");
		clientDao.remove(client);

		then:
		def result = clientDao.findClientsOrderedByName()
		result.size() == 2
	}
}
