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
<%@ tag body-content="empty" description="popup for node deletion. Requires a tree to be present in the context."%>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>


<%@ attribute name="treeSelector" description="jQuerySelector for the tree."%>
<%@ attribute name="successCallback" description="javascript callback in case of success."%>
<%@ attribute name="resourceName" required="true" %>
<%@ attribute name="treeNodeButton" required="true" description="the javascript button that will open the dialog" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="baseUrl" value="/${resourceName}-browser/delete-nodes"/>
<c:url var="delIterationsUrl" value="/${resourceName}-browser/delete-iterations" />


<f:message var="deleteMessage" key="dialog.label.delete-nodes.label" />

<%-- onLoad code --%>
<script type="text/javascript">



$(function(){

	var dialog=$( "#delete-node-dialog" );
	dialog.bind( "dialogopen", function(event, ui) {
		
		var jqThis = $(this);
		
		initDeleteNodeDialog(jqThis)
		.then(sendDeletionSimulationRequest)
		.fail(function(jqThis){
			<f:message var="deletionDeniedLabel" key="dialog.label.delete-node.rejected" />
			jqThis.dialog("close");
			displayInformationNotification("${deletionDeniedLabel}");			
		});
	
	});
	
	
	${treeNodeButton}.click(function(){
			dialog.dialog('open');
			return false;		
	});
		
	
});	

</script>

<%-- preamble --%>

<script type="text/javascript">

function toLiNodes(jqNodes){
	return liNode(jqNodes);
}


function collectIds(jqNodes){
	var result = [];
	if (jqNodes.length==0) return result;
	jqNodes.each(function(i,elt){
		result.push($(elt).attr("resid"));
	});
	return result;
}

function areNodesIterations(vNodes){
	return vNodes.is(":iteration");
}


//returns a deferred
function initDeleteNodeDialog(jqDialog){
	
	var deferred = $.Deferred();
	
	<%-- erase previously stored data --%>
	jqDialog.data("vNodes", null);			
	
	<%-- store the selected nodes. --%> 
	var tree = $('${treeSelector}');
	var rawNodes = tree.jstree("get_selected");
	var vNodes = toLiNodes(rawNodes);			
	
	var operations = tree.jstree("allowedOperations");
	
	if (operations.match("delete")){
		jqDialog.data("vNodes", vNodes);		
		
		var areIterations = areNodesIterations(vNodes);
		jqDialog.data("iterations", areIterations);	
		
		deferred.resolve(jqDialog);
		
	}else{
		deferred.reject(jqDialog);
	}
		
	
	return deferred.promise();
}

function sendDeletionSimulationRequest(jqDialog){
	var vNodes = jqDialog.data("vNodes");
	var areIterations = jqDialog.data("iterations");
	var nodeIds = collectIds(vNodes);

	var url = (! areIterations) ? "${baseUrl}/simulate" : "${delIterationsUrl}/simulate";
	

	$.post(url, {"nodeIds[]":nodeIds})			
	.success(function(data){
		var message = data + "\n\n<b>${deleteMessage}</b>";
		jqDialog.html(message);
	})
	.fail(function(){
		jqDialog.dialog("close"); <%-- the standard failure handler should kick in, no need for further treatment here. --%>
	});
				
}
</script>



<%-- confirmation code --%>

<script type="text/javascript">

function buildParamString(vNodeIds){
	var queryString="";
	$(vNodeIds).each(function(index){
		queryString+="&nodeIds[]="+this.toString();
	});
	
	return queryString.substr(1);
}


function confirmDeletion(){
	
	var jqDialog = $( "#delete-node-dialog" );
	
	var vNodes = jqDialog.data("vNodes");
	var areIterations = jqDialog.data("iterations");
	var nodeIds = collectIds(vNodes);
	
	var url = (! areIterations) ? "${baseUrl}/confirm" : "${delIterationsUrl}/confirm";

	var params = buildParamString(nodeIds);
	
	$.ajax({
		url : url+"?"+params,
		dataType : "json",
		type : 'DELETE'
	})
	.success(function(list){			
		jqDialog.dialog("close");
		handleDeletionSuccess(list, areIterations);
		<c:if test="${not empty successCallback}">
		${successCallback}();
		</c:if>
	})
	.fail();
}
	
</script>

		
<%-- node deletion success --%>

<script type="text/javascript">

function handleDeletionSuccess(vIds, bWereIterations){
	var jqTree = $('${treeSelector}');

	var newSelected = findPrevNode(vIds, bWereIterations);
	
	removeNodes(vIds, bWereIterations);
	
	jqTree.jstree("deselect_all");
	jqTree.jstree("select_node", newSelected);				
}

function removeNodes(vIds, bWereIterations){
	var i=0;		
	var tree =  $('${treeSelector}');
	var selector;
	if (bWereIterations){
		selector=":iteration";
	}else{
		selector=":node";
	}
	
	for (i=0;i<vIds.length;i++){
		var id = vIds[i];
		var node = $("li[resid='"+id+"']"+selector, tree);  
		tree.jstree("delete_node", node);
	}
}

function findPrevNode(vIds, bWereIterations){
	var tree =  $('${treeSelector}');
	
	if (vIds.length==0) return tree.jstree("get_selected");

	var baseId = vIds[0];

	var loopNode = (bWereIterations) ? 	$("li[resid='"+baseId+"']:iteration", tree): $("li[resid='"+baseId+"']:node", tree);

	var willStay = false;
	var candidateNode=null;
	
	while(! willStay){			
		<%-- get the previous sibling or its parent if none found --%>
		candidateNode = (loopNode.prev().length>0) ? loopNode.prev() : loopNode.parents("li").first();
		
		<%-- if the found node is a library, or do not belong to the array of deleted nodes we can stop looping --%>
		var cId = parseInt(candidateNode.attr('resid'));
		if ( candidateNode.is("[rel='drive']")  || $.inArray(cId, vIds) == -1){
			willStay = true;
		}else{
			loopNode = candidateNode;
		}
	}
	
	return candidateNode;	
}

</script>	


<pop:popup id="delete-node-dialog" titleKey="dialog.delete-tree-node.title" closeOnSuccess="false" >
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="tree.button.delete-node.label" />
	
			'${ label }': confirmDeletion,			
		<pop:cancel-button />
	</jsp:attribute>
	
	<jsp:attribute name="body">
		
		<span id="delete-node-dialog-label"></span>
		<br />				
	</jsp:attribute>
</pop:popup>