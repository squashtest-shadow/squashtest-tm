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
<%@ tag description="general information panel for an auditable entity. Client can add more info in the body of this tag" body-content="scriptless" %>
<%@ attribute name="auditableEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ attribute name="entityUrl" description="REST url representing the entity. If set, this component will pull itself from entityUrl/general" %>
<%@ attribute name="withoutCreationInfo" type="java.lang.Boolean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<f:message var="rawDateFormat" key="squashtm.dateformat.iso"/>

<div id="general-information-panel" class="information-panel">
	<c:if test="${ not withoutCreationInfo }">
		
		<span ><f:message key="label.CreatedOn" />&nbsp;:&nbsp;</span>
		
		<span id="created-on">
			<span class="datetime"><f:formatDate value="${ auditableEntity.createdOn }" pattern="${rawDateFormat}" timeZone="UTC"/></span> 
			<span class="author">(${ auditableEntity.createdBy })</span>
		</span>
		<br />
	</c:if>
	
	<span><f:message key="label.UpdatedOn" />&nbsp;:&nbsp;</span>
	
	(<f:message key="label.lower.Never" />)
	<span id="last-modified-on">	
		<span class="datetime"><f:formatDate value="${ auditableEntity.lastModifiedOn }" pattern="${rawDateFormat}"  timeZone="UTC"/></span> 
		<span class="author">(${ auditableEntity.lastModifiedBy })</span>
	</span>
	<br />
</div>
	<script type="text/javascript">

		$(function(){

			require(["squash.dateutils"], function(dateutils){

				var displayFormat = '<f:message key="squashtm.dateformat" />';
				var never = '<f:message key="label.lower.Never" />';
				
				function updateDateInformations(infos){					
					
					var newCreatedOn = (infos.createdOn !== null && infos.createdOn.length>0) ? dateutils.format(infos.createdOn, displayFormat) : "";
					var newCreatedBy = (infos.createdBy !== null && infos.createdBy.length>0) ? infos.createdBy : never;
					
					var newModifiedOn = (infos.modifiedOn !== null && infos.modifiedOn.length>0) ? dateutils.format(infos.modifiedOn, displayFormat) : "";
					var newModifiedBy = (infos.modifiedBy !== null && infos.modifiedBy.length>0) ? infos.modifiedBy : never;
										
					$("#created-on > .datetime").text(newCreatedOn);
					$("#created-on > .author").text(newCreatedBy);
					
					$("#last-modified-on > .datetime").text(newModifiedOn);
					$("#last-modified-on > .author").text(newModifiedBy);
				
				}
				
				var infos = {
					createdOn : $("#created-on > .datetime").text(),
					createdBy : $("#created-on > .author").text(),
					modifiedOn : $("#last-modified-on > .datetime").text(),
					modifiedBy : $("#last-modified-on > .author").text()					
				} 
				
				updateDateInformations(infos);
				
				<c:if test="${ not empty entityUrl }">
				// also autoupdate when any information is posted from this page
				$("#general-information-panel").ajaxSuccess(function(event, xrh, settings) {
					if (settings.type == 'POST') {
						$.ajax({
							type : 'GET',
							url : '${ entityUrl }/general',
							dataType : 'json'
						})
						.done(function(json){
							updateDateInformations(json);
						});
					}
				});
				</c:if>
			});
		});
	</script>