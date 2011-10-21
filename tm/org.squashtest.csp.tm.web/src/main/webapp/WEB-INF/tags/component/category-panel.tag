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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ attribute name="categories" type="java.util.Collection"  required="true" description="the categories of reports that should be displayed"%>

<s:url var="reportUrl" value="/report-workspace/report">
</s:url>


<script type="text/javascript">

	function findId(name){
		var idIndex = name.lastIndexOf("-")+1;
		
		return name.substring(idIndex);		
	}


	function cleanContextual(){
		var contextualList = $(".is-contextual");
		
		if (contextualList.length==0) return;
		
		
		/* damn widget factory */
		
		/*$(contextualList).each(function(){
			 $.Widget.prototype.destroy.apply($(this), arguments); 
		});*/			
		
		/* FIXME : find something more appropriate than just destroying dialogs */
		$('.is-contextual').each(function(){					
			$(this).dialog("destroy").remove(); 
		});
	}
	
	function loadContextualReport(reportItem){
		
		cleanContextual();
		$("#contextual-content").html('');
		
		var reportName=$(reportItem).attr("id");
		var repId=findId(reportName);
		
		$("#contextual-content").load("${reportUrl}?report="+repId);
	}

	
	//binding
	$(function(){
		var debug=$("#outer-category-frame .report-item");
		$("#outer-category-frame .report-item").click(function(){
				loadContextualReport(this);
		});
	});

</script>




<div id="outer-category-frame">
	<c:forEach var="category" items="${categories}">
		<f:message var="categName" key="${category.resourceKeyName}"/>
		<comp:toggle-panel id="${category.resourceKeyName}" title="${categName}" open="true">
				<jsp:attribute name="body">
					<div id="category-panel-${category.id}" class="category-panel">
						
						<c:forEach var="report" items="${category.reportList}">
							<div id="report-item-${report.id}" class="report-item">
								<div class="report-item-icon">
									<jq:report-type-image reportType="${report.reportType}"/>
								</div> 
								<f:message var="reportTypeName" key="${report.reportType.resourceKeyName}"/>
								<f:message var="reportName" key="${report.resourceKeyName}"/>
								<f:message var="reportDescription" key="${report.resourceKeyDescription}"/>
								<div class="report-item-description">
									<label><b>${reportName}</b><br/><b>(${reportTypeName})</b></label><br/>
									<span>${reportDescription}</span>
								</div>
								<div style="clear:both;"></div>
							</div>
						
						</c:forEach>
						
					
					</div>
				</jsp:attribute>
			
		</comp:toggle-panel>
	
	</c:forEach>


</div>