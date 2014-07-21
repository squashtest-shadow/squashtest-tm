<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag body-content="empty" %>
<%@ attribute name="requirementVersion" required="true" type="java.lang.Object" rtexprvalue="true" %>
<%@ attribute name="jsonCriticalities" required="true" rtexprvalue="true" %>
<%@ attribute name="jsonCategories" required="true" rtexprvalue="true" %>
<%@ attribute name="verifyingTestCaseModel" required="true" rtexprvalue="true" type="java.lang.Object"%>



<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/input" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>

<s:url var="requirementUrl" value="/requirement-versions/${ requirementVersion.id }" />
<s:url var="pageUrl" value="/requirement-versions/" />


<s:url var="verifyingTCManagerUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases/manager" /> 

<s:url var="getStatusComboContent" value="/requirement-versions/${ requirementVersion.id }/next-status" />

<c:url var="customFieldsValuesURL" value="/custom-fields/values" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%-- that page won't be editable if 
   * the user don't have the correct permission,
   * the requirement status doesn't allow it. --%>
 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ requirementVersion }">
	<c:set var="attachable" value="${ requirementVersion.modifiable }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirementVersion }">
	<c:set var="writable" value="${ requirementVersion.modifiable }" />
	<c:set var="editableStatus" value="${ requirementVersion.status.allowsStatusUpdate }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ requirementVersion }">
	<c:set var="linkable" value="${ requirementVersion.linkable }" />
</authz:authorized>

<%-- ----------------------------------- /Authorization ----------------------------------------------%>
<%-- ----------------------------------- header ----------------------------------------------%>
<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float:left;height:100%;">	
		<h2>
			<span><f:message key="requirement-version.header.title" />&nbsp;:&nbsp;</span>
			<c:set var="completeRequirementName" value="${ requirementVersion.name }" />
			<c:if test="${ not empty requirementVersion.reference && fn:length(requirementVersion.reference) > 0 }" >
				<c:set var="completeRequirementName" value='${ requirementVersion.reference } - ${ requirementVersion.name }' />
			</c:if>
			<a id="requirement-name" href="${ requirementUrl }/info"><c:out value="${ completeRequirementName }" /></a>
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="requirement-raw-reference" style="display:none"><c:out value="${ requirementVersion.reference }" /></span>
			<span id="requirement-raw-name" style="display:none"><c:out value="${ requirementVersion.name }" /></span>
			<span id="requirement-id" style="display:none"><c:out value="${ requirementVersion.id }" /></span>
		</h2>
	</div>
	<div class="unsnap"></div>		
</div>
<%-- ----------------------------------- /header ----------------------------------------------%>
<%-- ----------------------------------- toolbar ----------------------------------------------%>
<div id="requirement-toolbar" class="toolbar-class ui-corner-all" >
	<div  class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${ requirementVersion }" entityUrl="${ requirementUrl }" />
	</div>


	<div class="toolbar-button-panel">
		<c:if test="${ writable }">
			<input type="button" value='<f:message key="requirement.button.rename.label" />' id="rename-requirement-button" class="sq-btn" />
		</c:if>
		<input type="button" value="<f:message key='label.print'/>"
			id="print-requirement-version-button" class="sq-btn" />
	</div>


	<div class="unsnap"></div>			
</div>
<%-- ----------------------------------- /toolbar ----------------------------------------------%>
<%-- -------------------------------------------------------- TABS-----------------------------------------------------------%>
<csst:jq-tab>
<div class="fragment-tabs fragment-body">
	<ul class="tab-menu">
		<li><a href="#tabs-1"><f:message key="tabs.label.information" /></a></li>
		<li><a href="#tabs-2"><f:message key="label.Attachments" />
		<c:if test="${ requirementVersion.attachmentList.notEmpty }"><span class="hasAttach">!</span></c:if>
		</a></li>
	</ul>
