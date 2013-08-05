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
			
			<div id="dashboard-item-bound-reqs" class="dashboard-item dashboard-pie" data-def="model-attribute=boundRequirementsStatistics">
			
				<div id="dashboard-bound-reqs-view" class="dashboard-item-view">
				
				</div>

				<div class="dashboard-item-meta">
					<h2 class="dashboard-item-title"><f:message key="dashboard.test-cases.bound-reqs.title"/></h2>
					
					<div class="dashboard-item-legend">
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#9CCBE0"></div>
							<span><f:message key="dashboard.test-cases.bound-reqs.legend.no-reqs" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#69B1D1"></div>
							<span><f:message key="dashboard.test-cases.bound-reqs.legend.one-req" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#3383A7"></div>
							<span><f:message key="dashboard.test-cases.bound-reqs.legend.many-reqs" /></span>
						</div>
					</div>
				</div>
			</div>
			
			
			<div id="dashboard-item-test-case-status" class="dashboard-item dashboard-pie" data-def="model-attribute=statusesStatistics">
	
				<div id="dashboard-test-case-status" class="dashboard-item-view">
				
				</div>
						
				<div class="dashboard-item-meta">		
					<h2 class="dashboard-item-title"><f:message key="dashboard.test-cases.status.title"/></h2>
				
					<div class="dashboard-item-legend">
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#C9E8AA"></div>
							<span><f:message key="test-case.status.WORK_IN_PROGRESS" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#A3D86E"></div>
							<span><f:message key="test-case.status.UNDER_REVIEW" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#56AD25"></div>
							<span><f:message key="test-case.status.APPROVED" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#FFFF00"></div>
							<span><f:message key="test-case.status.TO_BE_UPDATED" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#D9D9D9"></div>
							<span><f:message key="test-case.status.OBSOLETE" /></span>
						</div>
					</div>
				</div>
			</div>
			
			<div id="dashboard-item-test-case-importance" class="dashboard-item dashboard-pie" data-def="model-attribute=importanceStatistics">
	
				<div id="dashboard-test-case-importance" class="dashboard-item-view">
				
				</div>
				
				<div class="dashboard-item-meta">					
					<h2 class="dashboard-item-title"><f:message key="dashboard.test-cases.importance.title"/></h2>
				
					<div class="dashboard-item-legend">
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#FCEDB6"></div>
							<span><f:message key="test-case.importance.LOW" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#FBD329"></div>
							<span><f:message key="test-case.importance.MEDIUM" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#FDA627"></div>
							<span><f:message key="test-case.importance.HIGH" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#FD7927"></div>
							<span><f:message key="test-case.importance.VERY_HIGH" /></span>
						</div>
					</div>
				</div>
			</div>
			
			<div id="dashboard-item-test-case-size" class="dashboard-item dashboard-pie" data-def="model-attribute=sizeStatistics">
				
				<div id="dashboard-test-case-size" class="dashboard-item-view">
				
				</div>
				
				<div class="dashboard-item-meta">		
					<h2 class="dashboard-item-title"><f:message key="dashboard.test-cases.size.title"/></h2>
					
					<div class="dashboard-item-legend">
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#DFC3EF"></div>
							<span><f:message key="dashboard.test-cases.size.legend.zerosteps" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#C998E4"></div>
							<span><f:message key="dashboard.test-cases.size.legend.b0and10" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#AF67D7"></div>
							<span><f:message key="dashboard.test-cases.size.legend.b10and20" /></span>
						</div>
						<div>
							<div class="dashboard-legend-sample-color" style="background-color:#993CCC"></div>
							<span><f:message key="dashboard.test-cases.size.legend.above20" /></span>
						</div>
					</div>
				</div>
			</div>
	
			<span class="dashboard-summary"><f:message key="dashboard.test-cases.summary"/><span class="dashboard-total"></span></span>
			
		</div>
	</div>
	
</div>