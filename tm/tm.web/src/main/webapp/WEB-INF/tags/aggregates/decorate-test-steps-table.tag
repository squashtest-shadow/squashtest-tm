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
<%@ tag body-content="empty"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>


<%-- ==================== DOM definition =================== --%>

<table id="test-steps-table">
	<thead>
		<tr>
			<th>S</th>
			<th>#</th>
			<th>stepId(masked)</th>
			<th><f:message key="table.column-header.has-attachment.label" />
			</th>
			<th><f:message
					key="label.Actions" />
			</th>
			<th><f:message
					key="label.ExpectedResults" />
			</th>
			<th>M</th>
			<th>&nbsp;</th>
			<th>nbAttach(masked)</th>
			<th>stepNature(masked)</th>
			<th>calledStepId(masked)</th>
		</tr>
	</thead>
	<tbody>
		<%-- Will be populated by ajax --%>
	</tbody>
</table>

<div id="test-step-row-buttons" class="not-displayed">
	<a id="delete-step-button" href="#" class="delete-step-button">
		<f:message	key="test-case.step.delete.label" />
	</a> 
	<a id="manage-attachment-button" href="#" class="manage-attachment-button">
		<f:message key="test-case.step.manage-attachment.label" />
	</a> 
	<a id="manage-attachment-button-empty" href="#" class="manage-attachment-button-empty">
		<f:message key="test-case.step.add-attachment.label" />
	</a>
</div>


<%-- ==================== /DOM definition =================== --%>

<script type="text/javascript">
	require(["domReady", "test-cases-management"], function(domReady,testCaseManagement){
		domReady(function(){
			
			var settings = {
				collapser : {
					language : {
						popupTitle : "<f:message key='popup.title.info' />",
						popupMessage : "<f:message key='message.CloseEditingFormsBeforeCollapse' />",
						btnExpand : "<f:message key='test-case.step.button.expand.label' />",
						btnCollapse : "<f:message key='test-case.step.button.collapse.label' />"
					}
				}
			};
			
			testCaseManagement.initStepTable(settings);
		})
	});
</script>