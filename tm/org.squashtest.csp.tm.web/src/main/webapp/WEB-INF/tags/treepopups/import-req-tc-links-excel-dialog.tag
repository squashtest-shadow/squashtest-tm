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
<%@ tag body-content="empty" description="javascript handling the copy and paste of nodes in the tree"%>
	

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>	
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

<%@ attribute name="treeSelector" 		required="true" description="jquery selector of the tree instance" %>
<%@ attribute name="treeNodeButton"		required="true" description="the javascript button that will open the dialog" %>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.form.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/FeedbackMultipartPopup.js"></script>    
<%--  <script type="text/javascript" src="http://localhost/scripts/FeedbackMultipartPopup.js"></script>  --%>


<s:url var="importUrl" value="/req-tc/import-links/upload"/>

<%-- 
	Note : as long as this popup is open if and only if exactly one drive node is selected, the following code is safe.
	if not, consider checking the results of $(tree).jstree("get_selected");

 --%>

			<f:message var="importInputFileLabel" key="dialog.import-links-excel.filetype.message"/>
			<c:set var="importFormatMIME" value="vnd.ms-excel"/>
			<c:set var="importFormatParam" value="'xls', 'xlsx'"/>
			<c:set var="importFormatText" value="xls, xlsx"/>
			
<pop:popup id="import-links-excel-dialog" titleKey="dialog.import-links-excel.title" isContextual="false"  closeOnSuccess="false">
	<jsp:attribute name="buttonsArray">	
		<f:message var="confirmLabel" key="dialog.import.confirm.label" />	
		<f:message var="cancelLabel" key="dialog.button.cancel.label"/>
		<f:message var="okLabel" key="dialog.button.ok.label"/>
		{
			text : "${confirmLabel}",
			"class" : FeedbackMultipartPopup.PARAMETRIZATION,
			click : function(){	importLinksExcelFeedbackPopup.validate();}
		},
		{		
			text : "${okLabel}",
			"class" : FeedbackMultipartPopup.SUMMARY,
			click : function(){
				var thisDialog = $("#import-links-excel-dialog");
				thisDialog.dialog("close");
				
			}
		},
		{
			text : "${okLabel}",
			"class" : FeedbackMultipartPopup.CONFIRM,
			click : function(){
				importLinksExcelFeedbackPopup.submit();
			}
		},
		{
			text : "${cancelLabel}",
			"class" : FeedbackMultipartPopup.PROGRESSION+" "+FeedbackMultipartPopup.PARAMETRIZATION+" "+FeedbackMultipartPopup.CONFIRM,
			click : function(){importLinksExcelFeedbackPopup.cancel();}	
		}
	</jsp:attribute>
	
	<jsp:attribute name="additionalSetup">
		open : function(){
			importLinksExcelFeedbackPopup.reset();
		}	
	</jsp:attribute>

	<jsp:attribute name="body">
		<div class="parametrization">
			<div style="margin-top:1em;margin-bottom:1em;">
				<form action="${importUrl}" method="POST" enctype="multipart/form-data" class="display-table">
					<div class="display-table-row">
						<div class="display-table-cell"><label>${ importInputFileLabel }</label></div>
						<div class="display-table-cell">
							<input type="file" name="archive" size="20" accept="application/${ importFormatMIME }" 
							onchange="var filename = /([^\\]+)$/.exec(this.value)[1]; $('#import-links-excel-dialog .confirm-file').text(filename);"/>
						</div>
					</div>
				</form>
			</div>
		</div>
		
		<div class="confirm">
			<div class="confirm-div">
				<label class="confirm-label"><f:message key="dialog.import.file.confirm"/></label>
				<span class="confirm-span confirm-file"></span>
			</div>
			<span style="display:block"><f:message key="dialog.import.confirm.message"/></span>
		</div>
		
		<div class="progression" style="vertical-align:middle;">
 			<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
			<span style="font-size:1.5em;"><f:message key="squashtm.processing"/></span>			
		</div>
		
			
			
			
		<div class="summary">
		<br/>
			<div>
				<span class="span-bold" ><f:message key="dialog.import-links-excel.success"/></span>
				<span class="success-import span-bold span-green"></span>
			</div>
			<div>
				<span class="span-bold" ><f:message key="dialog.import-links-excel.failed"/></span>
				<span class="failures-import span-bold"></span>
			</div>			
			
			
			<div class="import-links-excel-dialog-note">
				<br/>
				<span><f:message key="dialog.import.summary.notes.label"/><br>
				<f:message key="dialog.import-links.summary.notes.lines"/>
				</span>
				<ul  contenteditable="true" style="height:100px ; overflow : auto" >
					<li class="import-links-excel-dialog-req-not-found">
						<span><f:message key="dialog.import.summary.notes.req-not-found.label"/></span>
						<span class="req-not-found-import"></span>
					</li>
					<li class="import-links-excel-dialog-tc-not-found">
						<span><f:message key="dialog.import.summary.notes.tc-not-found.label"/></span>
						<span class="tc-not-found-import"></span>
					</li>
					<li class="import-links-excel-dialog-version-not-found">
						<span><f:message key="dialog.import.summary.notes.version-not-found.label"/></span>
						<span class="version-not-found-import"></span>
					</li>
					<li class="import-links-excel-dialog-link-already-exist">
						<span><f:message key="dialog.import.summary.notes.link-already-exist.label"/></span>
						<span class="link-already-exist-import"></span>
					</li>
					<li class="import-links-excel-dialog-obsolete">
						<span><f:message key="dialog.import.summary.notes.obsolete.label"/></span>
						<span class="obsolete-import"></span>
					</li>
					<li class="import-links-excel-dialog-req-access-denied">
						<span><f:message key="dialog.import.summary.notes.req-access-denied.label"/></span>
						<span class="req-access-denied-import"></span>
					</li>
					<li class="import-links-excel-dialog-tc-access-denied">
						<span><f:message key="dialog.import.summary.notes.tc-access-denied.label"/></span>
						<span class="link-already-exist-import"></span>
					</li>
				</ul>
			</div>
		</div>
		
	</jsp:attribute>