<%-- --------------------------------------------- tab1 Information----------------------------------------------%>
	<div id="tabs-1">
	<c:if test="${ writable }">
		<comp:rich-jeditable targetUrl="${ requirementUrl }" componentId="requirement-description" />
		<%-- make requirement-reference editable --%>
		<%-- TODO put at end of page, maybe componentize --%>
		<comp:simple-jeditable targetUrl="${ requirementUrl }" componentId="requirement-reference" submitCallback="squashtm.requirementVersion.updateReferenceInTitle" maxLength="50" />
	</c:if>

	<comp:toggle-panel id="requirement-information-panel" titleKey="requirement.panel.general-informations.title" open="true" >
		<jsp:attribute name="body">
			<div id="edit-requirement-table" class="display-table">
				<div>
					<label for="requirement-version-number"><f:message key="requirement-version.version-number.label" /></label>
					<div id="requirement-version-number">${ requirementVersion.versionNumber }</div>
				</div>
				<div class="display-table-row">
					<label for="requirement-version-id">ID</label>
					<div id="requirement-version-id">${ requirementVersion.requirement.id }</div>
				</div>
				
				<div>
					<label for="requirement-reference"><f:message key="label.Reference" /></label>
					<div id="requirement-reference">${ requirementVersion.reference }</div>
				</div>
				<div>
					<label for="requirement-criticality"><f:message key="requirement.criticality.combo.label" /></label>
					<div>
						<div id="requirement-criticality">
							<c:choose>
								<c:when test="${ writable }">
									<comp:level-message level="${ requirementVersion.criticality }"/>
									<comp:select-jeditable componentId="requirement-criticality" jsonData="${ jsonCriticalities }" targetUrl="${ requirementUrl }" />
								</c:when>
							<c:otherwise>
								<comp:level-message level="${ requirementVersion.criticality }"/>
							</c:otherwise>
							</c:choose>
						</div>
					</div>				
				</div>
				<div>
					<label for="requirement-category"><f:message key="requirement.category.combo.label" /></label>
					<div>
						<div id="requirement-category">
							<c:choose>
								<c:when test="${ writable }">
									<s:message code="${ requirementVersion.category.i18nKey }" htmlEscape="true" />
									<comp:select-jeditable componentId="requirement-category" jsonData="${ jsonCategories }" targetUrl="${ requirementUrl }" />
								</c:when>
							<c:otherwise>
								<s:message code="${ requirementVersion.category.i18nKey }" htmlEscape="true" />
							</c:otherwise>
							</c:choose>
						</div>
					</div>				
				</div>
				<div>
					<label for="requirement-status"><f:message key="requirement.status.combo.label" /></label>
					<div>
						<div id="requirement-status">
						<c:choose>
							<c:when test="${ editableStatus }">
								<comp:level-message level="${ requirementVersion.status }" />
								<comp:select-jeditable componentId="requirement-status" jsonUrl="${ getStatusComboContent }" 
														targetUrl="${ requirementUrl }"	
														onSubmit="statusSelect" submitCallback="statusSelectCallback" />
							</c:when>
							<c:otherwise>
								<comp:level-message level="${ requirementVersion.status }"/>
							</c:otherwise>
						</c:choose>
						</div>
					</div>		

				</div>				
			</div>
		</jsp:attribute>
	</comp:toggle-panel>

	<comp:toggle-panel id="requirement-description-panel" titleKey="label.Description" open="true" >
		<jsp:attribute name="body">	
					<div id="requirement-description">${ requirementVersion.description }</div>
		</jsp:attribute>
	</comp:toggle-panel>
	<%--------------- verifying TestCase section ------------------------------------%>
	<comp:toggle-panel id="verifying-test-case-panel" titleKey="requirement.verifying_test-case.panel.title" open="true">
		<jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">
				<f:message var="associateLabel" key="requirement.verifying_test-case.manage.button.label"/>
				<f:message var="removeLabel" key="label.removeRequirementsAssociation"/>
				
				<input id="verifying-test-case-button" type="button" class="sq-btn" value="${ associateLabel }"/>
				<input id="remove-verifying-test-case-button" type="button" class="sq-btn" value="${ removeLabel }"/>
			</c:if>
		</jsp:attribute>

		<jsp:attribute name="body">
			<aggr:decorate-verifying-test-cases-table batchRemoveButtonId="remove-verifying-test-case-button"
				editable="${ linkable }"  model="${verifyingTestCaseModel}" requirementVersion="${requirementVersion}"/>
		</jsp:attribute>
	</comp:toggle-panel>
	
	<%--------------- Audit Trail ------------------------------------%>
	<aggr:requirement-version-audit-trail requirementVersion="${ requirementVersion }" tableModel="${auditTrailModel}" />
