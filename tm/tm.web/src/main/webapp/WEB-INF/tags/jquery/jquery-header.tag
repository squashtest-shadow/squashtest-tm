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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:choose>
<c:when test="${initParam['stage'] == 'RELEASE'}">
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/lib/jquery/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/lib/jquery/jquery-ui-1.9.2.custom.min.js"></script>
</c:when>
<c:otherwise>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/lib/jquery/jquery-1.8.3.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/lib/jquery/jquery-ui-1.9.2.custom.js"></script>
</c:otherwise>
</c:choose>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.plugin.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.togglepanels.js"></script> 
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.messagedialog.js"></script>  
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.confirmdialog.js"></script>  
