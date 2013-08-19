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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>
<%@ attribute name="smallEditable"  required="true" type="java.lang.Boolean"  description="if the user has small edit permission on this test case" %>
<%@ attribute name="deletable"  required="true" type="java.lang.Boolean"  description="if the user has deletion permission on this test case" %>



<c:url var="testCaseUrl" 					value="/test-cases/${testCase.id}"/>

<%---------------------------- Rename test case popup ------------------------------%>

<c:if test="${ smallEditable }">
	<pop:popup id="rename-test-case-dialog"
		titleKey="dialog.rename-test-case.title" isContextual="true"
		openedBy="rename-test-case-button">
		
		<jsp:attribute name="buttons">
	
			<f:message var="label" key="dialog.rename-test-case.title" />
			
			'${ label }': function() {
				var newName = $("#rename-test-case-input").val();
				$.ajax({
					url : "${testCaseUrl}",
					type : "POST",
					dataType : "json",
					data : { 'newName' : newName}
				}).success(renameTestCaseSuccess);			
			},
			
			<pop:cancel-button />
			
		</jsp:attribute>
		
		<jsp:attribute name="body">
				<label>
					<f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-test-case-input" 
					   maxlength="255"	size="50" />
				<br />
				<comp:error-message forField="name" />
		</jsp:attribute>
	</pop:popup>
</c:if>

<%--------------------------- Deletion confirmation popup -------------------------------------%>

<c:if test="${ deletable }">

	<comp:delete-contextual-node-dialog
		itemId="${testCase.id}"
		successCallback="deleteTestCaseSuccess"
		openedBy="delete-test-case-button"
		titleKey="dialog.delete-test-case.title" />

</c:if>