</pop:popup>


<f:message var="wrongFileMessage" key="dialog.import.wrongfile" />
<script type="text/javascript">


	var importLinksExcelFeedbackPopup = null;
	
	
	function importSummaryBuilder(response){
			
		var panel = $("#import-links-excel-dialog .summary");
		
		//basic infos			
		$(".success-import", panel).text(response.success);
		
		var failSpan = $(".failures-import", panel).text(response.failures);
		if (response.failures==0){ failSpan.removeClass("span-red"); }else{	failSpan.addClass("span-red"); }
		
		// notes
		if (response.failures==0){
			$(".import-links-excel-dialog-note", panel).hide();
		}else{
			$(".import-links-excel-dialog-note", panel).show();
			
			var obsoleteDialog = $(".import-links-excel-dialog-obsolete", panel);
			if ($.trim(response.obsolete) != "") { 
				$(".obsolete-import", panel).text(response.obsolete);
				obsoleteDialog.show(); 
				} else { obsoleteDialog.hide(); }
			
			var reqAccessDeniedDialog = $(".import-links-excel-dialog-req-access-denied", panel);
			if ($.trim(response.requirementAccessRejected) != "") { 
				$(".req-access-denied-import", panel).text(response.requirementAccessRejected);
				reqAccessDeniedDialog.show(); 
				} else { reqAccessDeniedDialog.hide(); }
			
			var requirementNotFoundDialog = $(".import-links-excel-dialog-req-not-found", panel);
			if ($.trim(response.requirementNotFound) != "") { 
				$(".req-not-found-import", panel).text(response.requirementNotFound);
				requirementNotFoundDialog.show(); 
				} else { requirementNotFoundDialog.hide(); }
			
			var testCaseAccessRejectedDialog = $(".import-links-excel-dialog-tc-access-denied", panel);
			if ($.trim(response.testCaseAccessRejected) != "") { 
				$(".tc-access-denied-import", panel).text(response.testCaseAccessRejected);
				testCaseAccessRejectedDialog.show(); 
				} else { testCaseAccessRejectedDialog.hide(); }
			
			var testCaseNotFoundDialog = $(".import-links-excel-dialog-tc-not-found", panel);
			if ($.trim(response.testCaseNotFound) != "") { 
				$(".tc-not-found-import", panel).text(response.testCaseNotFound);
				testCaseNotFoundDialog.show(); 
				} else { testCaseNotFoundDialog.hide(); }
			
			var versionNotFoundDialog = $(".import-links-excel-dialog-version-not-found", panel);
			if ($.trim(response.versionNotFound) != "") { 
				$(".version-not-found-import", panel).text(response.versionNotFound);
				versionNotFoundDialog.show(); 
				} else { versionNotFoundDialog.hide(); }
			
			var linkAlreadyExistDialog = $(".import-links-excel-dialog-link-already-exist", panel);
			if ($.trim(response.linkAlreadyExist) != "") { 
				$(".link-already-exist-import", panel).text(response.linkAlreadyExist);
				linkAlreadyExistDialog.show(); 
				} else { linkAlreadyExistDialog.hide(); }
			
			if(eval("typeof " + "refreshVerifyingTestCases"+ " == 'function'")){
				refreshVerifyingTestCases();
			}else{
				if(eval("typeof " + "refreshVerifiedRequirements"+ " == 'function'")){
					refreshVerifiedRequirements();
				}
			}
		}
		
	}
	
	function importHandleErrors(json){
		//handling max size errors;
		if ('maxSize' in json){
			var errorMessage = "<f:message key='dialog.import.error.sizeexceeded'/>"
			var size = json.maxSize / 1000000;
			
			return errorMessage+size.toFixed(3)+' <f:message key="squashtm.megabyte.label"/>';
		}else{
			return null;
		}
	}
	
	
	
	$(function(){		
		
		var settings = {
				
			popup : $("#import-links-excel-dialog"),
			
			parametrization : {
				submitUrl : "${importUrl}",
				extensions : [ ${importFormatParam} ],
				errorMessage : "${wrongFileMessage} ${importFormatText}"
			},
			
			summary : {
				builder : importSummaryBuilder
			}, 
			
			errorHandler : importHandleErrors
				
		};
		
		importLinksExcelFeedbackPopup = new FeedbackMultipartPopup(settings);
		
		
		${treeNodeButton}.click(function(){
			$('#import-links-excel-dialog').dialog('open');
			return false;		
		});		
	});
	
	
</script>
