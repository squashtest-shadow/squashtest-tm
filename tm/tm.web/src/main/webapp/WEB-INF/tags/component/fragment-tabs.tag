<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag description="activation of jquery-ui tabs"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="beforeLoad" required="false" description="javascript function identifier. if set, will add a beforeLoad hook to the configuration of the tabs." %>
<%@ attribute name="cacheRequests" type="java.lang.Boolean"  required="false"  description="boolean. if set, will cache the ajax calls to the server to prevent multiple reload. Note that unfortunately it will void any 'beforeLoad' attribute. Default is false." %>

 
<script type="text/javascript">
require(["common"], function() {
  require(["jquery","jquery.squash.fragmenttabs"], function($, Frag){
		$(function() {
			var init = {};
			
			<c:if test="${not empty beforeLoad}">
			init.beforeLoad = ${beforeLoad};
			</c:if>
			<c:if test="${not empty cacheRequests and cacheRequests}">
			init.beforeLoad = Frag.confHelper.fnCacheRequests;
			</c:if>
			
			Frag.init(init);
		});
	});
});
</script>
