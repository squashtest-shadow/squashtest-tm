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
<%@ tag body-content="empty" description="Displays message if other user is viewing the same object" %>
<%@ attribute name="objectUrl" required="true" %>
<%@ attribute name="otherViewers" required="false" type="java.lang.Boolean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${ otherViewers }">
<div style="color: #7E0426; display: block; text-align: center;"><p><f:message key="squashtm.generic.opened-object.quit.message"/></p></div>
</c:if>
<script>
function quitTestCase (){
	 $.ajax({
			type : 'DELETE',
			url : '${objectUrl}'+'/opened-entity'
});
}
window.onbeforeunload = quitTestCase;
if(squashtm.contextualContent){
squashtm.contextualContent.onCleanContent = quitTestCase;
}
</script>
