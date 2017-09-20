package org.squashtest.tm.service.workspace

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.dto.UserDto
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class WorkspaceHelperServiceIT extends DbunitServiceSpecification {

	@Inject
	private WorkspaceHelperService workspaceHelperService


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find projects filter models"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def filterModel = workspaceHelperService.doFindFilterModel(user, [-1L, -2L, -3L])

		then:
		filterModel.id == -1
		filterModel.enabled
		filterModel.projectData.length == 3

		Object[] pData = filterModel.projectData[0]
		pData[0] == -3L
		pData[1] == "baz"
		pData[2] == false
		pData[3] == null

		Object[] pData1 = filterModel.projectData[1]
		pData1[0] == -2L
		pData1[1] == "bar"
		pData1[2] == true
		pData1[3] == null

		Object[] pData2 = filterModel.projectData[2]
		pData2[0] == -1L
		pData2[1] == "foo"
		pData2[2] == true
		pData2[3] == "foo label"

	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find default filter modef for user without filter"() {
		given:
		UserDto user = new UserDto("bob", -1L, [-100L, -200L, -300L], false)

		when:
		def filterModel = workspaceHelperService.doFindFilterModel(user, [-1L, -2L, -3L])

		then:
		filterModel.id == null
		!filterModel.enabled
		filterModel.projectData.length == 3

		Object[] pData = filterModel.projectData[0]
		pData[0] == -3L
		pData[1] == "baz"
		pData[2] == true
		pData[3] == null

		Object[] pData1 = filterModel.projectData[1]
		pData1[0] == -2L
		pData1[1] == "bar"
		pData1[2] == true
		pData1[3] == null

		Object[] pData2 = filterModel.projectData[2]
		pData2[0] == -1L
		pData2[1] == "foo"
		pData2[2] == true
		pData2[3] == "foo label"

	}

}
