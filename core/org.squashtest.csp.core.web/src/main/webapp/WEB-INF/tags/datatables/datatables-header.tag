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


<%@ tag body-content="empty" description="dataTables library imports. SHOULD BE IMPORTED BEFORE DATATABLE USAGE" %>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/datatables/jquery.dataTables.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.tablednd_0_5.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.datatables.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.tableDnD.ext.js"></script> 
<script type="text/javascript">
	$(function() {
		enableTableRangeSelection();
	});
</script>