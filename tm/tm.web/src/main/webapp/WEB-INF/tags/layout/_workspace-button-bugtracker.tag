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
<%@ tag body-content="empty"%>
<%@ attribute name="highlighted" type="java.lang.Boolean"
	required="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:url var="workspaceUrl" value="/bugtracker/workspace-button" />
<%--
	this section is loaded asynchronously. The bugtracker might be out of reach indeed.
 --%>
<script type="text/javascript">
	require([ "common" ], function() {
		require([ "domReady", "squash.bugtrackerMenu" ], function(domReady,
				BTM) {
			
			domReady(function() {
				squashtm.bugtrackerMenu = new BTM({workspaceUrl :"${workspaceUrl}"});
				squashtm.bugtrackerMenu.updateBugTrackerMenu(${ highlighted});
			});
		});
	});
</script>
<div id="bugtracker-div"></div>
