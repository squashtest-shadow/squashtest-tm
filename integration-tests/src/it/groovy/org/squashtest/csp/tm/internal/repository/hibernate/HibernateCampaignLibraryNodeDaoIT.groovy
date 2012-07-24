package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Specification
import spock.unitils.UnitilsSupport;


@UnitilsSupport
class HibernateCampaignLibraryNodeDaoIT extends DbunitDaoSpecification {

	
	@Inject
	HibernateCampaignLibraryNodeDao dao;
	
	@DataSet("HibernateCampaignDaoIt.small hierarchy.xml")
	def "should return the list of the parents names"(){

		when :
			def res = dao.getParentsName(30l)
			
		then :
			res == ["elder", "grandpa", "pa", "son"]
		
	}
	
	
}
