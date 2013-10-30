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
<%@ tag language="java" pageEncoding="utf-8" body-content="empty" description="structure of a dashboard for test cases. No javascript."%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ attribute name="url" required="true" description="url where to get the data" %>
<%@ attribute name="cacheKey" required="false" description="if set, will use the cache using that key" %>


<f:message var="advanceTitle" key="title.CampaignCumulativeAdvancement"/>
<f:message var="statisticsTitle" key="title.CampaignStatistics"/>
<f:message var="inventoryTitle" key="title.TestInventoryByIteration"/>
<f:message var="refreshLabel" key="label.Refresh" />

<div id="dashboard-master" data-def="url=${url}">

	<div class="toolbar">
		<span class="dashboard-timestamp not-displayed"><f:message key="dashboard.meta.timestamp.label"/></span> 
		<input type="button" class="dashboard-refresh-button button" role="button" value="${refreshLabel}"/>	
		<input type="button" class="button" role="button" value="I WANT MY ICON !"/>
	</div>
	
	
	<%-- alternate contents : when no data are available we'll display an empty pane, when there are some we'll display the rest. --%>
	
	<div class="dashboard-figleaf">
		
		<div class="dashboard-figleaf-notready" style="text-align : center">
			<h3 class="dashboard-figleaf-notready-title"><f:message key="dashboard.notready.title"/></h3>
		</div>
	
		<div class="dashboard-figleaf-figures not-displayed">
		
			<%-- first dashboard : cumulative progression of this campaign --%>
			<comp:toggle-panel id="dashboard-cumulative-progression" title="${advanceTitle}">
				<jsp:attribute name="body">
				<div class="dashboard-figures not-displayed">
				
				</div>
				</jsp:attribute>
			</comp:toggle-panel>
			
			<%-- second dashboard : campaign statistics --%>
			<comp:toggle-panel id="dashboard-statistics" title="${statisticsTitle}">
				<jsp:attribute name="body">
				<div class="dashboard-figures not-displayed">
				
				</div>
				</jsp:attribute>
			</comp:toggle-panel>
			
			<style type="text/css">
			
				table.test-inventory thead th{
					border : 1px solid black;
					border-collapse : collapse;
					height : 30px;
				}
				
				table.test-inventory th.status-color-ready{
					background-color : #BDD3FF;
				}
				
				table.test-inventory th.status-color-running{
					background-color : #6699FF;
				}
				
				table.test-inventory th.status-color-success{
					background-color : #99CC00;
				}
				
				table.test-inventory th.status-color-failure{
					background-color : #FF3300;
				}
				
				table.test-inventory th.status-color-blocked{
					background-color : #FFCC00;
				}
				
				table.test-inventory th.status-color-untestable{
					background-color : #969696;
				}
			
			</style>
			
			<comp:toggle-panel id="" title="${inventoryTitle}">
				<jsp:attribute name="body">
				<div class="dashboard-figures">		
					<table id="dashboard-test-inventory" class="test-inventory" data-def="model-attribute=iterationTestInventoryStatisticsList">
						<thead>
							<tr>				
								<th style="width:25%"><span><f:message key="label.iteration"/></span></th>
								<th class="status-color-ready"><span><f:message key="label.Ready"/></span></th>
								<th class="status-color-running"><span><f:message key="label.Running"/></span></th>
								<th class="status-color-success"><span><f:message key="label.Success"/></span></th>
								<th class="status-color-failure"><span><f:message key="label.Failure"/></span></th>
								<th class="status-color-blocked"><span><f:message key="label.Blocked"/></span></th>
								<th class="status-color-untestable"><span><f:message key="label.Untestable"/></span></th>
								<th><span><f:message key="dashboard.campaigns.testinventory.legend.testnumber"/></span></th>
								<th><span><f:message key="dashboard.campaigns.testinventory.legend.advancementexecution"/></span></th>
							</tr>
						</thead>
					
						<tbody>
							<tr>
								<td colspan="9">No record founds (résultat vide) (internationalise moi ça)</td>
							</tr>
						</tbody>			
					</table>		
				</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</div>
	
	</div>
</div>