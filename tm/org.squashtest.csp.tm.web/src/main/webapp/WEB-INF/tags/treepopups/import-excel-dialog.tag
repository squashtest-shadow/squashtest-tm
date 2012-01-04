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
	description="javascript handling the copy and paste of nodes in the tree"%>
	

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>	
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

	
<%@ attribute name="treeSelector" required="true" description="jquery selector of the tree instance" %>
<%@ attribute name="treeNodeButton" required="true" description="the javascript button that will open the dialog" %>
<%@ attribute name="workspace" required="true" description="the workspace (or nature) of the elements to import." %>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.form.js"></script>
<script type="text/javascript" src="http://localhost/scripts/FeedbackMultipartPopup.js"></script>

<s:url var="importUrl" value="/${workspace}-browser/import/upload"/>


<pop:popup id="import-excel-dialog" titleKey="dialog.import-excel.title" isContextual="false"  closeOnSuccess="false">
	<jsp:attribute name="buttonsArray">	
		<f:message var="confirmLabel" key="dialog.import.confirm.label" />	
		<f:message var="cancelLabel" key="dialog.button.cancel.label"/>
		{
			text : "${confirmLabel}",
			class : FeedbackMultipartPopup.CONFIRM_CLASS,
			click : function(){importExcelFeedbackPopup.initSubmission();}
		},
		{
			text : "Ok", <!--  todo : make it i18n -->
			class : FeedbackMultipartPopup.OK_CLASS,
			click : function(){alert("todo");}
		
		},
		{
			text : "${cancelLabel}",
			class : FeedbackMultipartPopup.CANCEL_CLASS,
			click : function(){alert("todo");}			
		}
	</jsp:attribute>
	
	<jsp:attribute name="additionalSetup">
		open : function(){
			importExcelFeedbackPopup.reset();
			
			var selected = $("${treeSelector}").jstree("get_selected");
			var projectNode = liNode(selected);
			
			var projectName = projectNode.attr('title');
			var projectId = projectNode.attr('resId');
			
			$("#import-excel-dialog-parametrization .import-project-name-span").text(projectName);
			$("#import-excel-dialog-parametrization input[type='hidden']").val(projectId);
		}	
	</jsp:attribute>

	<jsp:attribute name="body">
		<div id="import-excel-dialog-parametrization">
			<div>
				<form id="import-excel-form" action="${importUrl}" method="POST" enctype="multipart/form-data">
					<input type="hidden" name="projectId"/>
					<input type="file" name="archive" size="40" />
				</form>
			
			</div>
			<div>
				<span><f:message key="dialog.import.project.label"/></span>
				<span class="import-project-name-span" style="font-weight:bold;"></span>
			</div>	
		</div>
		
		<div id="import-excel-dialog-progession">
			please wait
		</div>
		
		<div id="import-excel-dialog-summary">
			done
		</div>
		
		<div id="import-excel-dialog-dump">
		
		</div>
		
	</jsp:attribute>	

</pop:popup>

<script type="text/javascript">

	var importExcelFeedbackPopup = null;
	$(function(){		
		
		var settings = {
			popup : $("#import-excel-dialog"),
			
			parametrizationPanel : {
				selector : "#import-excel-dialog-parametrization",
				submitUrl : "${importUrl}"
			},
			
			progressPanel : {
				selector : "#import-excel-dialog-progession"
			},
			
			summaryPanel : {
				selector : "#import-excel-dialog-summary"			
			},
			
			dumpPanel : {
				selector : "#import-excel-dialog-dump"	
			}
				
				
		};
		
		importExcelFeedbackPopup = new FeedbackMultipartPopup(settings);
		
		
		${treeNodeButton}.click(function(){
			$('#import-excel-dialog').dialog('open');
			return false;		
		});		
	});
</script>
