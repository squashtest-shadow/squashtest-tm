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
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="ck" tagdir="/WEB-INF/tags/ckeditor" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="highlightedWorkspace" required="false" description="the highlighted workspace in the navigation bar." %>
<%-- the declaration oder does matter --%>

<script type="text/javascript">
var require = require || {};
require.baseUrl = "${pageContext.servletContext.contextPath}/scripts";
	var squashtm = {};
	squashtm.app = {
		contextRoot: "${pageContext.servletContext.contextPath}",
		ckeditorLanguage: "<f:message key='rich-edit.language.value' />",
		projectFilterConf: {
			url: "<c:url value='/global-filter/filter' />",
			title: "<f:message key='dialog.settings.filter.title' />",
			confirmLabel: "<f:message key='label.Confirm' />",
			cancelLabel: "<f:message key='label.Cancel' />",
		}, 
		menuBarConf: {
    		boxSelector: "#menu-toggle-filter-ckbox",
    		url: "<c:url value='/global-filter/filter-status' />",
    		linkSelector: "#menu-project-filter-link",
    		enabledTxt: "<f:message key='workspace.menubar.filter.enabled.label' />",
    		disabledTxt: "<f:message key='workspace.menubar.filter.disabled.label' />",
    		enabledCallbacks: [ function(){ $("div.tree-filter-reminder-div > span").removeClass("not-displayed");} ]
    	}, 
    	notificationConf: {
  			infoTitle: "<f:message key='popup.title.info' />", 
  			errorTitle: "<f:message key='popup.title.error' />"
  		}
	};
	
	// -------------------- Waiting for better handle of locale in .js---------------------------------------------
	squashtm.message = squashtm.message || {};
	squashtm.message.cancel = "<f:message key='label.Cancel'/>";
	squashtm.message.placeholder = "<f:message key='rich-edit.placeholder'/>";
	squashtm.message.confirm = "<f:message key='label.Confirm'/>";		
	squashtm.message.intoTitle = "<f:message key='popup.title.info'/>";		
	squashtm.message.errorTitle = "<f:message key='popup.title.error'/>";	
	
</script>
<script  charset="utf-8"src="<c:url value='/scripts/require-min.js' />"></script>
<script  charset="utf-8"src="<c:url value='/scripts/common.js' />"></script>
<jq:jquery-header />
<ck:ckeditor-header />

<script charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.popup-error.js" ></script>

<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.oneshotdialog.js"></script>
<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/KeyEventListener.js"></script>


<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.squashbutton.js"></script>

<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/Event.js"></script>

<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/StatusFactory.js"></script>
<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/AutomatedSuiteOverview.js"></script>

<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.form.js"></script>
<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.cookie.js"></script>


<!-- --------------------DataTables library imports. SHOULD BE IMPORTED BEFORE DATATABLE USAGE--------------------------------------------- -->

<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/datatables/jquery.dataTables.js"></script>


<!-- -------------------- /DataTables library imports. SHOULD BE IMPORTED BEFORE DATATABLE USAGE--------------------------------------------- -->


<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/ta-admin-panel.js"></script>
  
<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/ta-picker.js"></script>
<script  charset="utf-8" type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/TestAutomationRemover.js"></script>


<script type="text/javascript">
  require([ "common", "squashtable" ], function() {	// temporary hack (I hope) : squashtable is required here to make it available globally even to non require-friendly code
    require([ "domReady", "app/ws/squashtm.workspace" ], function(domReady, WS) {
    	domReady(function() {
          WS.init("${ highlightedWorkspace }");
    	});
    });
  });
</script>
