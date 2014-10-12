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
<%@ tag body-content="empty"%>
<%@ attribute name="requirementVersion" required="true" type="java.lang.Object" rtexprvalue="true"%>
<%@ attribute name="jsonCriticalities" required="true" rtexprvalue="true"%>
<%@ attribute name="jsonCategories" required="true" rtexprvalue="true"%>
<%@ attribute name="verifyingTestCaseModel" required="true" rtexprvalue="true" type="java.lang.Object"%>



<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="reqs" tagdir="/WEB-INF/tags/requirements-components"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform"%>

<s:url var="requirementUrl" value="/requirement-versions/${ requirementVersion.id }" />
<s:url var="pageUrl" value="/requirement-versions/" />

<s:url var="verifyingTCManagerUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases/manager" />
<s:url var="getStatusComboContent" value="/requirement-versions/${ requirementVersion.id }/next-status" />
<s:url var="customFieldsValuesURL" value="/custom-fields/values" />

<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel"/>
<f:message var="okLabel" key="label.Ok"/>
<f:message var="DefaultStatusNotAllowedMessage" key='requirement.status.notAllowed.default' />
<f:message var="ApprovedStatusNotAllowedMessage" key='requirement.status.notAllowed.approved' />

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
  <div style="float: left; height: 100%;">
    <h2>

      <c:set var="completeRequirementName" value="${ requirementVersion.name }" />
      <c:if test="${ not empty requirementVersion.reference && fn:length(requirementVersion.reference) > 0 }">
        <c:set var="completeRequirementName" value='${ requirementVersion.reference } - ${ requirementVersion.name }' />
      </c:if>
      <a id="requirement-name" href="${ requirementUrl }/info">
        <c:out value="${ completeRequirementName }" />
      </a>
      <%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
      <span id="requirement-raw-reference" style="display: none">
        <c:out value="${ requirementVersion.reference }" />
      </span>
      <span id="requirement-raw-name" style="display: none">
        <c:out value="${ requirementVersion.name }" />
      </span>
      <span id="requirement-id" style="display: none">
        <c:out value="${ requirementVersion.id }" />
      </span>
    </h2>
  </div>
  <div class="unsnap"></div>
</div>
<%-- ----------------------------------- /header ----------------------------------------------%>
<%-- ----------------------------------- toolbar ----------------------------------------------%>
<div id="requirement-toolbar" class="toolbar-class ui-corner-all">
  <div class="toolbar-information-panel">
    <comp:general-information-panel auditableEntity="${ requirementVersion }" entityUrl="${ requirementUrl }" />
  </div>


  <div class="toolbar-button-panel">
    <c:if test="${ writable }">
      <input type="button" value='<f:message key="requirement.button.rename.label" />' id="rename-requirement-button"
        class="sq-btn" />
    </c:if>
    <input type="button" value="<f:message key='label.print'/>" id="print-requirement-version-button" class="sq-btn" />
  </div>


  <div class="unsnap"></div>
</div>
<%-- ----------------------------------- /toolbar ----------------------------------------------%>
<%-- -------------------------------------------------------- TABS-----------------------------------------------------------%>
<csst:jq-tab>
  <div class="fragment-tabs fragment-body">
    <ul class="tab-menu">
      <li>
        <a href="#tabs-1">
          <f:message key="tabs.label.information" />
        </a>
      </li>
      <li>
        <a href="#tabs-2">
          <f:message key="label.Attachments" />
          <c:if test="${ requirementVersion.attachmentList.notEmpty }">
            <span class="hasAttach">!</span>
          </c:if>
        </a>
      </li>
    </ul>
    <%-- --------------------------------------------- tab1 Information----------------------------------------------%>
    <div id="tabs-1">
      <c:if test="${ writable }">
        <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${requirementUrl}'"/>
        <c:set var="referenceEditableAttributes" value="class='editable text-editable' data-def='url=${requirementUrl}, maxlength=50, callback=squashtm.requirement.updateReferenceInTitle'" />  
      </c:if>
