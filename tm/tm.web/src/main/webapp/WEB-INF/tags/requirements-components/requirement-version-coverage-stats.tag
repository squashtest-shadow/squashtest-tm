<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<%@ tag body-content="empty" description="show coverage stats" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%-- ======================== VARIABLES & URLS ============================ --%>

<f:message var="labelConfirm" key="label.Confirm"/>
<f:message var="labelCancel"  key="label.Cancel"/>

<f:message var="titleCoverageRequirement"  key="requirement.rate.cover.main"/>
<f:message var="titleCoverageRequirementChildren"  key="requirement.rate.cover.children"/>
<f:message var="titleCoverageRequirementAll"  key="requirement.rate.cover.all"/>


<%-- ======================== /VARIABLES & URLS ============================ --%>

<div id="coverage-stat">
	<div class="display-table">
		<div class="display-table-row">
			<label for="coverage-rate" class="display-table-cell">
				<f:message key="requirement.rate.cover" />
			</label>
			<div id="coverage-rate" class="display-table-cell">XX %</div>
		</div>
		<div class="display-table-row">
			<label for="verification-rate" class="display-table-cell">
				<f:message key="requirement.rate.verification" />
			</label>
			<div id="verification-rate" class="display-table-cell">XX %</div>
			<div class="display-table-cell">
				<input type="button" value="<f:message key='requirement.rate.perimeter.change'/>" title="<f:message key='requirement.rate.perimeter.change'/>" id="change-perimeter-button" class="sq-btn btn-sm">
			</div>
		</div>
		<div class="display-table-row">
			<label for="validation-rate" class="display-table-cell">
				<f:message key="requirement.rate.validation" />
			</label>
			<div id="validation-rate" class="display-table-cell">XX %</div>
		</div>
	</div>
	 
	<div id="dialog-select-perimeter-wrapper"></div>
</div>
<script type="text/x-handlebars-template" id="tpl-show-coverage-rate">
	<span title="${titleCoverageRequirement}">{{requirementVersionRate}} % </span>
	|<span title="${titleCoverageRequirementChildren}"> {{requirementVersionChildrenRate}} % </span>
	|<span title="${titleCoverageRequirementAll}"> {{requirementVersionGlobalRate}} % </span>
</script>

<script type="text/x-handlebars-template" id="tpl-dialog-select-perimeter">
  <div id="dialog-select-perimeter" class="not-displayed popup-dialog" title="<f:message key='requirement.rate.perimeter.title'/>">
    <div id="perimeter-tree" style="min-height: 300px;"></div>
	<div class="popup-dialog-buttonpane">
      <input type="button" class="button" value="${labelConfirm}" data-def="evt=confirm, mainbtn"/>
      <input type="button" class="button" value="${labelCancel}" data-def="evt=cancel"/>
    </div>
  </div>
</script>
<script type="text/javascript">
publish('reload.requirement.requirementversionrate');
</script>
