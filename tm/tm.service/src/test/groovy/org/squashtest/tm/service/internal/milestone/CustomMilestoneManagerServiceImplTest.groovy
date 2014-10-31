package org.squashtest.tm.service.internal.milestone
import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.service.internal.repository.MilestoneDao
import spock.lang.Specification
class CustomMilestoneManagerServiceImplTest extends Specification {

	CustomMilestoneManagerServiceImpl manager = new CustomMilestoneManagerServiceImpl()
	MilestoneDao milestoneDao= Mock()
	
	def setup(){
		manager.milestoneDao = milestoneDao
	}
	
	def "should delete milestones"(){
		
		given :
		def ids = [1L, 2L, 5L]
		def milestones = ids.collect{new Milestone(id:it)}
		milestones.each{milestoneDao.findById(it.id) >> it}
		when :
		manager.removeMilestones(ids)
		then :
		milestones.each{1 * milestoneDao.remove(it)}
		
	}
}
