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
<%@ tag description="A dialog popup. The body of this tag contains the HTML body of the dialog. 
	Buttons and their bound actions are defined through a fragment passed as the 'buttons' attribute" %>
<%@ attribute name="openedBy" required="true" description="id of the button which opens the dialog" %>
<%@ attribute name="id" required="true" description="id of the popup" %>
<%@ attribute name="titleKey" %>
<%@ attribute name="width" %>
<%@ attribute name="isContextual" description="if set, this popup will be added a class to show it belongs to the contextual panel"%>
<%@ attribute name="buttons" fragment="true" required="true" %>
<%@ attribute name="closeOnSuccess" description="Closes the popup on ajax request success. Default is true." %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<%-- POPUP DEPRECIEE --%>

<h1 style="font-size:60em">COMP:POPUP IS OBOSLETE DON'T YOU USE IT AGAIN YOU F</h1> 