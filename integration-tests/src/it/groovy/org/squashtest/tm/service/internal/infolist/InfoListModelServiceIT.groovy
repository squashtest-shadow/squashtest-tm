package org.squashtest.tm.service.internal.infolist

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.infolist.InfoListModelService
import org.squashtest.tm.service.internal.dto.json.JsonInfoList
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
@DataSet("InfoListModelService.sandbox.xml")
class InfoListModelServiceIT extends DbunitServiceSpecification {

	@Inject
	InfoListModelServiceImpl infoListModelService


	def "should find infolist ids"() {

		when:
		def ids = infoListModelService.findUsedInfoListIds(readableProjectIds)

		then:
		ids.sort() == expectdInfolistIds.sort()

		where:
		readableProjectIds   || expectdInfolistIds
		[]                   || []
		[-1L]                || [-4L, -2L, -1L]
		[-1L, -2L]           || [-4L, -3L, -2L, -1L]
		[-1L, -2L, -3L, -4L] || [-4L, -3L, -2L, -1L]
	}


	def "should find infolist json models"() {
		when:
		Map<Long, JsonInfoList> infolistMap = infoListModelService.findInfoListMap([-1L, -2L, -3L, -4L] as Set);

		then:
		infolistMap.size() == 4
		JsonInfoList infoList = infolistMap.get(-1L)
		infoList.getId() == -1L
		infoList.getCode() == "DEF_REQ_CAT"
		infoList.getLabel() == "Req Cat"
		infoList.getDescription() == "Desc"

		def infoListItems = infoList.getItems()
		infoListItems.size() == 3
		infoListItems.collect { it.id }.sort() == [-1L, -2L, -3L].sort()

		JsonInfoList customInfoList = infolistMap.get(-4L)
		customInfoList.getItems().size() == 4
		customInfoList.getItems().collect { it.id }.sort() == [-13L, -12L, -11L, -10L].sort()

	}

}
