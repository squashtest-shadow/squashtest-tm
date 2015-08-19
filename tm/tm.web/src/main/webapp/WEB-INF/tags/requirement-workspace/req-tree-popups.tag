<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%--
	Implicit arguments (must be in the context)
	
	- categories : a List<RequirementCategory>, sorted according to the specs.

 --%>
 
 <%@ attribute name="importableLibraries" required="true" description="the potential target libraries for upload." type="java.lang.Object" %>
 

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<f:message var="addFolderTitle"  		key="dialog.new-folder.title"/>		
<f:message var="addRequirementTitle"	key="dialog.new-requirement.title"/>	
<f:message var="renameNodeTitle"		key="dialog.rename-tree-node.title" />
<f:message var="deleteNodeTitle"		key="dialog.delete-tree-node.title"/>
<f:message var="importExcelTitle"		key="dialog.import-excel.title" />
<f:message var="importLinksTitle"		key="dialog.import-links-excel.title" />
<f:message var="exportLabel"			key="label.Export" />
<f:message var="addLabel"		 		key="label.Add"/>
<f:message var="addAnotherLabel"		key="label.addAnother"/>
<f:message var="addAnotherLabelFem"		key="label.fem.addAnother"/>
<f:message var="cancelLabel"			key="label.Cancel"/>
<f:message var="confirmLabel"			key="label.Confirm"/>
<f:message var="importLabel"			key="label.Import"/>
<f:message var="okLabel"				key="label.Ok"/>
<f:message var="exportLabel"			key="label.Export" />
<f:message var="dateexportFormat"		key="export.dateformat"/>
<f:message var="exportnamePrefix" 		key="label.lower.dash.exportRequirements" />


<div id="treepopups-definition" class="not-displayed">

<div id="add-folder-dialog" class="popup-dialog not-displayed" title="${addFolderTitle}">
	<table class="add-node-attributes">
		<tr>
			<td><label for="add-folder-name"><f:message key="label.Name" /></label></td>
			<td><input id="add-folder-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		<tr>
			<td><label for="add-foldder-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-folder-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabel}"   	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>


<div id="add-requirement-dialog" class="popup-dialog not-displayed" title="${addRequirementTitle}">
	<table class="add-node-attributes">
		
		<tr>
			<td><label for="add-requirement-name"><f:message key="label.Name" /></label></td>

			<td><input id="add-requirement-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		
		<tr>
			<td><label for="add-requirement-reference"><f:message key="label.Reference" /></label></td>
			<td><input id="add-requirement-reference" type=text size="20" maxlength="50"/><br />
				<comp:error-message forField="reference" />	<td>
		</tr>
		
		<tr>
			<td><label for="add-requirement-criticality"><f:message key="requirement.criticality.combo.label" /></label></td>
			<td>
				<s:eval expression="T(org.squashtest.tm.domain.requirement.RequirementCriticality).values()" var="criticalities"></s:eval>
				<select id="add-requirement-criticality">
				<c:forEach var="crit" items="${criticalities}">
					<option value="${crit.code}"><f:message key="${crit.i18nKey}"/></option>
				</c:forEach>
				</select>
			</td>
		</tr>
		
		<tr>
			<td><label for="add-requirement-category"><f:message key="requirement.category.combo.label" /></label></td>
			<td>
				<select id="add-requirement-category">
				<c:forEach var="cat" items="${categories}">
				<option value="${cat}"><f:message key="${cat.i18nKey}" /></option>				
				</c:forEach>
				</select>
			</td>
		</tr>		
					
		<tr>
			<td><label for="add-requirement-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-requirement-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabelFem}"	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>




<div id="rename-node-dialog" class="popup-dialog not-displayed" title="${renameNodeTitle}" >

	<span data-def="state=denied">
		<f:message key="dialog.label.rename-node.rejected" />		
	</span>
	
	<div data-def="state=confirm">
		<label for="rename-tree-node-text"><f:message key="dialog.rename.label" /></label>
		<input id="rename-tree-node-text" type="text" size="50" /> <br />
		<comp:error-message forField="name" />
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" 		data-def="evt=confirm, mainbtn=confirm, state=confirm"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel, mainbtn"/>
	</div>	

</div>


