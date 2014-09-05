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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>


<f:message var="renameDialogTitle" key="dialog.rename-test-case.title"/>
<f:message var="renameButtonLabel" key="dialog.rename-test-case.title"/>
<f:message var="cancelLabel" key="label.Cancel" />


<%---------------------------- Rename test case popup ------------------------------%>

<%-- 
  
  That div is important because of the use of jquery.squash.formDialog, I'll explain
  why here.
  
  When an element in the DOM is turned into a jquery dialog it is detached then 
  attached to the <body>. Which means that, in the main view ('library'), when 
  one navigate from one test case to another the popup is not removed along 
  the rest of this part of the document.
  
  This commonly leads to 'widget leaks' because the more test cases are displayed 
  and the more dialogs leaks to the body. This is especially tricky because 
  those dialogs have all the same ID but jquery won't care and when selecting 
  a dialog by ID it'll just pick the first one it finds.
  
  It may also keep alive javascript handles that manage the data of test cases
  that are no longer displayed, possibly like in Issue 3474.
  
  Our custom widget jquery.squash.formDialog ensures that such leak cannot happen. 
  When a DOM element is turned to a dialog, it registers itself as a listener on 
  its immediate DOM parent before it is attached to <body>. That way, upon destruction 
  of the parent, the listener will also destroy and remove the dialog.
  
  Henceforth, the <div class="not-displayed"> acts as this parent. When another test 
  case is displayed, this div will be removed and thus trigger the destruction of the 
  dialogs it contains. 
  
  Hadn't it be there, the dialogs would depend on the  <div id="contextual-content">, 
  which is never removed, and thus the dialogs would never be destroyed and removed.

 --%>
<div class="not-displayed">

<c:if test="${ writable }">

<div id="rename-test-case-dialog" title="${renameDialogTitle}" class="popup-dialog not-displayed">
	
	<div>
		<label><f:message key="dialog.rename.label" /></label>
		<input type="text" id="rename-test-case-input"  maxlength="255"	size="50" />
		<br />
		<comp:error-message forField="name" />
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${renameButtonLabel}" data-def="evt=confirm, mainbtn"/>
		<input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
	</div>
</div>

</c:if>

</div>

