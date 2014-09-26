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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<%@ attribute name="importableLibraries" required="true" description="the potential target libraries for upload." type="java.lang.Object" %>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<f:message var="addFolderTitle"  	key="dialog.new-folder.title"/>	
<f:message var="addTestCaseTitle"  	key="dialog.new-test-case.title"/>	
<f:message var="renameNodeTitle"	key="dialog.rename-tree-node.title" />
<f:message var="deleteNodeTitle"	key="dialog.delete-tree-node.title"/>
<f:message var="importExcelTitle"	key="dialog.import-excel.title" />
<f:message var="importLinksTitle"	key="dialog.import-links-excel.title" />
<f:message var="exportLabel"		key="label.Export" />
<f:message var="addLabel"		 	key="label.Add"/>
<f:message var="addAnotherLabel"	key="label.addAnother"/>
<f:message var="cancelLabel"		key="label.Cancel"/>
<f:message var="confirmLabel"		key="label.Confirm"/>
<f:message var="importLabel"		key="label.Import"/>
<f:message var="simulateLabel"		key="label.Simulate"/>
<f:message var="okLabel"			key="label.Ok"/>
<f:message var="exportLabel"		key="label.Export" />
<f:message var="dateexportFormat"	key="export.dateformat"/>
<f:message var="exportnamePrefix" 	key="label.lower.dash.exportTestCase" />
<f:message var="targetProjectHelper" key="message.import.info.targetproject" />

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


