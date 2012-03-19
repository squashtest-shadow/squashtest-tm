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
<%-- 

	TODO : dump this pile of **** and put something decent in place of it.
	-- bsiri
	
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="stru" uri="http://org.squashtest.csp/taglib/string-utils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>

<%@ attribute name="workspace" description="Optional page foot" required="true"%>
<%@ attribute name="linkable" description="Optional page foot" %>

<s:url var ="searchUrl" value="/search/${workspace}s" />
<s:url var ="searchReqUrl" value="/search/requirements" />
<s:url var ="searchTCUrl" value="/search/test-cases" />
<s:url var ="loadEntityUrl" value="/${workspace}s" />

<f:message var="InputFailMessage" key='search.validate.failure.label'/>
<f:message var="InputEmptyMessage" key='search.validate.empty.label'/>
<script type="text/javascript">
	
	//Array with selected ids
	selectionSearch = new Array();
	
	//multiple selection
	var firstIndex;
	var lastIndex;
	
	$(function(){
		
		setup();	
		getOldSearch();
		$( "#tabbed-pane" ).bind( "tabsshow", function(event, ui) {
			<c:if test="${workspace == 'requirement'}">
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				if (selected == 1){
					$("#export-link").css("visibility", "hidden");
				}else{
					$("#export-link").css("visibility", "visible");
				}
			</c:if>
		});
		
		$("#search-button").button().bind("click", function(){
			<c:if test="${ workspace eq 'requirement' || linkable eq 'requirement' }" >
				if (!verifyParams()){
					return;
				}
			</c:if>
			research($("#searchName").val(), $('#project-view').attr('checked'));
			$.cookie('search${workspace}', $("#searchName").val());	
		});
		
		$("#sortParam").change(function(){
			var option = $("#sortParam option:selected");
			sortBy(option.attr('value'));
		});
	});
	
	function sortBy(value){
		var table = $("#search-result-datatable").dataTable();
		table.fnSort([[parseInt(value),'asc']]);
	}
	
	function setup(){
		<c:choose>
		<c:when test="${ workspace eq 'requirement' || linkable eq 'requirement' }" >
			<c:choose>
			<c:when test="${linkable eq 'test-case'}" >
				$("#search").addClass("search-div");
			</c:when>
			<c:otherwise>
				$("#search").addClass("search-div-requirement");
			</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			$("#search").addClass("search-div");
		</c:otherwise>
		</c:choose>
	}
	
	function verifyParams(){
		var aString = $("#searchName").val();
		var reference = $("#searchReference").val();
		
		if (aString.length == 0 && reference.length == 0 && !testEmptyCriticality ()) {
			alert("${InputEmptyMessage}");
			return false;
		}
		
		if (aString.length != 0)
		{
			if (aString.length < 3 ){
				alert("${InputFailMessage}");
				return false;
			}
		}
		return true;
	}
	
	function getOldSearch(){
		var oldSearch = $.cookie('search${workspace}');
		if ( oldSearch != null && oldSearch != "undefined"){
			$("#searchName").val(oldSearch);
		}
	}
	
	function research(name, order){
		var rename = name;
		var url;
		var data = {};
		
		<c:choose>
		<c:when test="${ (workspace eq 'requirement' || linkable eq 'requirement') }" >
			data['name'] = rename;
			data['criticalities'] = getCriticalityParams();
			data['reference'] = $('#searchReference').val();
			data['verification'] = $('#requirementVerification').val();
			data['order'] = order;
			url = '${searchReqUrl}';
		</c:when>
		<c:otherwise>
			if (document.getElementById("linkable-test-cases-tree") != null){
				data['name'] = rename;
				data['order'] = order;
				data['importance'] = getImportanceParams();
				url = '${searchTCUrl}';
			}
			else{
				data['name'] = rename;
				data['order'] = order;
				data['importance'] = getImportanceParams();
				url = '${searchUrl}';
			}
		</c:otherwise>
		</c:choose>
		
		<c:if test="${ (workspace eq 'requirement' && linkable eq 'test-case') }" >
			data['name'] = rename;
			data['order'] = order;
			data['importance'] = getImportanceParams();
			url = '${searchTCUrl}';
		</c:if>
		
		<%-- the following is just more wtf on the pile of wtf, I don't care anymore --%>
		if (data['importance']!=null&&data['importance'].length==0) delete data['importance'];

		<%-- load with data issues a POST --%>
		$.get(url, data, function(data) {
			$("#search-result-pane").html(data);
			bindSearchNodeHandler();
			bindSwitchToTreePanelHandler();
		});
		
	}
	
	function bindSearchNodeHandler(){
		$(".non-tree").hover(
			function () {
			   $(this).addClass("ui-state-active jstree-hovered");
			}, 
			function () {
				$(this).removeClass("ui-state-active jstree-hovered");
			}
		);
		
		$("td.non-tree").bind("click", function(e){
			
			if ( verifyIfProject($(this).attr("id"))){
				return;
			}
			<c:choose>
			<c:when test="${linkableTestCase != 'null' || linkable eq 'requirement'}">	
				if(selectedTab == 2 && e.shiftKey){
						multipleSelectDevice(this);
				}else{
					if ($(this).parent("tr").hasClass("ui-state-row-selected")){
						//deselect
						//remove the id
						removeItem(getRowId($(this).attr("id")));
						//update css
						$(this).parent("tr").removeClass("ui-state-row-selected");
						$(this).removeClass("jstree-clicked ui-state-default");
						
					} else {
						//select
						//push the selected id
						getIdSelection().push(getRowId($(this).attr("id")));
						//update css
						$(this).parent("tr").addClass("ui-state-row-selected");
						$(this).addClass("jstree-clicked ui-state-default");
						//nonetheless, set the first index for multiple selection
	 					firstIndex = $(this).parent("tr").prevAll().length;
					}
				}
			</c:when>
			<c:otherwise>
				$(".non-tree").each(function(){
					$(this).removeClass("jstree-clicked ui-state-default");
				});
				$(this).addClass("jstree-clicked ui-state-default");
				
				var url = "${loadEntityUrl}/" + getRowId($(this).attr("id"));
				
				$.get(url, function(data) {
					$("#contextual-content").html(data);	
				});
			</c:otherwise>
			</c:choose>
		});
	}
	
	function findIdsOfSelectedSearchRow(){
		var dataTable = $("#search-result-datatable").dataTable();
		var rows = dataTable.fnGetNodes();
		var ids = new Array();

		for(var i =0; i < rows.length ; i++){
			var row = rows[i]; 
			var td = $("td:eq(0)", row);
			if (td.is('.non-tree.jstree-clicked')) {
				var data = dataTable.fnGetData(row);
				ids.push(data[0]);
			}
		}
		return ids;		
	}
	
	function verifyIfProject(objectId){
		if (objectId.match("Library") != null){
			return true;
		}
		return false;
	}
	function getRowId(tableRowId){
		var pos = tableRowId.indexOf("-");
		var first = tableRowId.substring(pos + 1);
		var nextPos = first.indexOf("-");
		var theId = first.substring(nextPos + 1);
		return theId;
	}
	
	function selectTreeNodeFromSearchNode(searchNodeDomId){
		var offset = "searchnode-";
		var treeName = searchNodeDomId.substring(offset.length);
		
		<c:if test="${linkable eq 'test-case'}">
			var treeNode = $("#linkable-test-cases-tree li[id=\'"+treeName+"\']");
			jqTree=$("#linkable-test-cases-tree");
			jqTree.jstree("deselect_all");
			jqTree.jstree("select_node",treeNode);
			return;
		</c:if>
		
		<c:if test="${linkable eq 'requirement'}">
			var treeNode = $("#linkable-requirements-tree li[id=\'"+treeName+"\']");
			jqTree=$("#linkable-requirements-tree");
			jqTree.jstree("deselect_all");
			jqTree.jstree("select_node",treeNode);
			return;
		</c:if>
		
		var treeNode = $("#tree li[id=\'"+treeName+"\']");
		jqTree=$("#tree");
		jqTree.jstree("deselect_all");
		jqTree.jstree("select_node",treeNode);
	}
	
	function bindSwitchToTreePanelHandler(){
		$("td.non-tree").bind("dblclick", function(){
			selectTreeNodeFromSearchNode($(this).attr("id"));
			$("#tabbed-pane").tabs("select", 0);
		});
	}
	
