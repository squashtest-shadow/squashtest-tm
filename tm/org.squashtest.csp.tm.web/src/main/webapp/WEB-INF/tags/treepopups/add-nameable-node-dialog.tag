<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
<%@ tag body-content="empty"
	description="Defines a dialog which adds a nameable node to a tree"%><%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="su"
	uri="http://org.squashtest.csp/taglib/string-utils"%>
	
<%@ attribute name="resourceName" required="true"
	description="Name of the resource to add. Should be a lowercase, hyphened name. eg : 'test-case'"%>
<%@ attribute name="treeNodeButton" required="true" description="the javascript button that will open the dialog" %>

<c:url var="requirementCriticalityList"
	value="/requirement-workspace/criticality-options" />

<c:set var="handlerName"
	value="add${ su:hyphenedToCamelCase(resourceName) }Handler" />
<c:choose>
	<c:when
		test="${ (resourceName eq 'test-case') or (resourceName eq 'campaign') or (resourceName eq 'requirement') }">
		<c:set var="openButtonId" value="new-leaf-tree-button" />
	</c:when>
	<c:when test="${ (resourceName eq 'folder') }">
		<c:set var="openButtonId" value="new-folder-tree-button" />
	</c:when>
	<c:otherwise>
		<c:set var="openButtonId" value="new-resource-tree-button" />
	</c:otherwise>
</c:choose>


<c:if test='${ resourceName == "requirement" }'>
<script type="text/javascript">
$(function(){
	$.ajax({
		url: "${ requirementCriticalityList }",  
		context: document.body,
		success: function(data){
			  $('#criticalityList').html(data);
		  }
		});
});
</script>
</c:if>

<pop:popup id="add-${ resourceName }-dialog"
	titleKey="dialog.new-${ resourceName }.title">
	<jsp:attribute name="buttons">
		<pop:button labelKey="dialog.button.add.label"
			handler="${ handlerName }" />
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<table>
			<tr>
				<td><label for="add-${ resourceName }-name"><f:message
							key="${ resourceName }.name.label" />
				</label>
				</td>
				<td>
					<input id="add-${ resourceName }-name" type="text" size="50" /><br />
					<comp:error-message forField="name" />
				</td>
			</tr>
			<c:if test='${ resourceName == "requirement" }'>
				<tr>
					<td><label for="add-requirement-reference"><f:message key="requirement.reference.label" /></label></td>
					<td>
						<input id="add-requirement-reference" type=text size="15" />
					<td>
				</tr>
				<tr>
					<td><label for="add-requirement-criticality"><f:message key="requirement.criticality.combo.label" /></label></td>
					<td><div id="criticalityList"></div></td>
				</tr>
			</c:if>
			<tr>
				<td><label for="add-${ resourceName }-description"><f:message
							key="${ resourceName }.description.label" />
				</label>
				</td>
				<td><textarea id="add-${ resourceName }-description"></textarea>
				</td>
			</tr>
		</table>
	</jsp:attribute>
</pop:popup>
<script type="text/javascript">

	function ${ handlerName }() {
		<c:choose>
			<c:when test='${ resourceName eq "requirement" }'>
				var params = <jq:params-bindings 
				name="#add-${ resourceName }-name" 
				description="#add-${ resourceName }-description" 
				reference="#add-requirement-reference" 
				criticality="#add-requirement-criticality"/>;
			</c:when>
			<c:otherwise>
				var params = <jq:params-bindings 
				name="#add-${ resourceName }-name" 
				description="#add-${ resourceName }-description"/>;
			</c:otherwise>
		</c:choose>
		postNewTreeContent('tree', 'new-${ resourceName }', params);
	}
	
	$(function(){
		${treeNodeButton}.click(function(){
			$('#add-${ resourceName }-dialog').dialog('open');
			return false;
		});
	});
	
</script>