</div>
<%-- --------------------------------------------- /tab1 Information----------------------------------------------%>
<%-- --------------------------------------------- tab2 Attachments ----------------------------------------------%>
	<at:attachment-tab tabId="tabs-2" entity="${ requirementVersion }" editable="${ attachable }" tableModel="${attachmentsModel}" />
<%-- --------------------------------------------- /tab2 Attachments ----------------------------------------------%>
	
</div>
</csst:jq-tab>
<%-- --------------------------------------------------------------- /TABS ------------------------------------------------------------%>

<!------------------------------------------ POPUPS ------------------------------------------------------>
	<%------------------- confirm new status if set to obsolete ---------------------%>
	<c:if test="${ editableStatus }">
	<pop:popup id="requirement-status-confirm-dialog" closeOnSuccess="false" titleKey="dialog.requirement.status.confirm.title" isContextual="true" >
		<jsp:attribute name="buttons">
			<f:message var="confirmLabel" key="label.Confirm" />
			<f:message var="cancelLabel" key="label.Cancel"/>
				'${ confirmLabel }' : function(){
					var jqDiag = $(this);
					jqDiag.dialog( 'close' );
					jqDiag.data("confirm", true);
					var form = jqDiag.data('callMeBack');
					form.submit();
				},
				
				'${ cancelLabel }': function() {
					var jqDiag = $(this);
					jqDiag.dialog( 'close' );
					jqDiag.data("confirm", false);
					var form = jqDiag.data('callMeBack');
					form.submit();
				}
		</jsp:attribute>
		<jsp:attribute name="body">
			<span><f:message key="dialog.requirement.status.confirm.text"/></span>
		</jsp:attribute>					
	</pop:popup>
		<%------------------- rename ---------------------%>
		<pop:popup id="rename-requirement-dialog" titleKey="dialog.rename-requirement.title" 
			isContextual="true" openedBy="rename-requirement-button">
			<jsp:attribute name="buttons">
				<f:message var="label" key="dialog.rename-requirement.title" />
				'${ label }': function() {
					var url = "${ pageUrl }" + $('#requirement-id').text();
					<jq:ajaxcall  url="url" dataType="json" httpMethod="POST" useData="true" successHandler="squashtm.requirementVersion.renameRequirementSuccess">		
						<jq:params-bindings newName="#rename-requirement-input" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="body">
				<label><f:message key="dialog.rename.label" /></label>
				<input type="text" id="rename-requirement-input" maxlength="255" /><br/>
				<comp:error-message forField="name"/>
			</jsp:attribute>
		</pop:popup>
	</c:if>
<!------------------------------------------/ POPUPS ------------------------------------------------------>
<!------------------------------------------ SCRIPTS ------------------------------------------------------>
<%-- ----------------------------------- Init ----------------------------------------------%>
<%-- 
	Code managing the status of a requirement. It is a handler for the 'onsubmit' of a jeditable (see documentation for details).

	It will ask the user, if he chooses to change the requirement status to 'Obsolete', to confirm.
	Because a jQuery dialog will not stop the execution of javascript we have to code in an unnatural way. 
	Basically this function will be called twice : first to invoked the dialog, second to read its response.	
	
	Here is how it works : 
	
		- if any status but 'obsolete' is selected, return true.
		- if 'obsolete' is selected and 'summoned' is false, sets 'summoned' to true, summons the dialog and return false.
		- if 'obsolete' is selected and 'summoned' is true, sets 'summoned' to false and read 'confirm' :
			* 'confirm' is false : reset the widget then return false.
			* 'confirm' is true : send the information then return true
		
	note : Since summoning a dialog will not stop the rest of the code from executing, the second case returns false to ensure that the handler terminates quickly while the dialog
	is still executed.
	
	'summoned' and 'confirm' are attributes of the dialog #requirement-status-confirm-dialog. When summoned the dialog will set 'confirm' 
	according to the user response and submit the select again. This will in turn call that hook again, which will eventually read the user response and then only decide whether 
	the user input will be sent or not.
	
	See also the code in #requirement-status-confirm-dialog for details. 
--%>

<c:if test="${ editableStatus }">
<f:message var="StatusNotAllowedMessage" key='requirement.status.notAllowed' />
<script type="text/javascript">
		function statusSelect(settings, widget){
			
			//first check if 'obsolete' is selected
			var selected = $(this.find('select')).val();
			
			var toReturn = true;
			
			if (isDisabled(selected)){
				toReturn=false;
				$.squash.openMessage("<f:message key='popup.title.info' />", "${ StatusNotAllowedMessage }");
			}
			else if ("OBSOLETE" == selected) {
				var jqDialog = $('#requirement-status-confirm-dialog');
				var summoned = jqDialog.data('summoned');
				
				if (! summoned) {
					statusObsoleteSummonDialog(this, jqDialog);
					toReturn=false;
				} else {	
					jqDialog.data('summoned', false);
					toReturn = statusObsoleteReadDialog(widget, jqDialog);			
				}
			} else {
				toReturn = true;
			}
			
			return toReturn;
		}
			
		function isDisabled(selected){
			return (selected.search(new RegExp("disabled.*"))!=-1);
		}
		
		function statusObsoleteSummonDialog(form, jqDialog){
			jqDialog.data('summoned', true);
			jqDialog.data('callMeBack', form);
			jqDialog.dialog('open');			
		}
		
		//reset the 'summoned' flag and the widget if needed
		function statusObsoleteReadDialog(widget, jqDialog){
			var response = jqDialog.data('confirm');
			if (false==response) {
				$(widget).html(widget.revert);
				widget.editing  = false;	
			}	
			return response;
		}
		
		function statusSelectCallback(){
			document.location.reload();
		}
</script>
</c:if>
<%-- ----------------------------------- Other ----------------------------------------------%>
<script type="text/javascript">
require( ["common"], function(){
	require( ["jquery"], function($){
var identity = { resid : ${requirementVersion.id}, restype : "requirements"  };

	$(function(){
		
		var identity = { obj_id : ${requirementVersion.id}, obj_restype : "requirements"  };

		require(["domReady", "require"], function(domReady, require){
			domReady(function(){
				require(["jquery", "squash.basicwidgets", "contextual-content-handlers", "workspace.event-bus", 
				         "custom-field-values"], 
						function($, basic, contentHandlers, eventBus, cufvalues){
					
					basic.init();
					
					var nameHandler = contentHandlers.getNameAndReferenceHandler();
					
					nameHandler.identity = identity;
					nameHandler.nameDisplay = "#requirement-name";
					nameHandler.nameHidden = "#requirement-raw-name";
					nameHandler.referenceHidden = "#requirement-raw-reference";
					
					$("#print-requirement-version-button").click(function(){
						window.open("${requirementUrl}?format=printable", "_blank");
					});
					
					//****** tabs configuration *******
					
					$('.fragment-tabs').tabs();
					
				});
			});
		});
		
		
		
		$( "#rename-requirement-dialog" ).bind( "dialogopen", function(event, ui) {
			var name = $('#requirement-raw-name').text();
			$("#rename-requirement-input").val(name);
		});
		
		$("#verifying-test-case-button").click(function(){
			document.location.href="${ verifyingTCManagerUrl }" ;	
		});
		
		
		<c:if test="${hasCUF}">
		<%-- loading the custom fields --%>
		$.getJSON("${customFieldsValuesURL}?boundEntityId=${requirement.currentVersion.boundEntityId}&boundEntityType=${requirement.currentVersion.boundEntityType}")
		.success(function(jsonCufs){	
			var mode = <c:out value="${writable ? 'jeditable' : 'static'}"/>;
			cufvalues.infoSupport.init("#edit-requirement-table", jsonCufs, mode);
		});
    	</c:if>
		    	
		

	});
	   squashtm = squashtm || {};
      squashtm.requirementVersion = squashtm.requirementVersion || {} 

	<c:if test="${ writable }">
	function renameRequirementSuccess(data){
		squashtm.workspace.eventBus.trigger('node.rename', {identity : identity, newName : data.newName});
		
	};	
	 squashtm.requirementVersion.renameRequirementSuccess = renameRequirementSuccess;
     
	function updateReferenceInTitle(newRef){
		squashtm.workspace.eventBus.trigger('node.update-reference', {identity : identity, newName : newRef});	
	};
	squashtm.requirementVersion.updateReferenceInTitle = updateReferenceInTitle;
     
	</c:if>
	

	});
});

	
</script>
<!------------------------------------------ /SCRIPTS ------------------------------------------------------>