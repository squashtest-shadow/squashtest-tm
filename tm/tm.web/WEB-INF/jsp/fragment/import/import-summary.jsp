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
<!-- The form.ajaxSubmit() fakes to ask for json while it needs text/html. Chrome and FF handle well a repsonse in json but not IE. -->
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<p>${'{' } "success" : ${ summary.success }, "total" : ${ summary.total }, "rejected" : ${ summary.rejected }, "failures" : ${ summary.failures }, "modified" : ${ summary.modified }, "renamed" : ${ summary.renamed } ${'}'}</p>