<div id="add-test-case-dialog" class="popup-dialog not-displayed" title="${addTestCaseTitle}">
	<table class="add-node-attributes">
		
		<tr>
			<td><label for="add-test-case-name"><f:message key="label.Name" /></label></td>

			<td><input id="add-test-case-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		
		<tr>
			<td><label for="add-test-case-reference"><f:message key="label.Reference" /></label></td>
			<td><input id="add-test-case-reference" type=text size="20" maxlength="50"/><br />
				<comp:error-message forField="reference" />	<td>
		</tr>
					
		<tr>
			<td><label for="add-test-case-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-test-case-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabel}"   	data-def="evt=add-another, mainbtn"/>
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
						<f:message key="dialog.label.delete-nodes.test-cases.label"/>
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

	<div data-def="state=parametrization" class="std-margin-top">
		
		<div>
			<form action="${servContext}/test-cases/importer" method="POST" enctype="multipart/form-data" class="display-table">

				<div class="std-padding std-margin-top">
				
					<div class="grey-round-panel snap-left" style="width:43%;">
						<input id="xls-import-opt" type="radio" name="import-type" value="xls" class="centered" checked="checked"/><span style="text-decoration:underline;">Excel</span>
						<div class="nota-bene">
							<f:message key="test-case.import.dialog.excel.description"/>
						</div>
           	            <f:message var="xlsFileName" key="file.testcase.import.template.xls"/>
						<a href="${servContext }/static/${xlsFileName}">
							<f:message key="test-case.import.dialog.excel.template"/>
						</a>			
					</div>
				
					<div class="grey-round-panel snap-right" style="width:43%;">
						<input id="zip-import-opt" type="radio" name="import-type" value="zip" class="centered"/><span style="text-decoration:underline;">ZIP</span>
						<div class="nota-bene">
							<f:message key="test-case.import.dialog.zip.description"/>
						</div>			
                        <f:message var="zipFileName" key="file.testcase.import.template.zip"/>
						<a href="${servContext }/static/${zipFileName}">
							<f:message key="test-case.import.dialog.zip.template"/>
						</a>
					</div>
				
					<div class="unsnap"></div>
				
				</div>

				<div class="std-margin-top">
				<div>
				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.import.project.message"/></label></div>
					<div class="display-table-cell">
						<select id="import-project-list" name="projectId" disabled="disabled">
							<c:forEach items="${importableLibraries}" var="lib" varStatus="status" >
							<%-- warning : c tag nested in another c tag --%>
							<option value="${lib.id}" <c:if test="${status.first}">selected="yes"</c:if>>${lib.project.name}</option>
							</c:forEach>
						</select>
						<div class="icon-helper no-print small-margin-left" title="${targetProjectHelper}"></div>
					</div>
				</div>

				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.import-excel.test-case.filetype.message"/></label></div>
					<div class="display-table-cell">
						<input type="file" name="archive" size="20" accept="application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/zip" />
					</div>
				</div>
				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.import.encoding.label"/></label></div>
					<div class="display-table-cell">
						<select name="zipEncoding">
							<option value="Cp858">Windows <f:message key="dialog.import.encoding.default"/></option>
							<option value="UTF8">UTF-8</option>
						</select>
					</div>
				</div>
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
		<div class="confirm-div import-project-confirm not-displayed">
			<label class="confirm-label"><f:message key="dialog.import.project.confirm"/></label>
			<span class="confirm-span confirm-project"><c:out value="${importableLibraries[0].project.name}"/></span>
		</div>
		
		<div><f:message key="dialog.import.confirm.message"/></div>
	</div>
	
	<div data-def="state=progression" >
		<comp:waiting-pane/>
	</div>
	
	<div class="import-summary" data-def="state=summary">
	</div>
  
  <script id="zip-import-recap-tpl" type="text/x-handlebars-template">
 	<div><span><f:message key="dialog.import-excel.test-case.total"/></span><span class="total-import span-bold">{{total}}</span></div>

	<div><span><f:message key="dialog.import-excel.test-case.success"/></span><span class="success-import span-bold span-green">{{success}}</span></div>
	
	<div><span><f:message key="dialog.import-excel.test-case.rejected"/></span><span class="rejected-import span-bold span-green">{{rejected}}</span></div>
	
	<div>
		<span><f:message key="dialog.import-excel.test-case.failed"/></span>
		<span class="failures-import span-bold {{failuresClass}}">{{failures}}</span>
	</div>			
	{{#unless createdOnly}}
	<div class="import-excel-dialog-note">
		<hr/>
		<span><f:message key="dialog.import.summary.notes.label"/></span>
		<ul>
			{{#if hasRenamed}}
			<li class="import-excel-dialog-renamed"><span><f:message key="dialog.import-excel.test-case.warnings.renamed"/></span></li>
			{{/if}}
			{{#if hasModified}}
			<li class="import-excel-dialog-modified"><span><f:message key="dialog.import-excel.test-case.warnings.modified"/></span></li>
			{{/if}}
			{{#if hasRejects}}
			<li class="import-excel-dialog-extension"><span><f:message key="dialog.import-excel.test-case.warnings.extension"/></span></li>
			{{/if}}
		</ul>
	</div>
	{{/unless}}
  </script>
  
  <script id="xls-import-recap-tpl" type="text/x-handlebars-template">
  <div>
    <p class="cf">
      <span class="snap-left ui-icon ui-icon-check"></span><f:message key="message.import.success" />
    </p>
    <p>
    <table>
      <thead>
      <tr>
        <th class="not visible"></th>
        <th><f:message key="message.import.successRowCount" /></th>
        <th><f:message key="message.import.warningRowCount" /></th>
        <th><f:message key="message.import.failureRowCount" /></th>
      </tr>
      </thead>
      <tbody>
      <tr>
        <th><f:message key="label.testCases" /></th>
        <th class="txt-success">{{testCaseSuccesses}}</th>
        <th class="txt-warn">{{testCaseWarnings}}</th>
        <th class="txt-error">{{testCaseFailures}}</th>
      </tr>
      <tr>
        <th><f:message key="label.testSteps" /></th>
        <th class="txt-success">{{testStepSuccesses}}</th>
        <th class="txt-warn">{{testStepWarnings}}</th>
        <th class="txt-error">{{testStepFailures}}</th>
      </tr>
      <tr>
        <th><f:message key="label.parameters" /></th>
        <th class="txt-success">{{parameterSuccesses}}</th>
        <th class="txt-warn">{{parameterWarnings}}</th>
        <th class="txt-error">{{parameterFailures}}</th>
      </tr>
      <tr>
        <th><f:message key="label.datasets" /></th>
        <th class="txt-success">{{datasetSuccesses}}</th>
        <th class="txt-warn">{{datasetWarnings}}</th>
        <th class="txt-error">{{datasetFailures}}</th>
      </tr>
      </tbody>
    </table>
    </p>
    <p class="cf">
      <span class="snap-left ui-icon ui-icon-arrowthick-1-e"></span><a href="{{reportUrl}}"><f:message key="message.import.downloadLog" /></a>
    </p>
  </div>
  </script>
 
  
   <script id="xls-import-recap-ko" type="text/x-handlebars-template">
   <p><f:message key="message.import.error.format"/><ul>
     {{#if missingMandatoryColumns.length}}
       <li><f:message key="message.import.log.error.mandatoryColumns"/>{{missingMandatoryColumns}}.</li>
     {{/if}}
     {{#if duplicateColumns.length}}
       <li><f:message key="message.import.log.error.duplicateColumns"/></li>
     {{/if}}
   </ul></p>
   </script>

      <div data-def="state=error-size">
		<span class="error-size"><f:message key="dialog.import.error.sizeexceeded"/></span>
	</div>
	
	<div class="error-type" data-def="state=error-type">		
		<span class="import-err-filetype-text" id="import-err-filetype-text-xls"><f:message key="dialog.import.wrongfile"/><span id="import-err-filetype">xls, xlsx</span></span>	
		<span class="import-err-filetype-text" id="import-err-filetype-text-zip" style="display: none;"><f:message key="dialog.import.wrongfile"/><span id="import-err-filetype">zip</span></span>	
	</div>
	<div class="error-format" data-def="state=error-format">
    </div>
	<div id="import-excel-dump" class="not-displayed dump"></div>

	<div class="popup-dialog-buttonpane">
		<input id="simulateButton" type="button" value="${simulateLabel}"  data-def="evt=simulate, state=parametrization"/>
		<input type="button" value="${importLabel}"  data-def="evt=import, state=parametrization, mainbtn=parametrization"/>
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, state=confirm, mainbtn=confirm"/>
		<input type="button" value="${okLabel}"		 data-def="evt=ok, state=summary, mainbtn=summary"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrsize, state=error-size, mainbtn=error-size"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrformat, state=error-type error-format, mainbtn=error-type error-format"/>
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
		<div><f:message key="dialog.import.confirm.message"/></div>
	</div>
	
	<div data-def="state=progression">
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
  
	
	<div  data-def="state=error-type">
      <p><f:message key="message.import.error.format"/><ul><li><span><f:message key="dialog.import.wrongfile"/>xls, xlsx</span></li></ul></p>
      <p><f:message key="message.import.error.format"/><ul><li><span><f:message key="dialog.import.wrongfile"/>zip</span></li></ul></p>
	</div>
 
    
  
	
	<div id="import-links-dump" class="not-displayed dump"></div>	

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${importLabel}"  data-def="evt=import, state=parametrization, mainbtn=parametrization"/>
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, state=confirm, mainbtn=confirm"/>
		<input type="button" value="${okLabel}"		 data-def="evt=ok, state=summary, mainbtn=summary"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrsize, state=error-size, mainbtn=error-size"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrformat, state=error-type, mainbtn=error-type"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel, state=parametrization confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel-progression, state=progression"/>
	</div>
	
</div>

<%-- ================ /IMPORT LINK POPUP ====================== --%>
</sec:authorize>



<%-- ================= /EXPORT TC POPUP ======================= --%>

<div id="export-test-case-dialog" class="popup-dialog not-displayed" title="${exportLabel}" data-def="nameprefix=${exportnamePrefix}, dateformat=${dateexportFormat}">

	<div data-def="state=main" class="std-margin-top">
					
		<span ><f:message key="test-case.export.dialog.message"/></span>
		
		<div class="std-padding std-margin-top">
		
			<div class="grey-round-panel snap-left" style="width:43%;">
				<input type="radio" name="format" data-val="xls" class="centered" checked="checked"/><span style="text-decoration:underline;">Excel</span>
				<div class="nota-bene">
					<f:message key="test-case.export.dialog.excel.description"/>
				</div>			
			</div>
		
			<div class="grey-round-panel snap-right" style="width:43%;">
				<input type="radio" name="format" data-val="csv" class="centered"/><span style="text-decoration:underline;">CSV</span>
				<div class="nota-bene">
					<f:message key="test-case.export.dialog.csv.description"/>
				</div>			
			</div>
		
			<div class="unsnap"></div>
		
		</div>
		
		<div class="std-margin-top">
			<span><f:message key="test-case.export.dialog.filename"/></span>
			<input type="text" id="export-test-case-filename" size="255" style="width:350px"/>
		</div>
		
		<div class="std-margin-top">
			<input type="checkbox" id="export-test-case-includecalls"/>
			<span><f:message key="test-case.export.dialog.includecall"/></span>
			<div class="nota-bene">
				<f:message key="test-case.export.dialog.includecall.description"/>
			</div>
		</div>
		
	</div>
	
	<div data-def="state=nonodeserror" class="std-margin-top">
		<span><f:message key="message.exportTestCaseNoNodeSelected"/></span>
	</div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${exportLabel}"		data-def="evt=confirm, mainbtn=main"/>
		<input type="button" value="${cancelLabel}"		data-def="evt=cancel, mainbtn=nonodeserror" />
	</div>
</div>


<%-- ================= /EXPORT TC POPUP ======================= --%>
</div>