// 	<c:if test="${ (workspace eq 'requirement' || linkable eq 'requirement') || (workspace != 'requirement' || linkable eq 'test-case')}">
	function getCriticalityParams (){
		var critValue = [];
		critValue[0] = $('#crit-1').attr('checked');
		critValue[1] = $('#crit-2').attr('checked');
		critValue[2] = $('#crit-3').attr('checked');
		critValue[3] = $('#crit-4').attr('checked');
		return critValue;	
	}
	
	function getImportanceParams(){
		return $(".search-panel-tc-importance input:checked").collect(function(elt){
			return $(elt).data('value');
		});
	}
	
	function testEmptyCriticality (){
		var value = getCriticalityParams (); 
		for(var i = 0; i < value.length; i++)
		{
		    if (value[i] == true){
		    	return true;
		    }
		}
		return false;
	}
	
// 	</c:if>
		function showHideReqProperties(){
		if (testLinkableRequirementBoolean() != "true" && "${workspace}" != "requirement"){
			$(".requirementCriterion").remove();
		}
		if ("${workspace}" == "requirement" && linkable == "test-case" && document.getElementById("linkable-test-cases-tree") != null ){
			$(".requirementCriterion").remove();
		}
	}
	
	//multiple selection only for tc by requirement research
	//selection : the selected item
	function multipleSelectDevice(selection){
		var tr = $(selection).parent("tr");
		var rowIndex = tr.prevAll().length;
		if(firstIndex == undefined){
			firstIndex = rowIndex;
			//update css
			tr.addClass("ui-state-row-selected");
			$(selection).addClass("jstree-clicked ui-state-default");
			//push the id in selection
			getIdSelection().push(getRowId($(selection).attr("id")));
		}
		else{
			lastIndex = rowIndex;
			//where is the start ? i.e. is firstIndex > lastIndex ?
			var start = (firstIndex<lastIndex)?firstIndex:lastIndex;
			var end = (firstIndex<lastIndex)?lastIndex:firstIndex;
			//change all selected rows
			for(var i = start; i <= end ; i++){
				var el = $('#by-req-search-result-datatable td.non-tree:eq('+(i)+')');
				el.addClass("jstree-clicked ui-state-default");
				el.parent("tr").addClass("ui-state-row-selected");
				getIdSelection().push(getRowId(el.attr("id")));
			}
			//reset
			firstIndex = null;
			lastIndex = null;
		}
	}
	
	<%--  
		In test plan management, there's the classic search interface and the possibility to find test plan by requirement.
		In this case, there's two div used to display the results. 
		some javascript variables (like selectedTab) are set in tree-page-layout
	--%> 
	
	//get the proper ids array which corresponds to the selected TC in research 
	function getIdSelection()
	{
		var toReturn = selectionSearch;
			if(selectedTab == 2){
				toReturn = selectionSearchByReq;
			}
		return toReturn;
	}
	
	//Remove an id from a selection array
	function removeItem(idToRemove)
	{
		if(selectedTab == 2){
			selectionSearchByReq = jQuery.grep(selectionSearchByReq, function(n){
			  return n != idToRemove;
			});
		}
		else{
			selectionSearch = jQuery.grep(selectionSearch, function(n){
			  return n != idToRemove;
			});
		}
	}

