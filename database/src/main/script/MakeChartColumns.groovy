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

SUFFIX = 0
DTYPE = 1
ATTR = 2
ROLES = 3
QUEREF = 4


/*
 * Structure :
 *
 * definition = [
 *
 * 		<entity1> : [
 * 			columns : [
 * 				<attribute column ref> : ['labelsuffix', 'datatype', 'attribute', "list, of, roles"], -- 4 args
 * 				<calculate column ref> : ['labelsuffix', 'datatype', 'attribute', "list, of, roles", "subquery reference"] -- 5 args
 * 			],
 * 			subqueries : [
 * 				<subquery reference 1> : [
 * 					strategy : SUBQUERY | INLINED (default SUBQUERY)
 * 					joinStyle : LEFT_JOIN | INNER_JOIN (default INNER_JOIN)
 * 					measures : ['column ref [optional operation]'],
 * 					axes : ['column ref [optional operation']]
 * 				]
 * 			]
 * 		]
 *
 * ]
 * 
 * About "list, of, roles" :
 * 
 * accepts either "none", "all" or explicitly "[measure, ][axis, ][filter]" in any order
 * 
 * 
 *
 *
 */

def definition = [

	REQUIREMENT : [

		columns : [
			reqId : ['ID', 'NUMERIC', 'id',  "all" ],
			reqVCount : ['NB_VERSIONS', 'count(requirementVersionCoverages)', 'NUMERIC', 'all', 'reqVCountSubquery']
		],

		subqueries : [
			reqVCountSubquery : [
				label : 'REQUIREMENT_NB_VERSIONS_SUBQUERY',
				measures : ['rvId COUNT'],
				axes : ['reqId']
			]
		]
	],

	REQUIREMENT_VERSION : [
		
		columns : [
			rvCreatOn : ['CREATED_ON', 'DATE','audit.createdOn',  'axis, filter'],
			rvModOn : ['MODIFIED_ON',  'DATE', 'audit.lastModifiedOn','axis, filter'],
			rvId : ['ID', 'NUMERIC', 'id', 'all' ],
			rvVersnum : ['VERS_NUM', 'NUMERIC', 'versionNumber', 'filter, measure'],
			rvCreatBy : ['CREATED_BY',  'STRING','audit.createdBy', 'axis, filter'],
			rvModBy : ['MODIFIED_BY', 'STRING','audit.lastModifiedBy','axis, filter'],
			rvLabel : ['LABEL', 'STRING', 'name', 'filter, measure'],
			rvCat : ['CATEGORY', 'INFO_LIST_ITEM', 'category.label', 'all'],
			rvCrit : ['CRITICALITY', 'LEVEL_ENUM', 'criticality', 'all'],
			rvStatus : ['STATUS', 'LEVEL_ENUM', 'status', 'all'],
			rvVerifTcCount : ['TCCOUNT', 'NUMERIC', 'count(requirementVersionCoverages)', 'all', 'rvVerifTCCountSubquery'],
			rvMilesCount : ['MILCOUNT', 'NUMERIC', 'count(milestones)', 'all', 'rvMilesCountSubquery'],
		],
	
		subqueries : [
			rvVerifTCCountSubquery : [
				label : 'REQUIREMENT_VERSION_TCCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				measures : ['tcId COUNT'],
				axes : ['rvId']
			],
			rvMilesCountSubquery : [
				label : 'REQUIREMENT_VERSION_MILCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'INLINED',
				measures : ['rvmilId COUNT'],
				axes : ['rvId']
			]
		
		]	
		
	],

	TEST_CASE : [
		columns : [
			tcCreatOn : ['CREATED_ON', 'DATE', 'audit.createdOn', 'axis, filter'],
			tcModOn : ['MODIFIED_ON', 'DATE', 'audit.lastModifiedOn', 'axis, filter'],
			tcId : ['ID', 'NUMERIC', 'id', 'all'],
			tcCreatBy : ['CREATED_BY', 'STRING', 'audit.createdBy', 'axis, filter'],
			tcModBy : ['MODIFIED_BY', 'STRING', 'audit.lastModifiedBy', 'axis, filter'],
			tcLabel : ['LABEL', 'STRING', 'name', 'filter, measure'],
			tcImportance : ['IMPORTANCE', 'LEVEL_ENUM', 'importance', 'all'],
			tcNat : ['NATURE', 'INFO_LIST_ITEM', 'nature.label', 'all'],
			tcType : ['TYPE', 'INFO_LIST_ITEM', 'type.label', 'all'],
			tcStatus : ['STATUS', 'LEVEL_ENUM', 'status', 'all'],
			tcStatus : ['VERSCOUNT', 'NUMERIC', 'count(requirementVersionCoverages)', 'all', 'tcVerifVersionCountSub']
		], 
	
		subqueries : [
			tcVerifVersionCountSub : [
				label : 'TEST_CASE_VERSCOUNT_SUBQUERY',
				joinStyle : 'LEFT_JOIN',
				strategy : 'SUBQUERY',
				measures : ['rvId COUNT'],
				axes : ['tcId']
			]
		]
	],
	
	CAMPAIGN : [
		columns : [
			cId : ['ID', 'NUMERIC', 'id', 'all'],
			cName : ['LABEL', 'STRING', 'name', 'filter, measure'],
			cSchedStart : ['SCHED_START', 'DATE', 'scheduledPeriod.scheduledStartDate', 'axis, filter'],
			cSchedEnd : ['SCHED_END', 'DATE', 'scheduledPeriod.scheduledEndDate', 'axis, filter'],
			cActStart : ['ACTUAL_START', 'DATE', 'actualPeriod.actualStartDate', 'axis, filter'],
			cActEnd : ['ACTUAL_END', 'DATE', 'actualPeriod.actualEndDate', 'axis, filter'],	
		], 
	
		subqueries : [:]	
		
	],
	
	ITERATION : [
		columns : [
			itId : ['ID', 'NUMERIC', 'id', 'all'],
			itName : ['LABEL', 'STRING', 'name', 'filter, measure'],
			itSchedStart : ['SCHED_START', 'DATE', 'scheduledPeriod.scheduledStartDate', 'axis, filter'],
			itSchedEnd : ['SCHED_END', 'DATE', 'scheduledPeriod.scheduledEndDate', 'axis, filter'],
			itActStart : ['ACTUAL_START', 'DATE', 'actualPeriod.actualStartDate', 'axis, filter'],
			itActEnd : ['ACTUAL_END', 'DATE', 'actualPeriod.actualEndDate', 'axis, filter'],	
				
		], 
	
		subqueries : [:]	
			
	] ,

	ITEM_TEST_PLAN : [
		columns : [
			itpId : ['ID', 'NUMERIC', 'id', 'all'],
			itpLabel : ['LABEL', 'STRING', 'label', 'filter'],
			itpStatus : ['STATUS', 'EXECUTION_STATUS', 'executionStatus', 'all'],
			itpLastExec : ['LASTEXEC', 'DATE', 'lastExecutedOn', 'filter']	
		],
	
		subqueries : [:]	
	],

	EXECUTION : [
		columns : [
			exId : ['ID', 'NUMERIC', 'id', 'all'],
			exLabel : ['LABEL', 'STRING', 'name', 'axis, filter'],
			exDsLabel : ['DS_LABEL', 'STRING', 'datasetLabel', 'axis, filter'],
			exLastExec : ['LASTEXEC', 'DATE', 'lastExecutedOn', 'axis, filter'],
			exTesterLogin : ['TESTER_LOGIN', 'STRING', 'lastExecutedBy', 'axis, filter'],
			exStatus : ['STATUS', 'EXECUTION_STATUS', 'executionStatus', 'all']	
		],
	
		subqueries : [:]		
	],

	ISSUE : [
		columns : [
			isId : ['ID', 'NUMERIC', 'id', 'all'],
			isStatus : ['STATUS', 'STRING', 'status', 'axis, filter'],
			isSeverity : ['SEVERITY', 'String', 'severity', 'axis, filter'],
			isBugtrackerLabel : ['BUGTRACKER', 'STRING', 'bugtracker', 'axis, filter']	
		],
	
		subqueries : [:]		
		
	],

	TEST_STEP : [
		columns : [
			tsId : ['ID', 'NUMERIC', 'id', 'none'],
			tsClass : ['CLASS', 'STRING', 'class', 'none']	
		],
	
		subqueries : [:]			
	],
	
	TEST_CASE_NATURE : [
		columns : [
			tcnatId : ['ID', 'NUMERIC', 'id', 'none'],
			tcnatLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	TEST_CASE_TYPE : [
		columns : [
			tctypId : ['ID', 'NUMERIC', 'id', 'none'],
			tctypLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	REQUIREMENT_VERSION_CATEGORY : [
		columns : [
			rvcatId : ['ID', 'NUMERIC', 'id', 'none'],
			rvcatLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	TEST_CASE_MILESTONE : [
		columns : [
			tcmilId : ['ID', 'NUMERIC', 'id', 'none'],
			tcmilLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	REQUIREMENT_VERSION_MILESTONE : [
		columns : [
			rvmilId : ['ID', 'NUMERIC', 'id', 'none'],
			rvmilLabel : ['LABEL', 'STRING', 'label', 'none']	
		],
	
		subqueries : [:]
			
	],
	
	ITERATION_TEST_PLAN_ASSIGNED_USER : [
		columns : [
			itpassignId : ['ID', 'NUMERIC', 'id', 'none'],
			itpassignLogin : ['LOGIN', 'STRING', 'login', 'none']	
		],
	
		subqueries : [:]
			
	]




	

]

idmap = createIDmap(definition)

toSQL(definition)



// ********************* functions *********************


def createIDmap(definition){

	def colcount =0
	def querycount =0
	def idmap = [:]

	definition.each { entity, content ->
		content['columns'].each { colid, coldef ->
			idmap[colid] = colcount++
		}

		content['subqueries'].each { querid, querdef ->
			idmap[querid] = querycount++
		}
	}

	idmap
}



def toSQL(definition){

	File output = new File("chart-column-prototypes.sql")

	if (output.exists()){
		output.delete()
		output.createNewFile()
	}


	// first, process only attribute columns
	output.append """\n\n-- here come the basic attribute columns\n\n"""
	
	definition.each{ entity, content ->
		

		def attributeColumns = content['columns'].findAll{
			it.value.size() == 4
		}
		
		if (attributeColumns.size()>0){
			processColumns(output, entity, attributeColumns)
		}
		
		if (haveRoles(attributeColumns)){
			processRoles(output, attributeColumns)
		}

		output.append "\n\n"
		
	}
	
	// now , insert subqueries
	
	output.append """\n\n-- here come subqueries\n\n"""
	definition.each { entity, content ->
		
		
		
		def subqueries = content['subqueries']
		
		if (!subqueries.empty){
			processSubqueries(output, subqueries)
		}
		
	}

	// now insert the calculated columns that reference those subqueries
		output.append """\n\n-- here come the calculated columns referencing the subqueries attribute columns\n\n"""
	definition.each { entity, content ->
		
		
		
		def calculatedColumns = content['columns'].findAll{
			it.value.size() == 5
		}
		
		if (calculatedColumns.size() > 0){
			processColumns(output, entity, calculatedColumns)
		}
		
		if (haveRoles(calculatedColumns)){
			processRoles(output, calculatedColumns)
		}

		output.append "\n\n"
	}


}


def haveRoles(attributeColumns){
	
	def col = attributeColumns.find{ colid, coldef ->
		
		return (coldef[ROLES] != 'none')		
		
	}
	
}

def processColumns(output, entity, attributeColumns){
	//  append all the attribute column prototypes first
	
		output.append  """insert into CHART_COLUMN_PROTOTYPE(CHART_COLUMN_ID, COLUMN_TYPE, BUSINESS, LABEL, ENTITY_TYPE, ENTITY_ROLE, DATA_TYPE, ATTRIBUTE_NAME, SUBQUERY_ID)
values"""

		def colvalues = ""

		attributeColumns.each{ id, coldef ->
			colvalues += printColumn(entity, idmap[id], coldef)
		}

		output.append (colvalues.replaceAll(/,$/, ';\n\n'))

	
}

def printColumn(entity, id, coldef){
	def coltype = (coldef.size() <5) ? 'ATTRIBUTE' : 'CALCULATED'
	def business = (coldef[ROLES] == "none") ? 'FALSE' : 'TRUE'
	def label = entity + '_' + coldef[SUFFIX]

	def typrol = typerole(entity)
	def entityType = typrol[0]
	def entityRole = (typrol[1] == null) ? null : "'"+typrol[1]+"'"

	def datatype = coldef[DTYPE]
	def attname = coldef[ATTR]
	def queryid = (coldef.size() < 5 ) ? null : idmap[coldef[QUEREF]]

	return "\n\t($id, '$coltype', $business, '$label', '$entityType', $entityRole, '$datatype', '$attname', $queryid),"

}

def processRoles(output, attributeColumns){
	
	output.append """insert into CHART_COLUMN_ROLE(CHART_COLUMN_ID, ROLE)
values"""

	def rolevalues = ""

	attributeColumns.each { id, coldef ->
		rolevalues += printRoles(idmap[id], coldef[ROLES])
	}

	output.append (rolevalues.replaceAll(/,$/, ';\n\n'))

}

def printRoles(id, roles){
	def roleinserts = "\n\t"
	
	def localroles = roles
	
	if (localroles == "none"){
		roleinserts = ""
	}
	else{
		if (localroles == "all"){
			localroles = "measure, axis, filter"
		}
		def tokens = localroles.split(',').collect{it.trim()}
	
		tokens.each{
			roleinserts +=" ($id, '${it.toUpperCase()}'),"
		}
	}

	return roleinserts;

}

def processSubqueries(output, subqueries){
	

	subqueries.each{ querid, querdef ->

		// insert the query entry
		def basequeryid = idmap[querid]
		def strategy = (querdef['strategy'] != null) ? querdef['strategy'] : 'SUBQUERY'
		def joinStyle = (querdef['joinStyle'] != null) ? querdef['joinStyle'] : 'INNER_JOIN'
		
		output.append """insert into CHART_QUERY(CHART_QUERY_ID, NAME, STRATEGY, JOIN_STYLE) values ($basequeryid, '$querid', '$strategy', '$joinStyle')\n"""
		
		// insert the measures for it
		querdef['measures'].eachWithIndex { item, index ->
			def splitname = item.split()
			def colref = splitname[0]
			def operation = (splitname.size() > 1) ? splitname[1] : 'NONE'
			def id = idmap[colref]
			
			output.append """insert into CHART_MEASURE_COLUMN($id, $basequeryid, '$operation', $index)\n"""
		}
		
		// insert the axes
		querdef['axes'].eachWithIndex { item, index ->
			def splitname = item.split()
			def colref = splitname[0]
			def operation = (splitname.size() > 1) ? splitname[1] : 'NONE'
			def id = idmap[colref]
			
			output.append """insert into CHART_AXIS_COLUMN($id, $basequeryid, '$operation', $index)\n\n\n"""
		}
		
	}  
	
}

def typerole(tpname){
	switch(tpname){
		case "REQUIREMENT" : return ["REQUIREMENT", null]
		case "REQUIREMENT_VERSION" : return ["REQUIREMENT_VERSION", null]
		case "TEST_CASE" : return ["TEST_CASE", null]
		case "CAMPAIGN" : return ["CAMPAIGN", null]
		case "ITERATION" : return ["ITERATION", null]
		case "ITEM_TEST_PLAN" : return ["ITEM_TEST_PLAN", null]
		case "EXECUTION" : return ["EXECUTION", null]
		case "ISSUE" : return ["ISSUE", null]
		case "TEST_STEP" : return ["TEST_STEP", null]
		case "TEST_CASE_NATURE" : return ["INFO_LIST_ITEM", "TEST_CASE_NATURE"]
		case "TEST_CASE_TYPE" : return ["INFO_LIST_ITEM", "TEST_CASE_TYPE"]
		case "REQUIREMENT_VERSION_CATEGORY" : return ["INFO_LIST_ITEM", "REQUIREMENT_VERSION_CATEGORY"]
		case "TEST_CASE_MILESTONE" : return ["MILESTONE", "TEST_CASE_MILESTONE"]
		case "REQUIREMENT_VERSION_MILESTONE" : return ["MILESTONE", "REQUIREMENT_VERSION_MILESTONE"]
		case "ITERATION_TEST_PLAN_ASSIGNED_USER" : return ["USER", "ITERATION_TEST_PLAN_ASSIGNED_USER"]
	}
}














