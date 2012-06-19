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
<c:url var="delSuitesUrl" value="/${resourceName}-browser/delete-test-suites" />

<c:choose>
		<c:when test="${'requirement' == resourceName}">
			<f:message var="deleteMessage" key="dialog.label.delete-nodes.requirements.label" />
		</c:when>
		<c:when test="${'test-case' == resourceName}">
			<f:message var="deleteMessage" key="dialog.label.delete-nodes.test-cases.label" />
		</c:when>
		<c:when test="${'campaign' == resourceName}">
			<f:message var="deleteMessage" key="dialog.label.delete-nodes.campaigns.label" />
		</c:when>
		<c:otherwise>
			<f:message var="deleteMessage" key="dialog.label.delete-nodes.label" />
		</c:otherwise>
	</c:choose>


<%-- onLoad code --%>
<script type="text/javascript">



$(function(){

	var dialog=$( "#delete-node-dialog" );
	dialog.bind( "dialogopen", function(event, ui) {
		
		var jqThis = $(this);
		
		initDeleteNodeDialog(jqThis)
		.done(function(){
			sendDeletionSimulationRequest();
		})
		.fail(function(){
			<f:message var="deletionDeniedLabel" key="dialog.label.delete-node.rejected" />
			jqThis.dialog("close");
			squashtm.notification.showInfo("${deletionDeniedLabel}");			
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


/* ***************  utilities  ***************** */ 
  
 
function getNodeCategories(treeNodes){
	var allTypes = treeNodes.all('getDomType');
	return $.unique(allTypes);
}

function getUrl(strType){
	switch(strType){
		case "folder" : 
		case "file" : return "${baseUrl}"; break;
		case "resource" : return "${delIterationsUrl}"; break;
		case "view" : return "${delSuitesUrl}"; break;
		
	}
}

function getDeleteIds(strType, nodes){
	return nodes.filter("[rel='"+strType+"']").treeNode().all('getResId');
}


function buildMessage(newText){
	return $('<div>', {'text' : newText} );
}


/* ***************  /utilities  ***************** */ 




//returns a deferred
function initDeleteNodeDialog(jqDialog){
	
	var deferred = $.Deferred();
	
	<%-- erase previously stored data --%>
	jqDialog.data("vNodes", null);			
	
	<%-- store the selected nodes. --%> 
	
	var tree = $('${treeSelector}');
	var vNodes = tree.jstree("get_selected");
	jqDialog.data("vNodes", vNodes);
	
	var operations = tree.jstree("allowedOperations");
	
	if (operations.match("delete")){		
		deferred.resolve();		
	}else{
		deferred.reject();
	}

	return deferred.promise();
}

/**
 * will treat differently regular files from iterations and test-suites.
 */
function sendDeletionSimulationRequest(){

	var jqDialog = $('#delete-node-dialog');
	var message = $('<div/>');
	
	var vNodes = jqDialog.data("vNodes");
	var types = getNodeCategories(vNodes);
	
	for (var i in types){
		var domtype = types[i];
		if (domtype!=="drive"){
			
			var url = getUrl(types[i])+"/simulate";
			var nodeIds = getDeleteIds(domtype, vNodes);
			
			$.post(url, { 'nodeIds[]' : nodeIds})				
			.success(function(data){
				var newMessage = buildMessage(data); 
				message.append(newMessage);
			})
			.fail(function(){
				jqDialog.dialog("close"); <%-- the standard failure handler should kick in, no need for further treatment here. --%>
				return ;
			});		
		}
	}	
	
	message.append("<span><strong>${deleteMessage}</strong></span>");
		
	jqDialog.html(message.html());
	
	
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



function postConfirm(domtype, vNodes){

	var nodeIds = getDeleteIds(domtype, vNodes);
	var url = getUrl(domtype)+"/confirm?"+buildParamString(nodeIds);
	
	$.ajax({
		url : url,
		dataType : 'json',
		type : 'DELETE'
	})
	.success(function(list){		
		removeNodes(list, domtype);
		<%-- not functional for now. If 3 requests are sent, the callback would be invoked 3 times !
		<c:if test="${not empty successCallback}">
		${successCallback}();
		</c:if>
		--%>
	});		
}

function confirmDeletion(){
	
	var jqDialog = $('#delete-node-dialog');
	
	var vNodes = jqDialog.data("vNodes");
	

	var newSelected = findPrevNode(vNodes);
	vNodes.all('deselect');
	newSelected.select();		
	

	jqDialog.dialog("close");
	
	var types = getNodeCategories(vNodes);	
	
	for (var i in types){
		var domtype = types[i];
		if (domtype!=="drive"){
			postConfirm(domtype, vNodes);
		}
	}

}
	
</script>

		
<%-- node deletion success --%>

<script type="text/javascript">

function removeNodes(vIds, domtype){
	var i=0;		
	var tree =  $('${treeSelector}');
	
	for (i=0;i<vIds.length;i++){
		var id = vIds[i];
		var node = $("li[resid='"+id+"'][rel='"+domtype+"']", tree);  
		tree.jstree("delete_node", node);
	}
}

function findPrevNode(vNodes){
	if (vNodes.length==0) return vNodes;

	var loopNode = vNodes.first().treeNode();
	var ids = vNodes.all('getResId');

	var willStay = false;
	var candidateNode=null;
	
	while(! willStay){			
		<%-- get the previous sibling or its parent if none found --%>
		candidateNode = loopNode.getPrevious();
		var id = candidateNode.getResId();
		
		<%-- if the found node is a library, or do not belong to the array of deleted nodes we can stop looping --%>
		if ( candidateNode.is(":library")  || $.inArray(id, ids) == -1){
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