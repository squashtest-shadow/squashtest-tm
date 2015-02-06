package org.squashtest.tm.service.internal.repository.hibernate;

import static org.junit.Assert.*;

import javax.inject.Inject;
import org.squashtest.tm.service.internal.repository.ClientDao;
import org.unitils.dbunit.annotation.DataSet;
import spock.unitils.UnitilsSupport;

@UnitilsSupport
class HibernateClientDaoIT extends DbunitDaoSpecification {

	@Inject
	ClientDao clientDao;

	@DataSet("HibernateClientDaoIT.should return list of clients.xml")
	def "should return list of clients"(){
		when:
		def result =  clientDao.findClientsOrderedByName()

		then:
		result.size() == 3
		result.each {client.clientSecret == "secret"}
	}
}