<div id="delete-node-dialog" class="popup-dialog not-displayed" title="${deleteNodeTitle}">
	
	<div data-def="state=pleasewait">	
 		<comp:waiting-pane/>	
	</div>
	
	<div class="not-displayed" data-def="state=confirm">
	
		<div class="display-table-row">
			<div class="display-table-cell warning-cell">
				<div class="delete-node-dialog-warning"></div>
			</div>
			<div class="display-table-cell">
				<p>
					<f:message key="dialog.label.delete-node.label.start" />
					<span class='red-warning-message'>
						<f:message key="dialog.label.delete-nodes.requirements.label"/>
					</span> 
					<f:message key="dialog.label.delete-node.label.end"/>
				</p>
				
				<div class="not-displayed delete-node-dialog-details">
					<p><f:message key="dialog.delete-tree-node.details"/></p>
					<ul>
					</ul>				
				</div>
				
				<p>
					<span>
						<f:message key="dialog.label.delete-node.label.cantbeundone" />
					</span>				
					<span class='bold-warning-message'>
						<f:message key="dialog.label.delete-node.label.confirm" />
					</span>				
				</p>				
				
			</div>
		</div>
	</div>
		
	<div class="not-displayed" data-def="state=rejected">
		<f:message key="dialog.label.delete-node.rejected"/>
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn=confirm, state=confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel,  mainbtn=rejected"/>
	</div>
</div>


<%-- ================ IMPORT EXCEL POPUP ======================= --%>

<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
 
<div id="import-excel-dialog" class="popup-dialog not-displayed" title="${importExcelTitle}">

	<div data-def="state=parametrization">
		
		<div style="margin-top:1em;margin-bottom:1em;">
			<form action="${servContext}/requirement-browser/import/upload" method="POST" enctype="multipart/form-data" class="display-table">

				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.import.project.message"/></label></div>
					<div class="display-table-cell">
						<select name="projectId">
							<c:forEach items="${importableLibraries}" var="lib" varStatus="status" >
							<%-- warning : c tag nested in another c tag --%>
							<option value="${lib.id}" <c:if test="${status.first}">selected="yes"</c:if>>${lib.project.name}</option>
							</c:forEach>
						</select>
					
					</div>
				</div>
				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.import-excel.requirement.filetype.message"/></label></div>
					<div class="display-table-cell">
						<input type="file" name="archive" size="20" accept="application/vnd.ms-excel" />
					</div>
				</div>
			</form>
		
		</div>
	</div>
	
	<div data-def="state=confirm">
		<div class="confirm-div">
			<label class="confirm-label"><f:message key="dialog.import.file.confirm"/></label>
			<span class="confirm-span confirm-file"></span>
		</div>
		<div class="confirm-div">
			<label class="confirm-label"><f:message key="dialog.import.project.confirm"/></label>
			<span class="confirm-span confirm-project"><c:out value="${importableLibraries[0].project.name}"/></span>
		</div>
		
		<span style="display:block"><f:message key="dialog.import.confirm.message"/></span>
	</div>
	
	<div data-def="state=progression">
		<comp:waiting-pane/>		
	</div>
	
	<div class="import-summary" data-def="state=summary">
	
		<div><span><f:message key="dialog.import-excel.requirement.total"/></span><span class="total-import span-bold"></span></div>
		
		<div><span><f:message key="dialog.import-excel.requirement.success"/></span><span class="success-import span-bold span-green"></span></div>
		
		<div><span><f:message key="dialog.import-excel.requirement.failed"/></span><span class="failures-import span-bold"></span></div>			
		
		<div class="import-excel-dialog-note">			
			<hr/>
			<span><f:message key="dialog.import.summary.notes.label"/></span>
			<ul>
				<li class="import-excel-dialog-renamed"><span><f:message key="dialog.import-excel.requirement.warnings.renamed"/></span></li>
			</ul>
		</div>
	</div>	

	<div data-def="state=error-size">
		<span class="error-size"><f:message key="dialog.import.error.sizeexceeded"/></span>
	</div>
	
	<div data-def="state=error-format">		
		<span><f:message key="dialog.import.wrongfile"/>xls, xlsx</span>	
	</div>
  
      
    <div class="error-type" data-def="state=error-type">    
      <span class="import-err-filetype-text" id="import-err-filetype-text-xls"><f:message key="dialog.import.wrongfile"/><span id="import-err-filetype">xls, xlsx</span></span> 
    </div>
  
	
	<div id="import-excel-dump" class="not-displayed dump"></div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${importLabel}"  data-def="evt=import, state=parametrization, mainbtn=parametrization"/>
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, state=confirm, mainbtn=confirm"/>
		<input type="button" value="${okLabel}"		 data-def="evt=ok, state=summary, mainbtn=summary"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrsize, state=error-size, mainbtn=error-size"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrformat, state=error-format error-type, mainbtn=error-format error-type"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel, state=parametrization confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel-progression, state=progression"/>
	</div>
	
