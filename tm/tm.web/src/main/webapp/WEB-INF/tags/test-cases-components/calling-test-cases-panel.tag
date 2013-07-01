<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/input" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>



<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>
<%@ attribute name="model" required="true" type="java.lang.Object"  description="the model for this table" %>



<s:url var="modelUrl"	  value="/test-cases/${testCase.id}/calling-test-cases/table" />
<s:url var="languageUrl"  value="/datatables/messages" />
<s:url var="callingTCUrl" value="/test-cases/{tc-id}/info"/>



<comp:toggle-panel id="calling-test-case-panel"
				   titleKey="test-case.calling-test-cases.panel.title"
				   isContextual="true" 
				   open="true">


	<jsp:attribute name="body">
		<table id="calling-test-case-table" data-def="ajaxsource=${modelUrl}, language=${languageUrl}, hover, 
													  datakeys-id=tc-id, pagesize=10, pre-sort=4, deferloading=${model.iTotalRecords}">
			<thead>
				<tr>
					<th data-def="map=tc-index, select">#</th>
					<th data-def="map=project-name, sortable"><f:message key="label.project" /></th>
					<th data-def="map=tc-reference, sortable"><f:message key="test-case.reference.label" /></th>
					<th data-def="map=tc-name, sortable, sWidth=15em, link=${callingTCUrl}"><f:message key="label.Name" /></th>
					<th data-def="map=tc-mode, sortable"><f:message key="test-case.calling-test-cases.table.execmode.label" /></th>				
				</tr>
			</thead>
			<tbody>
			</tbody>		
		</table>	
	</jsp:attribute>


</comp:toggle-panel>

<script type="text/javascript">

	$(function(){

		var conf = {
			'aaData' : ${json:serialize(model.aaData)}
		}
		
		var table = $("#calling-test-case-table").squashTable(conf, {});
		
	});
</script>