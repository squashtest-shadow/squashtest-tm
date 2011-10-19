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
<%@ tag description="A dialog popup. The body of this tag contains the HTML body of the dialog. 
	Buttons and their bound actions are defined through a fragment passed as the 'buttons' attribute" %>
<%@ attribute name="openedBy" required="true" description="id of the button which opens the dialog" %>
<%@ attribute name="id" required="true" description="id of the popup" %>
<%@ attribute name="titleKey" %>
<%@ attribute name="width" %>
<%@ attribute name="isContextual" description="if set, this popup will be added a class to show it belongs to the contextual panel"%>
<%@ attribute name="buttons" fragment="true" required="true" %>
<%@ attribute name="closeOnSuccess" description="Closes the popup on ajax request success. Default is true." %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>




<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<script type="text/javascript">
<%-- The dialog creates and kills ckeditor instances each times it is used, otherwise it does not work --%>
$(function() {
	$( '#${ id }' ).dialog({
		autoOpen: false,
		resizable: false,
		modal: true,
		width: <c:choose><c:when test="${not empty width}">${width}</c:when><c:otherwise>600</c:otherwise></c:choose>,
		title: "<f:message key='${ titleKey }' />",
		position: ['center', 100],
		open: function() {
			$('#${ id } textarea').each(function(){
				$(this).ckeditor( function() { 
					if (CKEDITOR.instances["add-test-step-action"] != null){
						CKEDITOR.instances["add-test-step-action"].setData("  ");
					}
					if (CKEDITOR.instances["add-test-step-result"] != null){
						CKEDITOR.instances["add-test-step-result"].setData("  ");
					}
				}, { customConfig : '${ ckeConfigUrl }', language: '<f:message key="rich-edit.language.value" />' } )
				<c:if test="${not empty isContextual}">.addClass("is-contextual");</c:if>
				
			});

		},
		buttons: {
			<jsp:invoke fragment="buttons" />
		},
		close: function() {
			$('#${ id } .error-message').text('');
			$('#${ id } input:text').each(function() { $(this).val(''); });
			if ('${ openedBy }' == 'add-test-step-button'){
				CKEDITOR.instances["add-test-step-action"].destroy();
				CKEDITOR.instances["add-test-step-result"].destroy();
				return;
			}
			$('#${ id } textarea').each(function() {
				$(this).val(''); 
				try{
					$(this).ckeditorGet().destroy();
				}catch(damnyouie){
					var areaName=$(this).attr('id');
					CKEDITOR.remove(areaName); //destroying the instance will make it crash. So we remove it and hope the memory leak
												//wont be too high.
				}
			});
		}
	}).ajaxSuccess(function(event,xhr,settings) {
		<c:if test="${ (empty closeOnSuccess) or closeOnSuccess }">
			//this handler is not specific, find something better later
			//note about "isOpen" : its behavior is counterintruitive. Check http://www.sikosoft.com/item/having_trouble_with_jquerys_dialogisopen
			if ($(this).dialog("isOpen")==true) $( this ).dialog('close');
		</c:if>
	}).keypress(function(event) {
		  if (event.which == '13') {
			    var buttons=$( '#${ id }').dialog("option", "buttons" );
			    var firstOne;
			    for (var property in buttons){
			    	firstOne=buttons[property];
			    	break;
			    }
			    $(firstOne).click();
		   }
		}
	);

	<c:choose>
	<c:otherwise>
	$( "#${ openedBy }" ).bind(
		'click',
		function(){
			$( "#${id}" ).dialog( "open" );
			return false;
		}
	);
	</c:otherwise>
	</c:choose>
	
});
</script>


<c:if test="${not empty isContextual}">
<script type="text/javascript">
	$(function(){
		$('#${id}').addClass('is-contextual');
	});
</script>
</c:if>






<div id="${ id }" class="popup-dialog">
	<jsp:doBody />
</div>