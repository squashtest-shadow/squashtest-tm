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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ attribute name="associatedSelectJeditableId" required="true"%>
<%@ attribute name="isAuto" required="true"%>
<%@ attribute name="url" required="true"%>
<%@ attribute name="paramName" required="true" description="the name of the parameter being posted"%>

<%-- <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %> --%>
<%-- <%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %> --%>

<input type="checkbox" id="${associatedSelectJeditableId}-auto"
	style="vertical-align: middle; position: relative;" /><label for="${associatedSelectJeditableId}-auto" class="afterDisabled">auto</label>

<script type="text/javascript">
	$(function() {
		var sel_checkbx = $('#${associatedSelectJeditableId}-auto');
		
		sel_checkbx.prop('checked', ${isAuto});
		
		if (sel_checkbx.prop('checked')){
			sel_setAutoMode();
		}
		
		sel_checkbx.change(function(){
			var sel_isAuto = $(this).prop('checked');
			if (sel_isAuto){
				$('#${associatedSelectJeditableId}').find("[type='cancel']").trigger('click');
				sel_setAutoMode();
			}else{
				sel_setManualMode();
				$('#${associatedSelectJeditableId}').trigger('click');
			}
			sel_postState(this,sel_isAuto);
		});		
		
		function sel_setAutoMode(){
			$('#${associatedSelectJeditableId}').editable('disable');
		};
		function sel_setManualMode(){
			$('#${associatedSelectJeditableId}').editable('enable');
		};
		
		function sel_postState(checkbx, isAuto){
			$.ajax({
				type : 'POST',
				data : "${paramName}"+"="+isAuto.toString(),
				success : function(deducedValue){sel_postStateSuccess(deducedValue,  isAuto);},
				error : function(){sel_postStateFailed();},
				dataType : "text",
				url : '${url}'			
			});		
		};
		function sel_postStateSuccess(deducedValue, isAuto){
			if (isAuto){
				sel_postStateIsAutoSuccess(deducedValue);
			}
		};
		function sel_postStateIsAutoSuccess(deducedValue){
			$("#${associatedSelectJeditableId}").html(deducedValue);	
		};
		function sel_postStateFailed(){
			$.squash.openMessage("<f:message key='popup.title.error' />", "<f:message key='error.generic.label'");
			
		};

	})
	
</script>