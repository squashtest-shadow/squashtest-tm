<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ attribute name="head" fragment="true" description="Additional html head fragment" %>
<%@ attribute name="footer" fragment="true" description="Additional html foot fragment" %>
<%@ attribute name="resourceName" required="true" %>
<%@ attribute name="foot" fragment="true" description="Pseudo html foot fragment where one can put inlined script and js libraries imports" %>
<%@ attribute name="categories" type="java.util.Collection"  required="true" description="the categories of reports that should be displayed"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"  %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>


<c:set var="titleKey" value="workspace.${resourceName}.title"/>
<c:set var="highlightedWorkspace" value="${resourceName}" />



<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
		<title><f:message key="${ titleKey }"/></title>
		<layout:common-head />		
		<layout:_common-script-import />
		<comp:decorate-toggle-panels/>
		<jsp:invoke fragment="head" />
	</head>
	<body>
	

		<%-- script managing the state of the window : normal or expanded --%>
		<%-- default is normal state --%>
		<script type="text/javascript">
		
			<%-- 
				the function regarding the category panel are defined in edit-report.jsp,
				because the button managing it is there too.
				
				If that's too confusing consider refractoring some day.
			--%>

			function setReportWorkspaceNormalState(){
				setCategoryFrameNormalState();
				setEditReportNormalState();
			}
			
			function setReportWorkspaceExpandState(){
				setCategoryFrameExpandState();
				setEditReportExpandState();
			}
			
			function toggleReportWorkspaceState(){
				toggleCategoryFrameState();
				toggleEditReportState();							
			}
			

			<%-- script managing the expand/normal state of this part of the workspace --%>

			function setEditReportNormalState(){
				$("#contextual-content").removeClass("expanded");
			}
			
			function setEditReportExpandState(){
				$("#contextual-content").addClass("expanded");
			}
			
			function toggleEditReportState(){
				$("#contextual-content").toggleClass("expanded");
			}

		</script>
	
	
	
		<layout:navigation highlighted="${ highlightedWorkspace }" />		
		<div id="workspace">
			<c:if test="${resourceName != 'home'}">
				<div id="workspace-title">
					<div class="snap-left">
						<h2><f:message key="${titleKey}" /></h2>	
					</div>
					<div class="snap-right">
						<div style="display:inline-block;">
							<layout:_ajax-processing-indicator  cssClass="snap-right"/>
							<layout:_generic-error-notification-area cssClass="snap-right"/>
							<layout:_warning-notification-area  cssClass="snap-right"/>
						</div>
						<div class="main-menubar">
							<layout:_menu-bar />
						</div>
					
					</div>
				</div>
				<div id="category-pane">
					<comp:category-panel categories="${categories}" />
				</div>
				<div id="contextual-content" class="no-resizable-contextual-content">
					<%-- populated on demand later --%>
				</div>
			</c:if>			
		</div>
		<jsp:invoke fragment="footer" />
	</body>
	<comp:rich-jeditable-header />
	<layout:_handle-ajax-errors />
	<layout:_handle-ajax-processing/>
	<comp:decorate-buttons />
	<jsp:invoke fragment="foot" />
</html>