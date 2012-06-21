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

<%-- the declaration oder does matter --%>

<jq:jquery-header />
<ck:ckeditor-header />

<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/squashtm.fg.menu.css" />
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.oneshotdialog.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/KeyEventListener.js"></script>


<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.contextual-content.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.fg.menu.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.squashbutton.js"></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/Event.js"></script>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/classes/StatusFactory.js"></script>

<script type="text/javascript" src="http://localhost/scripts/jquery.squashtm.bugtracker-issue-dialog.js" ></script>


<dt:datatables-header/>
