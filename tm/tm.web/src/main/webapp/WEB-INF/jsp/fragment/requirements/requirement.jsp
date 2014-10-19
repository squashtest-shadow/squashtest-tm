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


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="reqs" tagdir="/WEB-INF/tags/requirements-components" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<s:url var="requirementUrl" value="/requirements/${requirement.id}"/>
<c:url var="attachmentsUrl" value="/attach-list/${requirement.currentVersion.attachmentList.id}/attachments" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%-- 
that page won't be editable if 
   * the user don't have the correct permission,
   * the requirement status doesn't allow it.

 --%>
 
 <c:set var="attachable"        value="${false}"/> 
 <c:set var="moreThanReadOnly"  value="${false}"/> 
 <c:set var="writable"          value="${false}"/> 
 <c:set var="deletable"         value="${false}"/> 
 <c:set var="creatable"         value="${false}"/> 
 <c:set var="linkable"          value="${false}"/> 
 <c:set var="status_editable"   value="${false}"/>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ requirement }">
	<c:set var="attachable" value="${ requirement.modifiable }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
	<c:set var="writable" value="${ requirement.modifiable }"/>
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ requirement }">
	<c:set var="deletable" value="${true}"/>
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ requirement }">
	<c:set var="creatable" value="${true }"/>
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
	<c:set var="linkable" value="${ requirement.linkable }" />
		<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<c:set var="status_editable" value="${ moreThanReadOnly and requirement.status.allowsStatusUpdate }"/>


<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel"/>
<f:message var="okLabel" key="label.Ok"/>



<script type="text/javascript">
	requirejs.config({
		config : {
			'requirement-page' : {
				basic : {
					'edited-entity-type' : 'requirement',
					identity : { resid : ${requirement.id}, restype : "requirements"  },
					requirementId : ${requirement.id},
					currentVersionId : ${requirement.currentVersion.id},
					criticalities : ${json:serialize(criticalityList)},
					categories : ${json:serialize(categoryList)},
					verifyingTestcases : ${json:serialize(verifyingTestCasesModel.aaData)},
					attachments : ${json:serialize(attachmentsModel.aaData)},
					audittrail : ${json:serialize(auditTrailModel.aaData)},
					hasCufs : ${hasCUF}
				},
				permissions : {
					moreThanReadOnly : ${moreThanReadOnly},
					attachable : ${attachable},
					writable : ${writable},
					deletable : ${deletable},
					creatable : ${creatable},
					linkable : ${linkable},
					status_editable : ${status_editable}
				},
				urls : {
					baseURL : "${requirementUrl}",
					attachmentsURL : "${attachmentsUrl}"
				}
			}
		}
	});
	
	require(['common'], function(){
		require(['requirement-page'], function(){});
	});

</script>

<%-- ----------------------------------- TITLE ----------------------------------------------%>
<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float:left;height:100%;" class="small-margin-left">	
		<h2>
		
			<c:set var="completeRequirementName" value="${ requirement.name }" />
			<c:if test="${not empty requirement.reference && fn:length(requirement.reference) > 0}" >
				<c:set var="completeRequirementName" value='${ requirement.reference } - ${ requirement.name }' />
			</c:if>
			<a id="requirement-name" href="${ requirementUrl }/info"><c:out value="${ completeRequirementName }" escapeXml="true"/></a>
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="requirement-raw-reference" style="display:none"><c:out value="${ requirement.reference }" escapeXml="true"/></span>
			<span id="requirement-raw-name" style="display:none"><c:out value="${ requirement.name }" escapeXml="true"/></span>
		</h2>
	</div>
	<div class="unsnap"></div>		
</div>
<%-- ----------------------------------- /TITLE ----------------------------------------------%>
<%-- ----------------------------------- AUDIT & TOOLBAR  ----------------------------------------------%>	
<div id="requirement-toolbar" class="toolbar-class ui-corner-all" >
	<div  class="toolbar-information-panel">
	<c:url var="currentVersionUrl" value="/requirement-versions/${requirement.currentVersion.id}" />
		<comp:general-information-panel auditableEntity="${ requirement.currentVersion }" entityUrl="${ currentVersionUrl }" />
	</div>

	<div class="toolbar-button-panel">
		<c:if test="${ writable }">
			<input type="button" value='<f:message key="requirement.button.rename.label" />' id="rename-requirement-button" class="sq-btn"/> 
		</c:if>
		<c:if test="${ creatable }">
			<input type="button" value='<f:message key="requirement.button.new-version.label" />' id="new-version-button" class="sq-btn"/>		
		</c:if>
		<input type="button" value="<f:message key='label.print'/>" id="print-requirement-version-button" class="sq-btn"/>
	</div>	

	<div class="unsnap"></div>	
	<c:if test="${ moreThanReadOnly	 }">
	<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ requirementUrl }" />
	</c:if>
</div>

<script type="text/javascript">
publish('reload.requirement.toolbar');
</script>

<%-- ----------------------------------- /AUDIT & TOOLBAR  ----------------------------------------------%>	
<%-- ----------------------------------- TABS  ----------------------------------------------%>	
<csst:jq-tab>
<div class="fragment-tabs fragment-body">
	<ul class="tab-menu">
		<li><a href="#tabs-1"><f:message key="tabs.label.information" /></a></li>
		<li><a href="#tabs-2"><f:message key="label.Attachments" />
		<c:if test="${ requirement.attachmentList.notEmpty }"><span class="hasAttach">!</span></c:if>
		</a></li>
	</ul>
	<%-- ----------------------------------- INFO TAB  ----------------------------------------------%>	
	<div id="tabs-1">
	
	<c:if test="${writable }">
        <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${requirementUrl}'"/>
	</c:if>
