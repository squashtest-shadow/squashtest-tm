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
<%@ attribute name="rootModel" required="true" type="java.lang.Object" description="JSON serializable model of root of tree" %>
<%@ attribute name="workspaceType" required="true" %>

<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="su" uri="http://org.squashtest.tm/taglib/string-utils" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>


<tree:_html-tree treeId="tree" />

<script type="text/javascript">
	$(function () {
		
		require(['tree'], function(treemaker){
			
			var conf = {
				model : ${ json:serialize(rootModel) },
				workspace : "${workspaceType}",
				treeselector : "#tree"
			};
			
			treemaker.initWorkspaceTree(conf);
			
		});
	});	
</script>
