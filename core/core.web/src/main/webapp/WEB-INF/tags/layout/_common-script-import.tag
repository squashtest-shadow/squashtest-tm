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
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="ck" tagdir="/WEB-INF/tags/ckeditor" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- the declaration oder does matter --%>

<script type="text/javascript">
var require = require || {};
require.baseUrl = "${pageContext.servletContext.contextPath}/scripts";
	var squashtm = {};
	squashtm.app = {
		contextRoot : "${pageContext.servletContext.contextPath}",
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
</script>
<script src="<c:url value='/scripts/require-min.js' />"></script>

<jq:jquery-header />
<ck:ckeditor-header />

<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/squashtm.fg.menu.css" />

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.popup-error.js" ></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.oneshotdialog.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/KeyEventListener.js"></script>


<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.contextual-content.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.fg.menu.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.squashbutton.js"></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/Event.js"></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/StatusFactory.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/AutomatedSuiteOverview.js"></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.form.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/FeedbackMultipartPopup.js"></script>


<!-- --------------------DataTables library imports. SHOULD BE IMPORTED BEFORE DATATABLE USAGE--------------------------------------------- -->

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/datatables/jquery.dataTables.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.datatables.js"></script>

<%-- transitional javascript --%>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.datatables.v2.js"></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.tableDnD.ext.js"></script> 

<!-- //end of datatable library imports -->

<!-- -------------------- /DataTables library imports. SHOULD BE IMPORTED BEFORE DATATABLE USAGE--------------------------------------------- -->

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.bugtracker-issue-dialog.js" ></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/ta-admin-panel.js"></script>
  
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/ta-picker.js"></script>


<script type="text/javascript">
  require([ "common" ], function() {
    require([ "app/ws/squashtm.workspace" ], function(WS) {
      WS.init("${ highlightedWorkspace }");
    });
  });
</script>

<script type="text/javascript">
	$(function() {
		enableTableRangeSelection();
	});
</script>

<!-- -------------------- Waiting for better handle of locale in .js--------------------------------------------- -->
<script>
$(function(){
	squashtm.message = new Object();
	squashtm.message.cancel = "<f:message key='label.Cancel'/>" ;
	squashtm.message.confirm = "<f:message key='label.Confirm'/>" ;
});
</script>
<!-- -------------------- Waiting for better handle of locale in .js--------------------------------------------- -->
