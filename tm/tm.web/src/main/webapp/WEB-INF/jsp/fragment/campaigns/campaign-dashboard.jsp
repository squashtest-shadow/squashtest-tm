<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	
	
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<?xml version="1.0" encoding="utf-8" contentType="text/html; charset=utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 			
		
		<link type="image/x-icon" rel="shortcut icon" href="${ pageContext.servletContext.contextPath }/images/favicon.ico"/>
		<comp:sq-css name="squash.core.css" />
    <comp:sq-css name="squash.print.css" />

		<script type="text/javascript">
		var require = require || {};
		require.baseUrl = "${pageContext.servletContext.contextPath}/scripts";
		var squashtm = {};
		squashtm.app = {
			contextRoot: "${pageContext.servletContext.contextPath}",
			locale : "<f:message key='squashtm.locale'/>"
		}
		<layout:_common-lang/>
		</script>
		<script type="text/javascript" src="${pageContext.servletContext.contextPath}/scripts/require-min.js"></script>

	</head>
	
	<body >
		<div class="ui-widget-header ui-state-default ui-corner-all fragment-header purple">
			<h2><span><f:message key="label.Campaign"/> : ${campaign.name}</span></h2>			
		</div>
		
		<div class="fragment-body">
			<dashboard:campaign-dashboard-panel url="${campaignStatisticsUrl}"  printUrl="${campaignStatisticsPrintUrl}" printmode="${true}" allowsSettled="${allowsSettled}" allowsUntestable="${allowsUntestable}" />
		</div>
		
		<script type="text/javascript">
		
			require(["common"], function(){
				require(["domReady","campaign-management"], function(domReady, campmanager){
					domReady(function(){
						campmanager.initDashboardPanel({
							master : '#dashboard-master',
							model : ${json:serialize(dashboardModel)}
						});	
					});
				});
			});
		
		</script>
	
	</body>
</html>