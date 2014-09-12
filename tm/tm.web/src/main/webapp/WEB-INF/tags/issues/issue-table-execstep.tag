<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag description="Table displaying the issues for an ExecutionStep" body-content="empty" %>
	
<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>	
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface"%>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="bugTrackerUrl" required="true" description="where the delete buttons send the delete instruction" %>
<%@ attribute name="freeSettings" required="true" description="added settings to issue table" %>
<%@ attribute name="entityId" required="true" description="id of the current execution step" %>
<%@ attribute name="executable" required="true" description="if the user has EXECUTE rights on the execution" %>
<%--

	columns are :
	
		- URL (not shown)
		- ID
		- owner
		- Priority
		- Summary

 --%>
 
<c:url var="tLanguageUrl" value="/datatables/messages"/>
<c:if test="${executable}">
	<c:set var="deleteBtnClause" value=", sClass=centered delete-button"/>
</c:if>
<table id="issue-table" data-def="ajaxsource=${dataUrl}, pre-sort=0-asc">
	<thead>
		<tr>
			<th style="cursor:pointer" data-def="link={issue-url}, select, map=remote-id, sortable, narrow, sClass=id-header">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th data-def="map=summary">${interfaceDescriptor.tableSummaryHeader}</th>
			<th data-def="map=priority">${interfaceDescriptor.tablePriorityHeader}</th>
			<th data-def="narrow, map=empty-delete-holder${ deleteBtnClause }"></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>


<script type="text/javascript">
require( ["common"], function(){
		require(["jquery","issues/issues-table"], function($,it){
	$(function(){
			it.initTestStepIssueTable({
				target : '#issue-table',
				urls : {
					bugtracker : "${bugTrackerUrl}",					
				},
				language : {
					removeMessage : '<f:message key="dialog.remove-testcase-association.message" />',
					removeTooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />'
				}
			});
		});
	});
});
</script>