</div>

<%-- ================ /IMPORT EXCEL POPUP ====================== --%>


<%-- ================ IMPORT LINK POPUP ====================== --%>

<div id="import-links-excel-dialog" class="popup-dialog not-displayed" title="${importLinksTitle}">

	<div data-def="state=parametrization">
		<div style="margin-top:1em;margin-bottom:1em;">
			<form action="${servContext}/requirement-version-coverage/upload" method="POST" enctype="multipart/form-data" class="display-table">
				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.import-links-excel.filetype.message"/></label></div>
					<div class="display-table-cell">
						<input type="file" name="file" size="20" accept="application/vnd.ms-excel" />
					</div>
				</div>
			</form>
		</div>
	</div>
	
	<div data-def="state=confirm">
		<div class="confirm-div">
			<label class="confirm-label"><f:message key="dialog.import.file.confirm"/></label>
			<span class="confirm-span confirm-file"></span>
		</div>
		<span style="display:block"><f:message key="dialog.import.confirm.message"/></span>
	</div>
	
	<div data-def="state=progression" >
		<comp:waiting-pane/>			
	</div>
	
		
	<div class="import-summary" data-def="state=summary">
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
			<div class="import-links-excel-dialog-normal-errors not-displayed">
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
			
			<div class="import-links-excel-dialog-critical-errors not-displayed">
				<span><f:message key="dialog.import.summary.errors.label"/><br></span>
				<ul  contenteditable="true" style="height:100px ; overflow : auto" >
					<li class="import-links-excel-dialog-missing-headers">
						<span><f:message key="dialog.import.summary.notes.missing-headers.label"/></span>
						<span class="file-missing-headers"></span>
					</li>					
				</ul>
			</div>
					
		</div>
	</div>	
	
			
	<div data-def="state=error-size">
		<span class="error-size"><f:message key="dialog.import.error.sizeexceeded"/></span>
	</div>
	
	<div data-def="state=error-format">		
		<span><f:message key="dialog.import.wrongfile"/>xls, xlsx</span>	
	</div>
  
    
    <div class="error-type" data-def="state=error-type">    
      <span class="import-err-filetype-text" id="import-err-filetype-text-xls"><f:message key="dialog.import.wrongfile"/><span id="import-err-filetype">xls, xlsx</span></span> 
    </div>
	
	<div id="import-links-dump" class="not-displayed dump"></div>	

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${importLabel}"  data-def="evt=import, state=parametrization, mainbtn=parametrization"/>
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, state=confirm, mainbtn=confirm"/>
		<input type="button" value="${okLabel}"		 data-def="evt=ok, state=summary, mainbtn=summary"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrsize, state=error-size, mainbtn=error-size"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrformat, state=error-format error-type, mainbtn=error-format error-type"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel, state=parametrization confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel-progression, state=progression"/>
	</div>
	
</div>

<%-- ================ /IMPORT LINK POPUP ====================== --%>
</sec:authorize>



<%-- ================= /EXPORT REQ POPUP ======================= --%>

<div id="export-requirement-dialog" class="popup-dialog not-displayed" title="${exportLabel}" data-def="nameprefix=${exportnamePrefix}, dateformat=${dateexportFormat}">

	<div data-def="state=main">
					
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
					
				<div class="std-margin-top">
			<input type="checkbox" id="export-keepRteFormat" checked="checked"/>
			<span><f:message key="test-case.export.dialog.keeprteformat"/></span>
			<div class="nota-bene">
				<f:message key="test-case.export.dialog.keeprteformat.description"/>
			</div>
		</div>
	</div>
	
	<div data-def="state=crossproerror">
		<span><f:message key="message.exportRequirementCrossProjectError"/></span>	
	</div>
	
	<div data-def="state=nonodeserror">
		<span><f:message key="message.exportRequirementNoNodeSelected"/></span>
	</div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${exportLabel}"		data-def="evt=confirm, mainbtn=main"/>
		<input type="button" value="${cancelLabel}"		data-def="evt=cancel, mainbtn=crossproerror, mainbtn=nonodeserror" />
	</div>
</div>


<%-- ================= /EXPORT REQ POPUP ======================= --%>


</div>