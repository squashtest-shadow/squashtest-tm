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
<%@ taglib prefix="lay" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="highlightedWorkspace" required="false" description="the highlighted workspace in the navigation bar." %>
<%@ attribute name="main" required="false" %>
<%-- the declaration oder does matter --%>

<script type="text/javascript">
var require = require || {};
require.baseUrl = "${pageContext.servletContext.contextPath}/scripts";
	var squashtm = {};
	squashtm.app = {
		contextRoot: "${pageContext.servletContext.contextPath}",
		ckeditorLanguage: "<f:message key='rich-edit.language.value' />",
		menuBarConf: {
    	}, 
    	notificationConf: {
  			infoTitle: "<f:message key='popup.title.info' />", 
  			errorTitle: "<f:message key='popup.title.error' />"
  		}
	};
	
<lay:_common-lang/>
	
</script>
<script type="text/javascript" src="<c:url value='/scripts/pubsub-boot.js' />"></script>
<c:choose>
  <c:when test="${ not empty main }">
<script  charset="utf-8" src="<c:url value='/scripts/require.js' />" data-main="${ main }"></script>
  </c:when>
  <c:otherwise>
<script  charset="utf-8" src="<c:url value='/scripts/require.js' />"></script>
<script  charset="utf-8" src="<c:url value='/scripts/common.js' />"></script>
  </c:otherwise>
</c:choose>
<script type="text/javascript">
  require([ "common" ], function() {
    require([ "domReady", "app/ws/squashtm.workspace" ], function(domReady, WS) {
      	domReady(function() {
            WS.init();
      });
    });  	
  });
</script>
