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
<%@ tag body-content="empty" %>
<%@ attribute name="title" description="Title of the panel. Alternative : set the titleKey attribute"%>
<%@ attribute name="titleKey" description="Key of the panel title. Alternative : set the title attribute" %>
<%@ attribute name="open" description="true if the panel should be opened when rendered" %>
<%@ attribute name="isContextual" %>
<%@ attribute name="panelButtons" fragment="true" description="add buttons to the togglepanel" %>
<%@ attribute name="body" fragment="true" description="body of the panel" %>
<%@ attribute name="id" required="true" description="the id of the panel" %>
<%@ attribute name="classes" description="classes the panel" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>


<div class="toolbar">
<jsp:invoke fragment="panelButtons"/>
</div>
<div class="table-tab-wrap">
<jsp:invoke fragment="body"/>
</div>