</script>

<div id="search-input">
	<table>
		<c:if test="${ (workspace eq 'requirement' || linkable eq 'requirement')}">
		<c:if test="${ not empty linkable && linkable != 'test-case' }">
		<tr id="requirementReference" class="requirementCriterion"> <td> <span class="gray-text"> <f:message key="search.reference.label" /> </span> : <input id="searchReference" type="text" class="std-height snap-right" style="width: 66%;" /> </td> </tr>
		</c:if>
		</c:if>
		<tr> <td> 
			<span class="gray-text"> <f:message key="search.name.label" /> </span> : <input id="searchName" type="text" class="std-height snap-right" style="width: 66%;margin-left: 2em;" /> 
		</td> </tr>
		
		<c:if test="${ (workspace eq 'requirement' || linkable eq 'requirement')}">
		<c:if test="${ not empty linkable && linkable != 'test-case' }">
		<tr> <td>
			<div id="requirementProperties" class="requirementCriterion">
			
				<span class="gray-text"><f:message key="requirement.criticality.label" /> :</span>
				<table>
					<tr>
						<td class="requirement-UNDEFINED">
							<span> <input type="checkbox" id="crit-1" value="1"/> <span> <f:message key="requirement.criticality.UNDEFINED" /> </span> </span>
						</td>
						<td class="requirement-MINOR">
							<span> <input type="checkbox" id="crit-2" value="2"/> <span> <f:message key="requirement.criticality.MINOR" /> </span> </span>
						</td>
					</tr>
					<tr>
						<td class="requirement-MAJOR">
							<span> <input type="checkbox" id="crit-3" value="3"/> <span> <f:message key="requirement.criticality.MAJOR" /> </span> </span>
						</td>
						<td class="requirement-CRITICAL">
							<span > <input type="checkbox" id="crit-4" value="4" /> <span> <f:message key="requirement.criticality.CRITICAL" /> </span></span>
						</td>
					</tr>
				</table>
			</div>
			<div class="requirementCriterion">
				<select id="requirementVerification">
					<c:forEach var="verificationCriterion" items="${ verificationCriterionEnum }" varStatus="status">
						<c:if test="${ status.first }">
							<option value="${ verificationCriterion }" selected="selected"><f:message key="${ verificationCriterion.i18nKey }" /></option>
						</c:if>
						<c:if test="${ not status.first }">
							<option value="${ verificationCriterion }" ><f:message key="${ verificationCriterion.i18nKey }" /></option>
						</c:if>
					</c:forEach>
				</select> 
			</div>			
		</td> </tr>
		</c:if>
		</c:if>
		
		
		<c:if test="${((workspace eq 'test-case' || linkable eq 'test-case' )&& linkable != 'requirement')}">		
			<tr> <td>
				<div class="search-panel-tc-importance">
					<div class="caption">
						<span class="gray-text"><f:message key="search.test-case.importance.filter"/></span>
					</div><div class="options">
						<div class="search-tc-importance-1"><input type="checkbox" id="importance-1" data-value="LOW"/><span><f:message key="test-case.importance.LOW"/></span></div>
						<div class="search-tc-importance-2"><input type="checkbox" id="importance-2" data-value="MEDIUM"/><span><f:message key="test-case.importance.MEDIUM"/></span></div>				
						<div class="search-tc-importance-3"><input type="checkbox" id="importance-3" data-value="HIGH"/><span><f:message key="test-case.importance.HIGH"/></span></div>
						<div class="search-tc-importance-4"><input type="checkbox" id="importance-4" data-value="VERY_HIGH"/><span><f:message key="test-case.importance.VERY_HIGH"/></span></div>
					</div>
				</div>
			</td></tr>		
		</c:if>		
		
		<tr> <td>
			<input type="checkbox" id="project-view" /> <span class="gray-text"> <f:message key="search.project.view"/> </span>
		</td> </tr>
		<f:message key="search.button.label" var="searchLabel"/>
		<tr> <td style="text-align: center;"> <input type="button" id="search-button" value="${ searchLabel }"/> </td> </tr>

		
		<c:if test="${ (workspace eq 'requirement' || linkable eq 'requirement')}">
		<c:if test="${ not empty linkable && linkable != 'test-case' }">
		<tr> <td> 
			 <div id="sortingProperties" class="requirementCriterion">
				<span><f:message key="search.sort.choose.label" /></span>
				<select id="sortParam">
					<option value="4" selected="selected"><f:message key="search.name.label" /></option>
					<option value="3" ><f:message key="search.reference.label" /></option>
					<option value="2" ><f:message key="search.criticality.label" /></option>
				</select> 
			</div>
		</td> </tr>
		</c:if>
		</c:if>
	</table>
</div>


<div id="search"> 
	<div id="search-result-pane"> </div>
</div>

<comp:decorate-buttons />
