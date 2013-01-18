<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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

<%-- TODO : obsolete. To be rewritten some day. --%>

<%@ tag body-content="empty"
	description="Defines a dialog which adds a nameable node to a tree"%><%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="su" uri="http://org.squashtest.tm/taglib/string-utils"%>
	
<%@ attribute name="resourceName" required="true" description="Name of the resource to add. Should be a lowercase, hyphened name. eg : 'test-case'"%>
<%@ attribute name="treeNodeButton" required="true" description="the javascript button that will open the dialog" %>


<c:url var="requirementComboLists"	value="/requirement-workspace/combo-options" />
<c:url var="customFieldBindings" value="/custom-fields-binding"/>

<c:set var="handlerName" value="add${ su:hyphenedToCamelCase(resourceName) }Handler" />
<c:set var="addAnotherHandlerName"	value="addAnother${ su:hyphenedToCamelCase(resourceName) }Handler" />
<c:set var="genericHandlerName"	value="genericAdd${ su:hyphenedToCamelCase(resourceName) }Handler" />

<c:choose>
	<c:when	test="${ (resourceName eq 'test-case') or (resourceName eq 'campaign') or (resourceName eq 'requirement') }">
		<c:set var="openButtonId" value="new-leaf-tree-button" />
	</c:when>
	<c:when test="${ (resourceName eq 'folder') }">
		<c:set var="openButtonId" value="new-folder-tree-button" />
	</c:when>
	<c:otherwise>
		<c:set var="openButtonId" value="new-resource-tree-button" />
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${ resourceName eq 'test-case' }">
		<c:set var="bindableEntity" value="TEST_CASE"/>
	</c:when>
	<c:when test="${ resourceName eq 'requirement'}">
		<c:set var="bindableEntity" value="REQUIREMENT_VERSION"/>
	</c:when>
	<c:when test="${ resourceName eq 'campaign'}">
		<c:set var="bindableEntity" value="CAMPAIGN"/>
	</c:when>
	<c:when test="${ resourceName eq 'iteration'}" >
		<c:set var="bindableEntity" value="ITERATION"/>
	</c:when>
</c:choose>


<c:if test='${ resourceName == "requirement" }'>
<script type="text/javascript">
$(function(){
	$.ajax({
		url: "${ requirementComboLists }",  
		context: document.body,
		success: function(data){ $('#criticalityList').html(data.criticities);
			  $('#categoryList').html(data.categories);
		  }
		});
});

</script>
</c:if>

<c:choose>
	<c:when test="${ (resourceName eq 'folder') || (resourceName eq 'test-case') }">
		<c:set var="addAnotherLabelKey" value="label.addAnother"/>
	</c:when>
	<c:otherwise>
		<c:set var="addAnotherLabelKey" value="label.fem.addAnother"/>
	</c:otherwise>
</c:choose>

<pop:popup id="add-${ resourceName }-dialog" titleKey="dialog.new-${ resourceName }.title" closeOnSuccess="${false}">
	<jsp:attribute name="buttons">
		<pop:button labelKey="${addAnotherLabelKey}" handler="${ addAnotherHandlerName }" />
		<pop:button labelKey="label.Add" handler="${ handlerName }" />
		<pop:cancel-button/>
	</jsp:attribute>
	<jsp:attribute name="body">
		<table class="add-node-attributes">
			<tr>
				<td><label for="add-${ resourceName }-name"><f:message key="label.Name" /></label></td>
				<td><input id="add-${ resourceName }-name" type="text" size="50" maxlength="255" /><br />
					<comp:error-message forField="name" /></td>
			</tr>
			<c:if test='${ resourceName == "requirement" || resourceName == "test-case"  }'>
				<tr>
					<td><label for="add-${ resourceName }-reference"><f:message key="requirement.reference.label" /></label></td>
					<td><input id="add-${ resourceName }-reference" type=text size="15" maxlength="20"/><br />
						<comp:error-message forField="reference" />	<td>
				</tr>
				<c:if test='${ resourceName == "requirement" }'>
				<tr><td><label for="add-requirement-criticality"><f:message key="requirement.criticality.combo.label" /></label></td>
					<td><div id="criticalityList"></div></td></tr>
				<tr><td><label for="add-requirement-category"><f:message key="requirement.category.combo.label" /></label></td>
					<td><div id="categoryList"></div></td></tr>
				</c:if>
			</c:if>
			<tr>
				<td><label for="add-${ resourceName }-description"><f:message key="label.Description" />
				</label></td>
				<td><textarea id="add-${ resourceName }-description"></textarea></td>
			</tr>
			<c:if test='${ resourceName == "iteration"}'>
				<tr>
					<td></td>
					<td>
					<input id="copy-test-plan-box" name="copy-test-plan-box" type="checkbox" onClick="setCopyTestPlan()"/>
					<input type=hidden id="copy-test-plan" value="false"></input>
					<label class="afterDisabled" for="copy-test-plan-box"><f:message key="dialog.new-iteration.copy"/></label>
					</td>
				</tr>
			</c:if>
		</table>
	</jsp:attribute>