<f:message var="requirementInformationPanelLabel" key="requirement.panel.general-informations.title" />


      <comp:toggle-panel id="requirement-information-panel" 	   title=  '${requirementInformationPanelLabel} <span class="small discret">[ID = ${requirementVersion.requirement.id }]</span>'
        open="true">
        <jsp:attribute name="body">
			<div id="edit-requirement-table" class="display-table">
				<div>
					<label for="requirement-version-number">
                <f:message key="requirement-version.version-number.label" />
              </label>
					<div id="requirement-version-number">${ requirementVersion.versionNumber }</div>
				</div>
	
				
				<div>
					<label for="requirement-reference">
                <f:message key="label.Reference" />
              </label>
					<div id="requirement-reference" ${referenceEditableAttributes}>${ requirementVersion.reference }</div>
				</div>
				<div>
					<label for="requirement-criticality">
                <f:message key="requirement.criticality.combo.label" />
              </label>
					<div>
						<div id="requirement-criticality">
							<c:choose>
								<c:when test="${ writable }">
									<comp:level-message level="${ requirementVersion.criticality }" />
									<comp:select-jeditable componentId="requirement-criticality" jsonData="${ jsonCriticalities }"
                        targetUrl="${ requirementUrl }" />
								</c:when>
							<c:otherwise>
								<comp:level-message level="${ requirementVersion.criticality }" />
							</c:otherwise>
							</c:choose>
						</div>
					</div>				
				</div>
				<div>
					<label for="requirement-category">
                <f:message key="requirement.category.combo.label" />
              </label>
					<div>
						<div id="requirement-category">
							<c:choose>
								<c:when test="${ writable }">
									<s:message code="${ requirementVersion.category.i18nKey }" htmlEscape="true" />
									<comp:select-jeditable componentId="requirement-category" jsonData="${ jsonCategories }"
                        targetUrl="${ requirementUrl }" />
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
				<div >
					<div id="requirement-status"><comp:level-message level="${ requirementVersion.status }"/></div>
				</div>

				</div>				
			</div>
		</jsp:attribute>
      </comp:toggle-panel>

      <comp:toggle-panel id="requirement-description-panel" titleKey="label.Description" open="true">
        <jsp:attribute name="body">	
					<div id="requirement-description" ${descrRicheditAttributes}>${ requirementVersion.description }</div>
		</jsp:attribute>
      </comp:toggle-panel>
      <%--------------- verifying TestCase section ------------------------------------%>
      <comp:toggle-panel id="verifying-test-case-panel" titleKey="requirement.verifying_test-case.panel.title"
        open="true">
        <jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">
				<f:message var="associateLabel" key="requirement.verifying_test-case.manage.button.label" />
				<f:message var="removeLabel" key="label.removeRequirementsAssociation" />
				
				<input id="verifying-test-case-button" type="button" class="sq-btn" value="${ associateLabel }" />
				<input id="remove-verifying-test-case-button" type="button" class="sq-btn" value="${ removeLabel }" />
			</c:if>
		</jsp:attribute>

        <jsp:attribute name="body">
			<reqs:verifying-test-cases-table batchRemoveButtonId="remove-verifying-test-case-button"
            editable="${ linkable }" model="${verifyingTestCaseModel}" requirementVersion="${requirementVersion}" />
		</jsp:attribute>
      </comp:toggle-panel>

      <%--------------- Audit Trail ------------------------------------%>
      <reqs:requirement-version-audit-trail requirementVersion="${ requirementVersion }" tableModel="${auditTrailModel}" />
      
    </div>
    <%-- --------------------------------------------- /tab1 Information----------------------------------------------%>
    <%-- --------------------------------------------- tab2 Attachments ----------------------------------------------%>
    <at:attachment-tab tabId="tabs-2" entity="${ requirementVersion }" editable="${ attachable }"
      tableModel="${attachmentsModel}" />
    <%-- --------------------------------------------- /tab2 Attachments ----------------------------------------------%>

  </div>
</csst:jq-tab>
<%-- --------------------------------------------------------------- /TABS ------------------------------------------------------------%>

<!------------------------------------------ POPUPS ------------------------------------------------------>
<%------------------- confirm new status if set to obsolete ---------------------%>
<c:if test="${ editableStatus }">
      <f:message var="statusChangeDialogTitle" key="dialog.requirement.status.confirm.title"/>
      <div id="requirement-status-confirm-dialog" class="not-displayed"
            title="${statusChangeDialogTitle}">
            
            <span><f:message key="dialog.requirement.status.confirm.text"/></span>
            
            <div class="popup-dialog-buttonpane">
              <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
              <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
            </div>
      </div>
</c:if>

  <%------------------- rename ---------------------%>
  <div>
  <c:if test="${writable}">
    <f:message var="renameDialogTitle" key="dialog.rename-requirement.title"/>
    <div  id="rename-requirement-dialog" class="not-displayed popup-dialog"
          title="${renameDialogTitle}">
    
        <label><f:message key="dialog.rename.label" /></label>
        <input type="text" id="rename-requirement-input" maxlength="255" size="50" /><br/>
        <comp:error-message forField="name"/>
    
    
        <div class="popup-dialog-buttonpane">
          <input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>        
        </div>
    
    </div>

</c:if>
</div>
<!------------------------------------------/ POPUPS ------------------------------------------------------>
<!------------------------------------------ SCRIPTS ------------------------------------------------------>

