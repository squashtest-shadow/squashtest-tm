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

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>

// -------------------- some shorthands for locale in .js---------------------------------------------
squashtm.message = squashtm.message || {};
squashtm.message.cancel = "<f:message key='label.Cancel'/>";
squashtm.message.placeholder = "<f:message key='rich-edit.placeholder'/>";
squashtm.message.confirm = "<f:message key='label.Confirm'/>";		
squashtm.message.intoTitle = "<f:message key='popup.title.info'/>";		
squashtm.message.errorTitle = "<f:message key='popup.title.error'/>";	


// ------------------- prefilled translator cache ---------------------------------------------------- 

var _langcache = {
	'label.Ok' : "<f:message key='label.Ok'/>",
	'label.Cancel' : "<f:message key='label.Cancel'/>",
	'squashtm.locale' : "<f:message key='squashtm.locale'/>",
	'rich-edit.language.value' : "<f:message key='rich-edit.language.value'/>"
}

squashtm.message.cache = _langcache;
