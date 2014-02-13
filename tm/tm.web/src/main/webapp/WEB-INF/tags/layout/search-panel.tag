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
<%-- 

	TODO : dump this pile of **** and put something decent in place of it.
	-- bsiri
	
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="stru"
	uri="http://org.squashtest.tm/taglib/string-utils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="workspace" 	required="true"%>
<%@ attribute name="linkable" %>

<s:url var="searchUrl" value="/search/${workspace}s" />
<s:url var="breadCrumbUrl" value="/search/${workspace}s/breadcrumb" />
<s:url var="testCaseBreadCrumbUrl" value="/search/test-cases/breadcrumb" />
<s:url var="requirementBreadCrumbUrl"
	value="/search/requirements/breadcrumb" />
<s:url var="searchReqUrl" value="/search/requirements" />
<s:url var="searchTCUrl" value="/search/test-cases" />

<s:url var="baseURL" value="/" />

<f:message var="InputFailMessage" key='search.validate.failure.label' />
<f:message var="InputEmptyMessage" key='search.validate.empty.label' />
<script type="text/javascript">
	
	//Array with selected ids
	selectionSearch = [];
	
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
			<c:if test="${ (workspace eq 'requirement' && linkable != 'test-case') || linkable eq 'requirement' }" >
				if (!verifyParams()){
					return;
				}
			</c:if>
			research($("#searchName").val(), $('#project-view').is(':checked'));
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
		$("#search").addClass("search-div");
		<c:if test="${ (workspace eq 'requirement' && linkable != 'test-case') || linkable eq 'requirement' }" >
		$("#search").addClass("search-div-requirement");
		</c:if>
		<c:if test="${ workspace eq 'campaign' && empty linkable}" >
		$("#search").addClass("search-div-campaign");
		</c:if>
		<c:if test="${ workspace eq 'test-case' || linkable eq 'test-case' }" >
		$("#search").addClass("search-div-testcase");
		</c:if>
	}
	
	function verifyParams(){
		var aString = $("#searchName").val();
		var reference = $("#searchReference").val();
		
		if (aString.length == 0 && reference.length == 0 && !testEmptyCriticality () && !testEmptyCategory ()) {
			$.squash.openMessage("<f:message key='popup.title.error' />", "${InputEmptyMessage}");
			return false;
		}
		
		if (aString.length != 0)
		{
			if (aString.length < 3 ){
				$.squash.openMessage("<f:message key='popup.title.error' />", "${InputFailMessage}");
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
			data['categories'] = getCategoryParams();
			data['reference'] = $('#searchReference').val();
			data['verification'] = $('#requirementVerification').val();
			data['order'] = order;
			url = '${searchReqUrl}';
		</c:when>
		<c:when test="${ (workspace eq 'test-case' || linkable eq 'test-case') }">
			data['name'] = rename;
			data['order'] = order;
			data['importance'] = getImportanceParams();
			data['nature'] = getNatureParams();
			data['type'] = getTypeParams();
			data['status'] = getStatusParams();
			url = '${searchTCUrl}';
			
			<%-- the following is just more wtf on the pile of wtf, I don't care anymore --%>
			if (data['importance']!=null&&data['importance'].length==0) delete data['importance'];
		</c:when>
		<c:when test="${ workspace eq 'campaign' && empty linkable}">
				data['name'] = rename;
				data['order'] = order;
				url = '${searchUrl}';
		</c:when>
		</c:choose>
		
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
			<c:when test="${not empty linkable}">	
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
				
				var url = getEntityURL($(this));
				
				squashtm.workspace.contextualContent.loadWith(url);
				
				<%--
				$.get(url, function(data) {
					$("#contextual-content").html(data);	
				});
				--%>
			</c:otherwise>
			</c:choose>
		});
	}
	
	function getEntityURL(jqRow){
		var expr = /([a-z])(?=[A-Z])/g
		var idParts = jqRow.attr("id").split("-");
		
		var lowerCase = idParts[1].replace(expr, "$1-").toLowerCase()+"s";
		var id = idParts[2];
		
		return "${baseURL}/"+lowerCase+"/"+id;
	} 
	
	
	function findIdsOfSelectedSearchRow(){
		var dataTable = $("#search-result-datatable").dataTable();
		var rows = dataTable.fnGetNodes();
		var ids = [];

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
		var treeNodeName = searchNodeDomId.substring(offset.length);
		
		<c:if test="${linkable eq 'test-case'}">
			var treeNode = $("#linkable-test-cases-tree li[id=\'"+treeNodeName+"\']");
// 			console.log("treenode, ="+treeNode+" linkable = ${linkable}");
			if(treeNode[0] == null){
					openLinkableTreeToReachTestCaseTreeNodeAndOpenIt(treeNodeName );
			}else{
			jqTree=$("#linkable-test-cases-tree");
			jqTree.jstree("deselect_all");
			jqTree.jstree("select_node",treeNode);
			}
			return;
		</c:if>
		
		<c:if test="${linkable eq 'requirement' && workspace != 'requirement'}">
			var treeNode = $("#linkable-requirements-tree li[id=\'"+treeNodeName+"\']");
// 			console.log("treenode, ="+treeNode+" linkable = ${linkable}");
			if(treeNode[0] == null){
					openLinkableTreeToReachRequirementTreeNodeAndOpenIt(treeNodeName);
			}else{
			jqTree=$("#linkable-requirements-tree");
			jqTree.jstree("deselect_all");
			jqTree.jstree("select_node",treeNode);
			}
			return;
		</c:if>
		
		var treeNode = $("#tree li[id=\'"+treeNodeName+"\']");
// 		console.log("treenode="+treeNode[0]);
		if(treeNode[0] == null){
			openTreeToReachTreeNodeAndOpenIt(treeNodeName);
			
		}else{
		jqTree=$("#tree");
		jqTree.jstree("deselect_all");
		jqTree.jstree("select_node",treeNode);
		}
		
	}
	function openLinkableTreeToReachTestCaseTreeNodeAndOpenIt(treeNodeName){
		jqTree=$("#linkable-test-cases-tree");
		findTreeBreadcrumbToNode(treeNodeName, "${testCaseBreadCrumbUrl}").done(function(data){openBreadCrumb(data, jqTree);});
	}
	function openLinkableTreeToReachRequirementTreeNodeAndOpenIt(treeNodeName){
		jqTree=$("#linkable-requirements-tree");
		findTreeBreadcrumbToNode(treeNodeName,"${requirementBreadCrumbUrl}" ).done(function(data){openBreadCrumb(data, jqTree);});
	}
	function openTreeToReachTreeNodeAndOpenIt(treeNodeName){
		jqTree=$("#tree");
		findTreeBreadcrumbToNode(treeNodeName,"${breadCrumbUrl}" ).done(function(data){openBreadCrumb(data, jqTree);});
		
	}