<script type="text/javascript">
require( ["common"], function(){
	require( ["jquery"], function($){
var identity = { resid : ${requirementVersion.id}, restype : "requirements"  };

	$(function(){
		
		var identity = { obj_id : ${requirementVersion.id}, obj_restype : "requirements"  };

		require(["domReady", "require"], function(domReady, require){
			domReady(function(){
				require(["jquery", "squash.basicwidgets", "contextual-content-handlers", "workspace.event-bus", 
				         "custom-field-values", "squash.configmanager", "app/ws/squashtm.notification", 
				         "jquery.squash.formdialog" ], 
						function($, basic, contentHandlers, eventBus, cufvalues, confman, notification){
					
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
					
					
					// *********** status modification **********
				
					<c:if test="${editableStatus}">
					
					var statusChangeDialog = $("#requirement-status-confirm-dialog");
					statusChangeDialog.formDialog();
					
					var statusChangeSelect = $("#requirement-status"),
						statusSelectConf = confman.getJeditableSelect();
					
					// this function uses an interplay with the 
					// statusChangeDialog, see the "OBSOLETE" branch
					function submitOrConfirm(settings, widget){
						var selected = this.find('select').val(),
							cansubmit = true;
						
						// if disabled, tell the user and exit
						if (selected.search(/disabled.*/)!=-1){
							cansubmit = false;
							var msg = ("disabled.APPROVED" === selected ) ? 
										"${ApprovedStatusNotAllowedMessage}" : 
										"${DefaultStatusNotAllowedMessage}";
										
							notification.showError(msg);
							widget.reset();
						}
						else if ("OBSOLETE" == selected){
							
							cansubmit = false;
							
							var summoned = statusChangeDialog.data('summoned'),
								confirmed = statusChangeDialog.data('confirmed');
							
							if (summoned !== true){
								statusChangeDialog.data('summoned', true);
								statusChangeDialog.data('confirmed', false);
								statusChangeDialog.data('form', this);
								statusChangeDialog.formDialog('open');
							}
							else{
								cansubmit = confirmed;
								if (! confirmed){
									widget.reset();
								}
								// rearm the switch;
								statusChangeDialog.data('summoned', false);
							}
						}
						
						return cansubmit;
					}
					
					var finalStatusSelectConf = $.extend(true, statusSelectConf, 
						{
							loadurl : "${getStatusComboContent}",
							callback : function(){document.location.reload();},
							onsubmit : submitOrConfirm
						}	
					);
					
					statusChangeSelect.editable('${requirementUrl}', finalStatusSelectConf)
								.addClass('editable');
					
					
					statusChangeDialog.on('formdialogconfirm', function(){
						statusChangeDialog.formDialog('close');
						statusChangeDialog.data('confirmed', true);
						var form = statusChangeDialog.data('form');
						form.submit();
					});
					
					statusChangeDialog.on('formdialogcancel', function(){
						statusChangeDialog.formDialog('close');
						statusChangeDialog.data('confirmed', false);
						var form = statusChangeDialog.data('form');
						form.submit();				
					});
					
					</c:if>
					
					// ************ rename dialog****************
				
					
				var renameDialog = $("#rename-requirement-dialog");
				renameDialog.formDialog();
				
				renameDialog.on('formdialogconfirm', function(){
					var url = "${ pageUrl }" + $('#requirement-id').text();
						params = { newName : $("#rename-requirement-input").val() };
					
					$.ajax({
						url : url,
						type : 'POST',
						dataType : 'json',
						data : params
					}).success(function(json){
						renameDialog.formDialog('close');
						squashtm.requirementVersion.renameRequirementSuccess(json);
					});
					
				});
				
				renameDialog.on('formdialogcancel', function(){
					renameDialog.formDialog('close');
				});
				
				renameDialog.on('formdialogopen', function(){
   					var name = $.trim($('#requirement-raw-name').text());
					$("#rename-requirement-input").val(name);
				});
				
				$("#rename-requirement-button").on('click', function(){
					renameDialog.formDialog('open');
				});
        		
        		$("#verifying-test-case-button").click(function(){
        			document.location.href="${ verifyingTCManagerUrl }" ;	
        		});
        		
        		
        		<c:if test="${hasCUF}">
                    <c:choose>
                    <c:when test="${writable}">
                    <c:set var="mode" value="jeditable"/>
                    </c:when>
                    <c:otherwise >
                    <c:set var="mode" value="static"/>
                    </c:otherwise>
                   </c:choose>
            		<%-- loading the custom fields --%>
            		$.getJSON("${customFieldsValuesURL}?boundEntityId=${requirementVersion.boundEntityId}&boundEntityType=${requirementVersion.boundEntityType}")
            		.success(function(jsonCufs){	
            			cufvalues.infoSupport.init("#edit-requirement-table", jsonCufs, "${mode}");
            		});
            	</c:if>

				});
			});
		});

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