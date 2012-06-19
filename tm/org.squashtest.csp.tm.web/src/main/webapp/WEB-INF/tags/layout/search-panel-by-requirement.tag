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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<f:message var="InputFailMessage" key='search.validate.failure.label' />
<f:message var="InputEmptyMessage" key='search.validate.empty.label' />

<s:url var ="searchUrl" value="/search/tc-by-requirement" />
<s:url var ="loadEntityUrl" value="/test-cases" />

<script type="text/javascript">

	//Array with selected ids
	selectionSearchByReq = new Array();
	
	function setupReq(){
		$("#byReqSearch").addClass("search-div");
		$("#byReqSearch").addClass("search-div-requirement");
	}
	
	//add the event on research button and sort options change
	$(function() {
		setupReq();
		//Set the search input to last entered value
		byReqGetOldSearch();
		
		$("#byReqSearch-button").button().bind(
				"click",
				function() {

					if (!byReqVerifyParams()) {
						return;
					}
					byReqResearch($("#byReqSearchName").val(), $('#byReqProject-view').attr(
							'checked'));
					//save the research to pre-fill the field for the next research
					$.cookie('searchByReq${workspace}', $("#byReqSearchName").val());
				});

		$("#byReqSortParam").change(function(){
			var option = $("#byReqSortParam option:selected");
			byReqSortBy(option.attr('value'));
		});
	});
	
	//Sort only by requirement dataTable
	function byReqSortBy(value){
		var table = $("#by-req-search-result-datatable").dataTable();
		table.fnSort([[parseInt(value),'asc']]);
	}

	//Check param only for by requirement research
	function byReqVerifyParams() {
		var aString = $("#byReqSearchName").val();
		var reference = $("#searchReference").val();

		if (aString.length == 0 && reference.length == 0
				&& !testEmptyCriticality() && !testEmptyCategory() ) {
			$.squash.openMessage("<f:message key='popup.title.error' />", "${InputEmptyMessage}");
			return false;
		}

		if (aString.length != 0) {
			if (aString.length < 3) {
				$.squash.openMessage("<f:message key='popup.title.error' />", "${InputFailMessage}");
				return false;
			}
		}
		return true;
	}

	//Launch the research
	function byReqResearch(name, order) {
		var rename = name;
		var url
		var data = {}
		data['name'] = rename;
		data['criticalities'] = getCriticalityParams();
		data['categories'] = getCategoryParams();
		data['reference'] = $('#searchReference').val();
		data['order'] = order;
		url = '${searchUrl}';
		
		//load with data issues a POST
		$.get(url, data, function(data) {
			$("#byReqSearch-result-pane").html(data);
			bindSearchNodeHandler();
			bindSwitchToTreePanelHandler();
		});

	}
	
	//pre-fill search field
	function byReqGetOldSearch(){
		var oldSearch = $.cookie('searchByReq${workspace}');
		if ( oldSearch != null && oldSearch != "undefined"){
			$("#byReqSearchName").val(oldSearch);
		}
	}
	
</script>

<div id="by-requirement-search-input">
	<table>

		<tr id="requirementReference">
			<td><span class="gray-text"> <f:message
						key="search.reference.label" /> </span> : <input
				id="searchReference" type="text" class="std-height snap-right"
				style="width: 66%;" /></td>
		</tr>

		<tr>
			<td><span class="gray-text"> <f:message
						key="search.name.label" /> </span> : <input id="byReqSearchName" type="text"
				class="std-height snap-right" style="width: 66%; margin-left: 2em;" />
			</td>
		</tr>

		<tr>
			<td>
				<div id="requirementProperties">

					<span class="gray-text"><f:message
							key="requirement.criticality.label" /> :</span>
					<table>
						<tr>
							<td class="requirement-UNDEFINED"><span> <input
									type="checkbox" id="crit-1" value="1" /> <span> <f:message
											key="requirement.criticality.UNDEFINED" /> </span> </span></td>
							<td class="requirement-MINOR"><span> <input
									type="checkbox" id="crit-2" value="2" /> <span> <f:message
											key="requirement.criticality.MINOR" /> </span> </span></td>
						</tr>
						<tr>
							<td class="requirement-MAJOR"><span> <input
									type="checkbox" id="crit-3" value="3" /> <span> <f:message
											key="requirement.criticality.MAJOR" /> </span> </span></td>
							<td class="requirement-CRITICAL"><span> <input
									type="checkbox" id="crit-4" value="4" /> <span> <f:message
											key="requirement.criticality.CRITICAL" /> </span>
							</span></td>
						</tr>
					</table>
					<span class="gray-text"><f:message
							key="requirement.category.label" /> :</span>
					<table>
						<tr>
							<td ><span> <input
									type="checkbox" id="cat-1" value="1" /> <span> <f:message
											key="requirement.category.FUNCTIONAL" /> </span> </span></td>
							<td ><span> <input
									type="checkbox" id="cat-2" value="2" /> <span> <f:message
											key="requirement.category.NON_FUNCTIONAL" /> </span> </span></td>
							</tr>
						<tr>					<td ><span> <input
									type="checkbox" id="cat-3" value="2" /> <span> <f:message
											key="requirement.category.USE_CASE" /> </span> </span></td>
					
							<td ><span> <input
									type="checkbox" id="cat-4" value="3" /> <span> <f:message
											key="requirement.category.BUSINESS" /> </span> </span></td>
							</tr>
						<tr>	<td ><span> <input
									type="checkbox" id="cat-5" value="4" /> <span> <f:message
											key="requirement.category.TEST_REQUIREMENT" /> </span>
							</span></td>
							<td ><span> <input
									type="checkbox" id="cat-6" value="4" /> <span> <f:message
											key="requirement.category.UNDEFINED" /> </span>
							</span></td>
						</tr>
					</table>
				</div>
			</td>
		</tr>

		<tr>
			<td>
			<input type="checkbox" id="byReqProject-view" /> <span
				class="gray-text"> <f:message key="search.project.view" /> </span></td>
		</tr>
		<f:message key="search.button.label" var="searchLabel" />
		<tr>
			<td style="text-align: center;"><input type="button"
				id="byReqSearch-button" value="${ searchLabel }" /></td>
		</tr>
		<tr>
			<td>
				<div id="byReqSortingProperties">
					<span><f:message key="search.sort.choose.label" />
					</span> <select id="byReqSortParam">
						<option value="3" selected="selected">
							<f:message key="search.name.label" />
						</option>
						<option value="2">
							<f:message key="search.criticality.label" />
						</option>
					</select>
				</div></td>
		</tr>
	</table>
</div>


<div id="byReqSearch">
	<div id="byReqSearch-result-pane"></div>
</div>

<comp:decorate-buttons />