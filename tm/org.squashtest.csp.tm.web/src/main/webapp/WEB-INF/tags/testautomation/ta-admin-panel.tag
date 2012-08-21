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
<%@ tag description="test automation panel (project level)"
	body-content="empty"%>

<%@ attribute name="project" type="java.lang.Object" required="true"
	description="the TM Project"%>
<%@ attribute name="taServer" type="java.lang.Object" required="true"
	description="the TA server"%>
<%@ attribute name="boundProjects" type="java.lang.Object" required="true"
	description="the TA projects already bound"%>

<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>



<!--  ===================== DEV STYLESHEET, MOVE IT TO MAIN STYLESHEET WHEN READY ==================== -->

<link href="http://localhost/css/ta-admin-panel.css" rel="stylesheet"
	type="text/css">
	
	
<%--
equivalent to : 


div.ta-main-div .ta-block{
	margin-top	: 0.8em;
	margin-bottom : 0.8em;	
	padding-top	:	0.2em;
	padding-bottom : 0.2 em;
}

div.ta-main-div .ta-block-item{
	margin-top	:	0.1em;
	margin-bottom : 0.1em;
	margin-right:	0.1em;
}

div.ta-main-div .ta-server-block{
	display : table;
}

div.ta-main-div .ta-server-block .ta-block-item {
	display : table-row;
}

div.ta-main-div .ta-server-block .ta-block-item-unit{
	display : table-cell;
	padding	:	0.2em;	
}

div.ta-main-div  .ta-maincheck-div{
	padding-left : 2em;
}

div.ta-main-div .ta-maincheck-div input{
	vertical-align : middle;
}	


div.ta-main-div .ta-manager-disabled{
	background-color: lightgray;
	opacity: 0.4;	
	filter: alpha(opacity=40);		/* for IE8 */
}


 --%>	

<!--  ==================== /DEV STYLESHEET, MOVE IT TO MAIN STYLESHEET WHEN READY ==================== -->


<c:url var="listRemoteProjectsURL" 	value="/test-automation/servers/projects-list" />

<s:url var="projectUrl" value="/projects/{projectId}">
	<s:param name="projectId" value="${project.id}" />
</s:url>

<s:url var="localProjectsURL" value="/projects/{projectId}/test-automation-projects">
	<s:param name="projectId" value="${project.id}"/>
</s:url>


<c:set var="initialChecked" value="checked=\"checked\"" />
<c:set var="initialDisabled" value="" />
<c:set var="initialCss" value="" />


<c:if test="${not project.testAutomationEnabled}">
	<c:set var="initialChecked" value="" />
	<c:set var="initialDisabled" value="disabled=\"disabled\"" />
	<c:set var="initialCss" value="ta-manager-disabled" />
</c:if>

<c:set var="inputSize" value="50" />

<comp:toggle-panel id="test-automation-management-panel"
	titleKey="project.testauto.panel.title" isContextual="true" open="true">

	<jsp:attribute name="body">
		<div class="ta-main-div">
		
			<div class="ta-maincheck-div ta-block">
				<label><f:message key="project.testauto.maincheckbox" />
				</label><input type="checkbox" id="test-auto-enabled-ckbox" ${initialChecked} />
			</div>
			
			<fieldset class="ta-server-block  ta-block  ${initialCss}">
				<legend>
					<f:message key="project.testauto.serverblock.title" />
				</legend>
				<div class="ta-block-item">
					<div class="ta-block-item-unit">
						<label><f:message
								key="project.testauto.serverblock.url.label" />
						</label>
					</div>
					<div class="ta-block-item-unit">
						<input type="text" class="ta-serverblock-url-input"
							value="${taServer.baseURL}" size="${inputSize}" />
					</div>
				</div>
				<div class="ta-block-item">
					<div class="ta-block-item-unit">
						<label><f:message
								key="project.testauto.serverblock.login.label" />
						</label>
					</div>
					<div class="ta-block-item-unit">
						<input type="text" class="ta-serverblock-login-input"
							value="${taServer.login}" size="${inputSize}" />
					</div>
				</div>
				<div class="ta-block-item">
					<div class="ta-block-item-unit">
						<label><f:message
								key="project.testauto.serverblock.password.label" />
						</label>
					</div>
					<div class="ta-block-item-unit">
						<input type="password" class="ta-serverblock-password-input"
							value="${taServer.password}" size="${inputSize}" />
					</div>
				</div>
			</fieldset> 
			
			<fieldset class="ta-projects-block  ta-block">
				<legend>
					<f:message key="project.testauto.projectsblock.title" />
				</legend>
				
				<div class="ta-block-item">
					<f:message var="addTAProjectLabel" key="project.testauto.projectsblock.add.button.label" />
					<input id="ta-projectsblock-add-button" title="${addTAProjectLabel}" type="button" class="button" value="+"/>
				</div>
				
				<table id="ta-projects-table" class="ta-projects-table">
					<thead>
						<tr>
							<th>Id (masked)</th>
							<th>#</th>
							<th><f:message key="project.testauto.projectsblock.table.headers.name"/></th>
							<th><f:message key="project.testauto.projectsblock.table.headers.serverurl"/></th>
							<th><f:message key="project.testauto.projectsblock.table.headers.serverkind"/></th>
							<th>&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${boundProjects}" var="taproj" varStatus="status">
						<tr>
							<td>${taproj.id}</td>
							<td>${status.index}</td>
							<td>${taproj.name}</td>
							<td>${taproj.server.baseURL}</td>
							<td>${taproj.server.kind}</td>
							<td> </td>
						</tr>
						</c:forEach>
					</tbody>
				</table>
				<br />
	
		</fieldset>
	</div>
	</jsp:attribute>

