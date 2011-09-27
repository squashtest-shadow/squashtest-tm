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
<%@ tag description="HTML skeleton for a standard tree. Tag for internal use only" %>
<%@ attribute name="treeId" required="true" %>
<%@ attribute name="topToolbar" fragment="true" description="html definition of the top toolbar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>

<div  class="tree-filter-reminder-div">
	<span class="not-displayed"><f:message key="tabbed_panel.tree.pane.filter.enabled.label"/></span>
</div>
<div id="${ treeId }" class="tree"></div>
