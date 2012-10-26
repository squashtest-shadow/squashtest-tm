
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
<%@ attribute name="head" fragment="true"
	description="Additional html head fragment"%>
<%@ attribute name="footer" fragment="true"
	description="Additional html foot fragment"%>
<%@ attribute name="resourceName" required="true"%>
<%@ attribute name="iframeUrl" required="true"%>
<%@ attribute name="foot" fragment="true"
	description="Pseudo html foot fragment where one can put inlined script and js libraries imports"%>
	
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>


<c:set var="titleKey" value="workspace.${resourceName}.title" />
<c:set var="highlightedWorkspace" value="${resourceName}" />


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><f:message key="${titleKey }" />
</title>
<layout:common-head />
<layout:_common-script-import />
<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />	
<jsp:invoke fragment="head" />
</head>
<body>

	<layout:navigation highlighted="${highlightedWorkspace }" />
	<div id="workspace">

		<div id="workspace-title">
			<div class="snap-left">
				<h2>
					<f:message key="${titleKey }" />
				</h2>
			</div>
			<div class="snap-right">
				<div class="unstyled-notification-pane">
					<layout:_ajax-notifications cssClass="snap-right" />
				</div>
				<div class="main-menubar unstyled">
					<layout:_menu-bar />
				</div>

			</div>
		</div>
		<div id="iframeDiv" style="bottom: 0;    position: absolute;    top: 1cm;    width: 100%;">
		<f:message var="canotLoadMessage" key="iframe.cantLoad.message"/>
		<f:message var="canotLoadLink" key="iframe.cantLoad.link"/>
		<f:message var="canotLoadNote" key="iframe.cantLoad.note"/>
		<iframe id="iframePpal" src="${ iframeUrl }" style="height: 100%; width: 100%;"></iframe>
		</div>
		
<!-- 		Here is part to write if can detect iframe did not load -->
<%-- 		<div id='canotLoad'><p>${canotLoadMessage}  --%>
<%-- 		<br><br><a href='${ iframeUrl }' target='_blank'> ${canotLoadLink} </a> --%>
<%-- 		<br><br>${canotLoadNote }</p></div> --%>
	</div>
	<jsp:invoke fragment="footer" />
</body>
<comp:rich-jeditable-header />
<jsp:invoke fragment="foot" />
<div id="emptyIframe" style="display:none; ">
<p style="vertical-align:middle; text-align:center; ">
</p></div>
</html>