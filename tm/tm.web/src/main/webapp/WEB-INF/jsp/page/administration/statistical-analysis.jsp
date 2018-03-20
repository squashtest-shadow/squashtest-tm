<%--

        This file is part of the Squashtest platform.
        Copyright (C) Henix, henix.fr

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
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url var="administrationUrl" value="/administration"/>

<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="addLabel" key="label.Add"/>
<f:message var="cancelLabel" key="label.Cancel"/>
<f:message var="addClientTitle" key="label.addClientTitle"/>
<f:message var="deleteClientTitle" key="label.deleteClientTitle"/>

<layout:info-page-layout titleKey="label.StatisticsAnalysis" isSubPaged="true" main="advanced-config-page">
  <jsp:attribute name="head">
  <script type="text/javascript">
    squashtm = squashtm || {};
    squashtm.appRoot = '<c:url value="/" />';
  </script>
  <comp:sq-css name="squash.grey.css"/>
  </jsp:attribute>

  <jsp:attribute name="titlePane">
  <h2 class="admin">
    <f:message key="label.administration"/>
  </h2>
  </jsp:attribute>

  <jsp:attribute name="subPageTitle">
  <h2>
    <f:message key="label.StatisticalAnalysis"/>
  </h2>
  </jsp:attribute>

  <jsp:attribute name="subPageButtons">
  <a class="sq-btn" href="${administrationUrl}"><f:message key="label.Back"/></a>
  </jsp:attribute>

  <jsp:attribute name="footer"/>
  <jsp:attribute name="informationContent">
  <c:url var="clientsUrl" value="/administration/config/clients/list"/>
  <c:url var="addClientUrl" value="/administration/config/clients"/>

  <div id="statistics-page-content" class="admin-message-page-content">

    <div id="statistics-info-panel" class="expand sq-tg">
      <div class="tg-head">
        <h3><f:message key="label.database.size"/></h3>
      </div>

      <div class="tg-body">
      </div>


    </div>

  </jsp:attribute>
</layout:info-page-layout>