</comp:toggle-panel>


<%-- ================================================
	Project add popup. 
	
	Dumb definition here, the code is elsewhere 
================================================= --%>

<pop:popup id="ta-projectsblock-popup" openedBy="ta-projectsblock-add-button" closeOnSuccess="false" 
							   usesRichEdit="false" titleKey="project.testauto.projectsblock.add.popup.title" >
							   
	<jsp:attribute name="buttonsArray">
		{
			'text' 	: '<f:message key="dialog.button.confirm.label" />',
			'class' : 'ta-projectsadd-popup-confirm'
		},
		{
			'text'  : '<f:message key="dialog.button.cancel.label" />',
			'class' : 'ta-projectsadd-popup-cancel'
		}											
	</jsp:attribute>
	
	<jsp:attribute name="body">
	
 		<div class="ta-projectsadd-pleasewait" style="vertical-align:middle;">
 			<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
			<span style="font-size:1.5em;"><f:message key="squashtm.processing"/></span>
 		</div>
	

		<div class="ta-projectsadd-fatalerror">
			<span> </span>
		</div>
		
		<div class="ta-projectsadd-error">
			<span> </span>
		</div>
			 	
	
		<div class="ta-projectsadd-maindiv">
			<label><f:message key="project.testauto.projectsblock.add.popup.caption"/></label>
			<div class="ta-projectsadd-listdiv">
				
			</div>
		</div>
		
	</jsp:attribute>
</pop:popup>



<%-- ===================================
	Js initialization
==================================== --%>

<script type="text/javascript">
	$(function(){
		
		//************************** manager setup ********************
		
		if (! squashtm.testautomation){
			squashtm.testautomation = {};
		}
		
		var managerSettings = {
			selector : "#test-automation-management-panel .ta-main-div",
			initiallyEnabled : ${project.testAutomationEnabled}					
		};
		
		squashtm.testautomation.projectmanager = new TestAutomationProjectManager(managerSettings);
		
		
		var popupSettings = {
			selector : "#ta-projectsblock-popup",
			manager : squashtm.testautomation.projectmanager,
			listProjectsURL : "${listRemoteProjectsURL}",
			bindProjectsURL : "${localProjectsURL}"
		}
		
		var projectAddPopup = new TestAutomationAddProjectPopup(popupSettings);
		
		//************************** table setup **********************
		
		var tableSettings = {
				"oLanguage":{
					"sLengthMenu": '<f:message key="generics.datatable.lengthMenu" />',
					"sZeroRecords": '<f:message key="generics.datatable.zeroRecords" />',
					"sInfo": '<f:message key="generics.datatable.info" />',
					"sInfoEmpty": '<f:message key="generics.datatable.infoEmpty" />',
					"sInfoFiltered": '<f:message key="generics.datatable.infoFiltered" />',
					"oPaginate":{
						"sFirst":    '<f:message key="generics.datatable.paginate.first" />',
						"sPrevious": '<f:message key="generics.datatable.paginate.previous" />',
						"sNext":     '<f:message key="generics.datatable.paginate.next" />',
						"sLast":     '<f:message key="generics.datatable.paginate.last" />'
					}
				},
				"iDeferLoading": ${fn:length(boundProjects)},
				"sAjaxSource": "${localProjectsURL}", 
				"aoColumnDefs":[
					{'bSortable': false, 'bVisible': false, 'aTargets': [0], 'mDataProp' : 'entity-id'},
					{'bSortable': false, 'bVisible': true,  'aTargets': [1], 'mDataProp' : 'entity-index', 'sClass' :'select-handle centered', 'sWidth' : '2em'},
					{'bSortable': false, 'bVisible': true,  'aTargets': [2], 'mDataProp' : 'name'},
					{'bSortable': false, 'bVisible': true,  'aTargets': [3], 'mDataProp' : 'server-url'},
					{'bSortable': false, 'bVisible': true,  'aTargets': [4], 'mDataProp' : 'server-kind'},
					{'bSortable': false, 'bVisible': true, 'aTargets': [5], 'mDataProp' : 'empty-delete-holder', 'sWidth': '2em', 'sClass': 'centered delete-button' }										
				]

			};
		
		var squashSettings = {
			enableHover : true,
			confirmPopup : {
				oklabel : '<f:message key="attachment.button.delete.label" />',
				cancellabel : '<f:message key="dialog.button.cancel.label" />'
			},
			enableDnD : false,
			deleteButtons : {
				url : "${projectUrl}/test-automation-projects/{entity-id}",
				popupmessage : '<f:message key="dialog.unbind-ta-project.message" />',
				tooltip : '<f:message key="dialog.unbind-ta-project.tooltip" />',
				success : function(){
					$("#ta-projects-table").squashTable().fnDraw();
				}
			}
		}
		
		$("#ta-projects-table").squashTable(tableSettings, squashSettings);

		
	});

</script>