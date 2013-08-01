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
<%@ tag language="java" pageEncoding="ISO-8859-1" body-content="empty" description="structure of a dashboard for test cases. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="url" required="true" description="url where to get the data" %>
<%@ attribute name="listenTree" type="java.lang.Boolean"  required="true" description="whether to listen to the tree selection or not" %>


<div id="dashboard-master" data-def="rendering=toggle-panel, workspace=test-case, url=${url}, listenTree=${listenTree}">

	<f:message var="dashboardTitle" key="title.Dashboard"/>
	<f:message var="refreshLabel" key="label.Refresh" />
	<div class="toggle-panel">
		<span class="not-displayed toggle-panel-buttons">
			<input type="button" class="dashboard-refresh-button button" role="button" value="${refreshLabel}"/>
		</span>
		<div class="toggle-panel-main dashboard-figures" title="${dashboardTitle}">
			<div class="dashboard-item">
				
			</div>
			<div class="dashboard-item">
				
			</div>
			<div class="dashboard-item">
				
			</div>
			<div class="dashboard-item">
				
			</div>
		</div>
	</div>
	
	<span class="dashboard-summary"><f:message key="dashboard.test-cases.summary"/><span class="dashboard-total"></span></span>
	
</div>