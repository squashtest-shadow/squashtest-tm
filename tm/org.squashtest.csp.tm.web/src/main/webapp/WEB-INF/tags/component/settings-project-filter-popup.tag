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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ attribute name="openedBy" required="true" description="the id of the clickable widget that will open the popup" %>
<%@ attribute name="divId" required="true" description="the name you wish the popup to have"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>


<c:url var="projectFilterUrl" value="/global-filter/filter"/>
<%-- 
	yeees that one is not contextual. Set it to true and the damn dialog will get wiped by any tree action. 	
--%>

<f:message var="selectallLabel" key="dialog.settings.filter.controls.selectall" />
<f:message var="deselectAllLabel" key="dialog.settings.filter.controls.deselectall" />
<f:message var="invertselectLabel" key="dialog.settings.filter.controls.invertselect" />

<div id="project-filter-popup" class="popup-dialog project-picker" title="<f:message key='dialog.settings.filter.title' />">
	<div class="project-item-template not-displayed">
		<div class="project-item ">
			<input type="checkbox" class="project-checkbox"/> <span class="project-name"></span>
		</div>
	</div>
	<div id="dialog-settings-filter-maincontent">			
		<div id="dialog-settings-filter-controls" class="project-filter-controls">
			<a id="dialog-settings-filter-selectall" href="#" class="project-picker-selall">${selectallLabel}</a>
			<a id="dialog-settings-filter-deselectall" href="#" class="project-picker-deselall">${deselectAllLabel}</a>
			<a id="dialog-settings-filter-invertselect" href="#" class="project-picker-invsel">${invertselectLabel}</a>				
		</div>	
		<hr/>
		<div id="dialog-settings-filter-projectlist" class="project-filter-list">
		
		</div>
	
		<div style="clear:both;display:hidden;"></div>
	</div>
</div>

<%-- 
	code managing the initialization of the popup
 --%>
<c:url var="libUrl" value="/scripts/squash/squashtm.projectfilter.js" />
<script type="text/javascript" src="${libUrl}"></script> 
<script type="text/javascript">
	var projectFilterConf = {
			url: "${projectFilterUrl}",
			title : '<f:message key="dialog.settings.filter.title" />',
			confirmLabel: '<f:message key="dialog.button.confirm.label" />',
			cancelLabel: '<f:message key="dialog.button.cancel.label"/>'
	}
		
	squashtm.projectfilter.init(projectFilterConf);
</script>