// 	function openLinkableTestCaseBreadCrumb(treeNodesIds){
// 		var breadCrumbLength = treeNodesIds.length;
// 		var libraryName = treeNodesIds[breadCrumbLength - 1];
// 		jqTree=$("#linkable-test-cases-tree");
// 		jqTree.jstree("deselect_all");
// 		var librayNode = jqTree.find("li[id=\'"+libraryName+"\']");
// 		  var start = breadCrumbLength -2;
// 		  jqTree.jstree("open_node",librayNode, function(){openFoldersUntillEnd(treeNodesIds,  jqTree, start);});
		
// 	}
	
// 	function openLinkableRequirementBreadCrumb(treeNodesIds, jqTree){
// 		var breadCrumbLength = treeNodesIds.length;
// 		var libraryName = treeNodesIds[breadCrumbLength - 1];
		
// 		jqTree.jstree("deselect_all");
// 		var librayNode = jqTree.find("li[id=\'"+libraryName+"\']");
// 		  var start = breadCrumbLength -2;
// 		  jqTree.jstree("open_node",librayNode, function(){openFoldersUntillEnd(treeNodesIds,  jqTree, start);});
		
// 	}
	function openBreadCrumb(treeNodesIds, jqTree){
// 		console.log("treenode="+treeNodesIds);
		var breadCrumbLength = treeNodesIds.length;
		var libraryName = treeNodesIds[breadCrumbLength - 1];
		jqTree.jstree("deselect_all");
		var librayNode = jqTree.find("li[id=\'"+libraryName+"\']");
// 		  console.log(librayNode);
		  var start = breadCrumbLength -2;
		  jqTree.jstree("open_node",librayNode, function(){openFoldersUntillEnd(treeNodesIds,  jqTree, start);});
		
		
	}
	function openFoldersUntillEnd(treeNodesIds,  jqTree, i){
		 if ( i >= 1 ) {  
// 			  console.log(i);
			  var treeNodeName = treeNodesIds[i];
			  var treeNode = jqTree.find("li[id=\'"+treeNodeName+"\']");
// 			  console.log(treeNode);
			  i--;
			  jqTree.jstree("open_node",treeNode, function(){openFoldersUntillEnd(treeNodesIds,  jqTree, i);});
		  }else{
			  var treeNodeName = treeNodesIds[i];
			  var treeNode = jqTree.find("li[id=\'"+treeNodeName+"\']");
			  jqTree.jstree("deselect_all");
			  jqTree.jstree("select_node",treeNode);
		  }
	}
		
	function findTreeBreadcrumbToNode (treeNodeName, url){
// 		console.log("on va chercher "+treeNodeName);
		var dataB = {
				'nodeName' : treeNodeName
			};
		return $.ajax({
			'url' : url,
			type : 'POST',
			data : dataB,
			dataType : 'json'
		});
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
		critValue[0] = $('#crit-1').is(':checked');
		critValue[1] = $('#crit-2').is(':checked');
		critValue[2] = $('#crit-3').is(':checked');
		critValue[3] = $('#crit-4').is(':checked');
		return critValue;	
	}
	function getCategoryParams (){
		var catValue = [];
		catValue[0] = $('#cat-1').is(':checked');
		catValue[1] = $('#cat-2').is(':checked');
		catValue[2] = $('#cat-3').is(':checked');
		catValue[3] = $('#cat-4').is(':checked');
		catValue[4] = $('#cat-5').is(':checked');
		catValue[5] = $('#cat-6').is(':checked');
		return catValue;	
	}
	
	function getImportanceParams(){
		return $(".search-panel-tc-importance input:checked").collect(function(elt){
			return $(elt).data('value');
		});
	}
	
	function getNatureParams(){
		return $(".search-panel-tc-nature input:checked").collect(function(elt){
			return $(elt).data('value');
		});
	}
	
	function getTypeParams(){
		return $(".search-panel-tc-type input:checked").collect(function(elt){
			return $(elt).data('value');
		});
	}
	
	function getStatusParams(){
		return $(".search-panel-tc-status input:checked").collect(function(elt){
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
	function testEmptyCategory (){
		var value = getCategoryParams (); 
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
	
		<c:if test="${!((workspace eq 'test-case' || workspace eq 'requirement' || linkable eq 'test-case' )&& linkable != 'requirement')}">		
		<tr>
			<td><span class="gray-text"> <f:message	key="label.Name" /> </span> : 
			<input id="searchName" type="text"
				class="std-height snap-right" style="width: 66%; margin-left: 2em;" />
			</td>
		</tr>
		</c:if>
		

		<c:if test="${!((workspace eq 'test-case' || workspace eq 'requirement' || linkable eq 'test-case' )&& linkable != 'requirement')}">

		<tr>
			<td><input type="checkbox" id="project-view" /> <span
				class="gray-text"> <f:message key="search.project.view" /> </span>
			</td>
		</tr>
		<f:message key="search.button.label" var="searchLabel" />
		<tr>
			<td style="text-align: center;"><input type="button"
				id="search-button" value="${ searchLabel }" />
			</td>
		</tr>
		</c:if>
	</table>
</div>

<c:if test="${!((workspace eq 'test-case' || workspace eq 'requirement' || linkable eq 'test-case' )&& linkable != 'requirement')}">		
<div id="search">
	<div id="search-result-pane"></div>
</div>
</c:if>