</pop:popup>


<script type="text/javascript">

	function setCopyTestPlan(){
		$('#copy-test-plan').val($('#copy-test-plan-box').is(':checked'));
	}

	function ${ handlerName }() {
		var params = ${genericHandlerName}();
		$('#tree').jstree('postNewNode','new-${ resourceName }', params).then(function(){
			$("#add-${ resourceName }-dialog").dialog('close');
		});
	}
	function ${ addAnotherHandlerName }() {
		var params = ${genericHandlerName}();
		$('#tree').jstree('postNewNode','new-${ resourceName }', params, false).then(function(){
			$('#add-${ resourceName }-name').val("");
			CKEDITOR.instances["add-${ resourceName }-description"].setData('');
			<c:if test='${ resourceName == "requirement" || resourceName == "test-case"  }'>
			$('#add-${ resourceName }-reference').val("");
			</c:if>
			$('.error-message').text("");
			<c:if test="${not empty bindableEntity}">
			var dialog =  $('#add-${ resourceName }-dialog')
						  .data('cuf-values-support')
						  .reset();
			</c:if>
		});
	}
	

	function ${genericHandlerName}(){
		<c:choose>
		<c:when test='${ resourceName eq "requirement" }'>
		var params = {
			name : $("#add-requirement-name").val(), 
			description : $("#add-requirement-description").val(), 
			reference : $("#add-requirement-reference").val(), 
			criticality : $("#add-requirement-criticality").val(),
			category : $("#add-requirement-category").val()
		};
		</c:when>
		<c:when test='${ resourceName eq "test-case" }'>
		var params = {
			name : $("#add-test-case-name").val(), 
			description : $("#add-test-case-description").val(), 
			reference : $("#add-test-case-reference").val()
		};
		</c:when>
		<c:when test='${ resourceName eq "iteration" }'>
		var params = {
			name : $("#add-iteration-name").val(), 
			description : $("#add-iteration-description").val(),
			copyTestPlan : $("#copy-test-plan").val()
		};
		cleanup();
		</c:when>
		<c:otherwise>
		var params = {
			name : $("#add-${ resourceName }-name").val(), 
			description : $("#add-${ resourceName }-description").val()
		};
		</c:otherwise>
		</c:choose>
		
		<c:if test="${not empty bindableEntity}">
		
		var dialog =  $('#add-${ resourceName }-dialog');	
		var cufParams = dialog.data('cuf-values-support').readValues();
		
		$.extend(params, cufParams);
		
		</c:if>
		
		return params;	
		
	}

	
	function cleanup(){
		<c:if test='${ resourceName eq "iteration" }'>
			$('#copy-test-plan-box').attr('checked', true);
			setCopyTestPlan();
		</c:if>
	}
	
	
	$(function(){
		${treeNodeButton}.click(function(){
			$('#add-${ resourceName }-dialog').dialog('open');
			cleanup();
			return false;
		});

		<c:if test="${not empty bindableEntity}">
		
		require(['jquery', 'custom-field-values'] , function($,cufValuesManager){	

			var dialog = $("#add-${ resourceName }-dialog");			
			var table = dialog.find('table.add-node-attributes');
			var cufValuesCreator = cufValuesManager.newCUFValuesCreator({table : table});			
		
			dialog.on("dialogopen", function(){						
				var projectId = $("#tree").jstree('get_selected').getProjectId();
				var bindingsUrl = "${customFieldBindings}?projectId="+projectId+"&bindableEntity=${bindableEntity}&optional=false";
						
				cufValuesCreator.loadPanel(bindingsUrl);
			});
			
			dialog.data('cuf-values-support', cufValuesCreator);			
			
		});

		</c:if>
	});
	
</script>