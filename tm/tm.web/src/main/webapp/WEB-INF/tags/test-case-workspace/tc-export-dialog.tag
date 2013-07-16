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
<%@ tag body-content="empty"
	description="popup for requirement export. Requires a tree to be present in the context."%>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ attribute name="treeSelector"
	description="jQuerySelector for the tree."%>
<%@ attribute name="treeNodeButton"	required="true" description="the javascript button that will open the dialog" %>
<%@ attribute name="resourceName" required="true" description="the resourceName : 'test-case', 'requirement' or 'campaign' '" %>

<script type="text/javascript"
	src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.dateformat.js"></script>


<c:url var="exportFolderUrl" value="/${resourceName}-browser/export-folder" />
<c:url var="exportLibraryUrl" value="/${resourceName}-browser/export-library" />

<c:choose>
<c:when test="${ resourceName == 'requirement' }">
	<f:message var="crossProjectError" key="message.exportRequirementCrossProjectError" />
	<f:message var="exportNamePrefix" key="label.lower.dash.exportRequirements" />
	<f:message var="noNodeSelected" key="message.exportRequirementNoNodeSelected"/>
</c:when>
<c:when test="${ resourceName == 'test-case'  }">
	<f:message var="crossProjectError" key="message.exportTestCaseCrossProjectError" />
	<f:message var="exportNamePrefix" key="label.lower.dash.exportTestCase" />
	<f:message var="noNodeSelected" key="message.exportTestCaseNoNodeSelected"/>
</c:when>
</c:choose>

<script type="text/javascript">
$(function(){
	${treeNodeButton}.click(function(){
		$('#export-${resourceName}-node-dialog').dialog('open');
			return false;		
		});	
			
});

	function customSerialize(array, name) {

		var serialized = name + "=" + array[0];

		for ( var i = 1; i < array.length; i++) {
			serialized += "&" + name + "=" + array[i];
		}

		return serialized;
	}

</script>


<pop:popup id="export-${resourceName}-node-dialog"	titleKey="label.Export" >
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="label.Export" />
		'${ label }': function() {
			var nodes = $("${treeSelector}").jstree("get_selected");
			var url = (nodes.is(':library')) ? "${ exportLibraryUrl }" : "${ exportFolderUrl }";
			if ((nodes.length) && (nodes.areSameLibs())){
				var tab = nodes.all('getResId');
				var filename = $('#export-name-input').val();
				var exportFormat = $('#export-option').val();
				url+="?name="+filename+"&"+customSerialize(tab,"tab[]")+"&format="+exportFormat;
				document.location.href = url;
			}else{
				$.squash.openMessage("<f:message key='popup.title.error' />", "${crossProjectError}");
			}
			
			$("#export-${resourceName}-node-dialog").dialog("close");
		},
		<pop:cancel-button />
	</jsp:attribute>

	<jsp:attribute name="additionalSetup">
		open : function(){
			var selection = $("${treeSelector}").jstree("get_selected");
			if(selection.length){
				var name = '${exportNamePrefix }-' + new Date().format('<f:message
				key="export.dateformat" />');
				$("#export-name-input").val(name);
			}
			else{
				$.squash.openMessage("<f:message key='popup.title.error' />", "${noNodeSelected}");
				$("#export-${resourceName}-node-dialog").dialog("close");
			}		
		}
	
	</jsp:attribute>

	<jsp:attribute name="body">
		<div class="display-table" style="width:100%">
		<div style="display:table-column-group">
		    <div style="display:table-column" ></div>
		    <div style="display:table-column; width:70%" ></div>
		  </div>
			<div class="display-table-row">
			<label><f:message key="dialog.rename.label" /></label>
			<div class="display-table-cell" ><input type="text" id="export-name-input" style="width:100%"/></div>
			</div>
			<div class="display-table-row">		
			<label><f:message key="label.ExportFormat" />
			</label><div class="display-table-cell"><select id="export-option" >
				<option value="csv">csv</option>
				<option value="xls">xls</option>
			</select></div>
			</div>
		</div>
	</jsp:attribute>
</pop:popup>