<%--------------------------- General Informations section ------------------------------------%>

	<f:message var="labelRequirementInfoPanel" key="requirement.panel.general-informations.title"  />
	<comp:toggle-panel id="requirement-information-panel"   title=  '${labelRequirementInfoPanel} <span class="small discret">[ID = ${ requirement.id }]</span>' open="true" >
		<jsp:attribute name="body">
			<div id="edit-requirement-table" class="display-table">
				<div class="display-table-row">
					<label for="requirement-version-number"><f:message key="requirement-version.version-number.label" /></label>
					<div class="display-table-cell" id="requirement-version-number">${ requirement.currentVersion.versionNumber }&nbsp;&nbsp;<a href="<c:url value='/requirements/${ requirement.id }/versions/manager' />"><f:message key="requirement.button.manage-versions.label" /></a></div>
				</div>
			
				
				<div class="display-table-row">
					<label class="display-table-cell"  for="requirement-reference"><f:message key="label.Reference" /></label>
					<div id="requirement-reference">${ requirement.reference }</div>
				</div>
				<div class="display-table-row">
					<label for="requirement-criticality" class="display-table-cell"><f:message key="requirement.criticality.combo.label" /></label>
					<div class="display-table-cell">
						<div id="requirement-criticality"><comp:level-message level="${ requirement.criticality }"/></div>
					</div>
				</div>
				<div class="display-table-row">
					<label for="requirement-category" class="display-table-cell"><f:message key="requirement.category.combo.label" /></label>
					<div class="display-table-cell">
						<div id="requirement-category"><s:message code="${ requirement.category.i18nKey }" htmlEscape="true" /></div>
					</div>				
				</div>
				<div class="display-table-row">
					<label for="requirement-status" class="display-table-cell"><f:message key="requirement.status.combo.label" /></label>
					<div class="display-table-cell">
						<div id="requirement-status"><comp:level-message level="${ requirement.status }"/></div>
					</div>

				</div>				
			</div>
		</jsp:attribute>
	</comp:toggle-panel>
  
  <script type="text/javascript">
  	publish('reload.requirement.generalinfo');
  </script>
  
	<%--------------------------- Description section------------------------------------%>
	<comp:toggle-panel id="requirement-description-panel" titleKey="label.Description" open="true" >
		<jsp:attribute name="body">	
					<div id="requirement-description" ${descrRicheditAttributes}>${ requirement.description }</div>
		</jsp:attribute>
	</comp:toggle-panel>

	<%--------------------------- verifying TestCase section ------------------------------------%>


	<comp:toggle-panel id="verifying-requirement-panel" titleKey="requirement.verifying_test-case.panel.title" open="true">
		<jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">
				<f:message var="associateLabel" key="requirement.verifying_test-case.manage.button.label"/>
				<f:message var="removeLabel" key="label.removeRequirementsAssociation"/>
				
				
				<input id="verifying-test-case-button" type="button" class="sq-btn" value="${ associateLabel }"/>
				<input id="remove-verifying-test-case-button" type="button" class="sq-btn" value="${ removeLabel }"/>
			</c:if>
		</jsp:attribute>

		<jsp:attribute name="body">
			<reqs:verifying-test-cases-table 
			batchRemoveButtonId="remove-verifying-test-case-button" requirementVersion="${requirement.currentVersion}" 
				editable="${ linkable }" model="${verifyingTestCasesModel}" autoJsInit="${false}"/>
		</jsp:attribute>
	</comp:toggle-panel>
  
  <script type="text/javascript">
  publish('reload.requirement.verifyingtestcases');
  </script>
	
	<reqs:requirement-version-audit-trail requirementVersion="${ requirement.currentVersion }" tableModel="${auditTrailModel}"/>
<script type="text/javascript">
publish('reload.requirement.audittrail');
</script>
</div>
<%-- ----------------------------------- /INFO TAB  ----------------------------------------------%>	
<%-- ----------------------------------- ATTACHMENT TAB  ----------------------------------------------%>
<at:attachment-tab tabId="tabs-2" entity="${ requirement }" editable="${ attachable }" tableModel="${attachmentsModel}" autoJsInit="${false}"/>
<script type="text/javascript">
publish('reload.requirement.attachments');
</script>
<%-- ----------------------------------- /ATTACHMENT TAB  ----------------------------------------------%>	
<%-- -------------------------------------------------------- /TABS  ----------------------------------------------%>	
</div>
</csst:jq-tab>
<%-- ----------------------------------------------------------- /CONTENT ----------------------------------------------%>
	
<%-- -----------------------------------POPUPS ----------------------------------------------%>
<%--------------------------- Rename popup -------------------------------------%>

<div class="not-displayed">
<c:if test="${ writable }">
		
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
<%--------------------------- New version popup -------------------------------------%>
<c:if test="${ creatable }">
	<f:message var="confirmNewVersionDialogTitle" key="requirement.new-version.confirm-dialog.title" />	
	<div id="confirm-new-version-dialog" class="not-displayed popup-dialog" title="${ confirmNewVersionDialogTitle }">
		<strong><f:message key="requirement.new-version.confirm-dialog.label" /></strong>
		<input type="button" value="${okLabel}" />
		<input type="button" value="${cancelLabel}" />
	</div>
  		
</c:if>	

<%------------------------------- confirm new status if set to obsolete popup---------------------%>
<c:if test="${status_editable}">
  
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
  
</div>
<script type="text/javascript">
publish('reload.requirement.popups');
</script>
<%-- -----------------------------------/POPUPS ----------------------------------------------%>

<script type="text/javascript">
publish('reload.requirement.complete');
</script>


