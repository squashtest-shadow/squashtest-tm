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


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:choose>
<c:when test="${initParam['stage'] == 'RELEASE'}">
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery-1.5.2.min.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery-ui-1.8.13.custom.min.js"></script>
</c:when>
<c:otherwise>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery-1.5.2-dev.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery-ui-1.8.13.all.dev.js"></script>
</c:otherwise>
</c:choose>


<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.plugin.js"></script> 
<%--<script type="text/javascript" src="http://localhost/scripts/jquery.squashtm.plugin.js"></script>--%>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.togglepanels.js"></script> 
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squash.messagedialog.js"></script>  
<%--<script type="text/javascript" src="http://localhost/scripts/jquery.squashtm.togglepanels.js"></script>--%>
