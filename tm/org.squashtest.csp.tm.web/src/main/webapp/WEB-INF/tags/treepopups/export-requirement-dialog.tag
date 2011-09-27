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
<%@ tag body-content="empty" description="popup for requirement export. Requires a tree to be present in the context."%>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="openedBy" description="id of the widget that will open the popup"%>
<%@ attribute name="treeSelector" description="jQuerySelector for the tree."%>


<c:url var="exportFolderUrl" value="/requirement-browser/export-folder" />
<c:url var="exportLibraryUrl" value="/requirement-browser/export-library" />

<script type="text/javascript">
	function customSerialize(array, name){

		var serialized = name +"="+  array[0];
		
		for (var i=1;i<array.length;i++){
			serialized+="&"+name+"="+array[i];
		}
		
		return serialized;
	}

</script>

<comp:popup id="export-requirement-node-dialog" titleKey="dialog.export-requirement.title" openedBy="${openedBy}">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="dialog.export-requirement.title" />
		'${ label }': function() {
			var data = $("${treeSelector}").jstree("get_selected");
			var url = "";
			
			if ($(data[0]).attr("rel") != "drive"){
				url  = "${ exportFolderUrl }";
			}else{
				url = "${ exportLibraryUrl }";
			}
			if (!checkCrossProjectSelection(data)){
				var tab = getIds(data, 3);
				var filename = $('#export-name-requirement-input').val();
				url+="?name="+filename+"&"+customSerialize(tab,"tab[]");
				document.location.href = url;
			}
			
			$("#export-requirement-node-dialog").dialog("close");
		},
		<pop:cancel-button />
	</jsp:attribute>
	
	<jsp:body>
		<script type="text/javascript">
		$( "#export-requirement-node-dialog" ).bind( "dialogopen", function(event, ui) {
			var now = new Date();
			var name = "export-" + now.toLocaleString();
			name = name.replace(/ /g, '');
			name = name.replace(/:/g, '');
			$("#export-name-requirement-input").val(name);
		});
		</script>	
		<label><f:message key="dialog.rename.label" /></label>
		<input type="text" id="export-name-requirement-input" /><br/>
		<select>
			<option value="1">CSV</option>
		</select>
	</jsp:body>
</comp:popup>		


