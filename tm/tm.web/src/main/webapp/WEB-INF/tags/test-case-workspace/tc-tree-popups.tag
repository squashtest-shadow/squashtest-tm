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
<f:message var="okLabel"			key="label.Ok"/>
<f:message var="exportLabel"		key="label.Export" />
<f:message var="dateexportFormat"	key="export.dateformat"/>
<f:message var="exportnamePrefix" 	key="label.lower.dash.exportTestCase" />

<f:message var="deleteMessagePrefix"	key="dialog.label.delete-node.label.start" />
<f:message var="deleteMessageVariable"  key="dialog.label.delete-nodes.test-cases.label"/>
<f:message var="deleteMessageSuffix"	key="dialog.label.delete-node.label.end"/>
<f:message var="deleteMessageNoUndo"	key="dialog.label.delete-node.label.cantbeundone" />
<f:message var="deleteMessageConfirm"	key="dialog.label.delete-node.label.confirm" />



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
			<td><input id="add-test-case-reference" type=text size="15" maxlength="20"/><br />
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
	
	<div class="waiting-loading" data-def="state=pleasewait"></div>
	
	<div class="not-displayed" data-def="state=confirm">
	
		<div class="display-table-row">
			<div class="display-table-cell delete-node-dialog-warning">
				<!-- content is empty on purpose, let it display a background image. -->
			</div>
			<div class="display-table-cell">
				<p>
					<c:out value="${deleteMessagePrefix}"/>
					<span class='red-warning-message'><c:out value="${deleteMessageVariable}" /></span> 
					<c:out value="${deleteMessageSuffix}" />
				</p>
				
				<div class="not-displayed delete-node-dialog-details">
					<p><f:message key="dialog.delete-tree-node.details"/></p>
					<ul>
					</ul>				
				</div>
				
				<p>
					<span><c:out value="${deleteMessageNoUndo}"/></span>				
					<span class='bold-warning-message'><c:out value="${deleteMessageConfirm}"/></span>				
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
			<form action="${servContext}/test-case-browser/import/upload" method="POST" enctype="multipart/form-data" class="display-table">

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
					<div class="display-table-cell"><label><f:message key="dialog.import-excel.test-case.filetype.message"/></label></div>
					<div class="display-table-cell">
						<input type="file" name="archive" size="20" accept="application/zip" />
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
	
	<div data-def="state=progression" style="vertical-align:middle;">
		<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
		<span style="font-size:1.5em;"><f:message key="squashtm.processing"/></span>			
	</div>
	
	<div class="import-summary" data-def="state=summary">
	
		<div><span><f:message key="dialog.import-excel.test-case.total"/></span><span class="total-import span-bold"></span></div>
		
		<div><span><f:message key="dialog.import-excel.test-case.success"/></span><span class="success-import span-bold span-green"></span></div>
		
		<div><span><f:message key="dialog.import-excel.test-case.rejected"/></span><span class="rejected-import span-bold span-green"></span></div>
		
		<div><span><f:message key="dialog.import-excel.test-case.failed"/></span><span class="failures-import span-bold"></span></div>			
		
		<div class="import-excel-dialog-note">			
			<hr/>
			<span><f:message key="dialog.import.summary.notes.label"/></span>
			<ul>
				<li class="import-excel-dialog-renamed"><span><f:message key="dialog.import-excel.test-case.warnings.renamed"/></span></li>
				<li class="import-excel-dialog-modified"><span><f:message key="dialog.import-excel.test-case.warnings.modified"/></span></li>
				<li class="import-excel-dialog-extension"><span><f:message key="dialog.import-excel.test-case.warnings.extension"/></span></li>
			</ul>
		</div>
	</div>	

	<div data-def="state=error-size">
		<span class="error-size"><f:message key="dialog.import.error.sizeexceeded"/></span>
	</div>
	
	<div data-def="state=error-format">		
		<span><f:message key="dialog.import.wrongfile"/>zip</span>	
	</div>
	
	<div id="import-excel-dump" class="not-displayed dump"></div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${importLabel}"  data-def="evt=import, state=parametrization, mainbtn=parametrization"/>
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, state=confirm, mainbtn=confirm"/>
		<input type="button" value="${okLabel}"		 data-def="evt=ok, state=summary, mainbtn=summary"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrsize, state=error-size, mainbtn=error-size"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrformat, state=error-format, mainbtn=error-format"/>
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
	
	<div data-def="state=progression" style="vertical-align:middle;">
			<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
		<span style="font-size:1.5em;"><f:message key="squashtm.processing"/></span>			
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
	
	<div id="import-links-dump" class="not-displayed dump"></div>	

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${importLabel}"  data-def="evt=import, state=parametrization, mainbtn=parametrization"/>
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, state=confirm, mainbtn=confirm"/>
		<input type="button" value="${okLabel}"		 data-def="evt=ok, state=summary, mainbtn=summary"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrsize, state=error-size, mainbtn=error-size"/>
		<input type="button" value="${okLabel}"		 data-def="evt=okerrformat, state=error-format, mainbtn=error-format"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel, state=parametrization confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel-progression, state=progression"/>
	</div>
	
</div>

<%-- ================ /IMPORT LINK POPUP ====================== --%>
</sec:authorize>



<%-- ================= /EXPORT TC POPUP ======================= --%>

<div id="export-test-case-dialog" class="popup-dialog not-displayed" title="${exportLabel}" data-def="nameprefix=${exportnamePrefix}, dateformat=${dateexportFormat}">

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
	</div>
	
	<div data-def="state=crossproerror">
		<span><f:message key="message.exportTestCaseCrossProjectError"/></span>	
	</div>
	
	<div data-def="state=nonodeserror">
		<span><f:message key="message.exportTestCaseNoNodeSelected"/></span>
	</div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${exportLabel}"		data-def="evt=confirm, mainbtn=main"/>
		<input type="button" value="${cancelLabel}"		data-def="evt=cancel, mainbtn=crossproerror, mainbtn=nonodeserror" />
	</div>
</div>


<%-- ================= /EXPORT TC POPUP ======================= --%>
</div>