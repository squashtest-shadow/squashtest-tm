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
<%@ tag description="Table which fetches its data through Ajax, no pagination, no filter. SHOULD BE PUT BEFORE THE DECORATED TABLE"  %>
<%@ attribute name="tableId" required="true" %>
<%@ attribute name="paginate" %>
<%@ attribute name="displayLength" type="java.lang.Long" description="The nb of rows shown by the table. Default is 50" %>
<%@ attribute name="url" required="true" description="Url used to populate the table" %>
<%@ attribute name="rowCallback"  fragment="true" description="Javascript function name or inlined function to be called when a row is created. Default is no function. Definition of row buttons should go there." %>
<%@ attribute name="drawCallback"  fragment="true" description="Javascript function name or inlined function to be called when table is redrawn. DnD activation should go there" %>
<%@ attribute name="columnDefs"  fragment="true" description="Columns definition, as inlined list of JSON" %>
<%@ attribute name="dom" fragment="true" description="Optional cryptic string which defines table header and footer." %>
<%@ attribute name="initialSort" fragment="true" required="false" description="If set, the data will be sorted at first call"%>
<%@ attribute name="disableHighlightOnMouseOver" fragment="true" required="false" description="If set, the rows will NOT be highlighted on mouse over"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${ empty displayLength }"><c:set var="displayLength" value="50" /></c:if>

<script type="text/javascript">
	$(function() {
		var dataTable = $( "#${ tableId }" ).dataTable({
			"oLanguage":{
				"sLengthMenu": '<f:message key="generics.datatable.lengthMenu" />',
				"sZeroRecords": '<f:message key="generics.datatable.zeroRecords" />',
				"sInfo": '<f:message key="generics.datatable.info" />',
				"sInfoEmpty": '<f:message key="generics.datatable.infoEmpty" />',
				"sInfoFiltered": '<f:message key="generics.datatable.infoFiltered" />',
				"oPaginate":{
					"sFirst":    '<f:message key="generics.datatable.paginate.first" />',
					"sPrevious": '<f:message key="generics.datatable.paginate.previous" />',
					"sNext":     '<f:message key="generics.datatable.paginate.next" />',
					"sLast":     '<f:message key="generics.datatable.paginate.last" />'
				}
			},
			"bJQueryUI": true,
			"bAutoWidth": false,
			"bFilter": false,
			"bPaginate": ${ not empty paginate and paginate },
			"sPaginationType": "squash",
			"iDisplayLength": ${ displayLength },
			"bProcessing": true,
			"bServerSide": true,
			"sAjaxSource": '${ url }', 
			/* 
			 * For some reasons when the DOM is modified around the table 
			 * (ie because of jQuery.wrap()) the datatable constructor is called again.
			 * 
			 * To prevent annoying warning messages we must set the bRetrieve flag to true. That flag says that
			 * if an instance of dataTable for this DOM table exists, it will be returned.
			 *
			 * This fix seems to work for the implementation of DataTables v 1.7.3 and jQuery 1.5.2.
			 */
			"bRetrieve" : true,				
<c:if test="${ not empty initialSort}">"aaSorting": <jsp:invoke fragment="initialSort"/>,  </c:if>
<c:if test="${ not empty rowCallback }">"fnRowCallback": <jsp:invoke fragment="rowCallback"/>,</c:if>
<c:if test="${ not empty drawCallback }">"fnDrawCallback" : <jsp:invoke fragment="drawCallback"/>,</c:if>				

			"sDom" : 't<"dataTables_footer"lirp>',
			"aoColumnDefs": [
				<jsp:invoke fragment="columnDefs" />
			] 
		})
		.addClass("is-contextual");	
		<c:if test="${ empty disableHighlightOnMouseOver }">
			bindHover(dataTable);
		</c:if>
	});
	
	
</script>