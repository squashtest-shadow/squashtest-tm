<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
	Winner of the Golden WTF Award here ! Whenever we have time for refractoring DUMP THIS along with 
		- search-result-display-by-requirement
		- search-result-display-ordered-by-requirement
		- search-result-display-ordered
		
	13/10/04 : that page is still there and I still hate it
	
	14/01/24 : pruned lot of dead code and waiting for the day I will dump the remainder		
 --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<table id="search-result-datatable" data-def="pre-sort=1, hover"  class="unstyled-table">

    <thead class="not-displayed">
         <tr>
            <th data-def="invisible, target=0">col1</th>
            <th data-def="target=1">col2</th>
         </tr>
    </thead>

  <tbody>
    <c:forEach var="object" items="${resultList}">

      <c:set var="iconClass" value="icon-folder" />
      <c:choose>
        <c:when test="${object.class.simpleName == 'Campaign'}">
          <c:set var="iconClass" value="icon-campaign" />
        </c:when>
        <c:when test="${object.class.simpleName == 'Iteration'}">
          <c:set var="iconClass" value="icon-iteration" />
        </c:when>
      </c:choose>

      <tr class="search-items">

        <td class="objectId">${object.id}</td>
        <td id="searchnode-${object.class.simpleName}-${object.id}" class="non-tree" style="border: none;">
          <span class="icon-entity ${iconClass}"></span>
          <span class="search-text">${object.name}</span>
        </td>

      </tr>
    </c:forEach>
  </tbody>
</table>

<script type="text/javascript">
  require(["common"], function() {
    require(["jquery", "squashtable"], function($){
      /*
      $("#search-result-datatable").squashTable({
        'sDom' : '<r>t<<l><ip>>',
        'sPaginationType' : 'full_numbers'
      },{});
      
    })
  });*/
  $(function() {
		var dataTable = $( "#search-result-datatable" ).dataTable({
			"oLanguage":{
				"sLengthMenu": '<f:message key="generics.datatable.lengthMenu" />',
				"sZeroRecords": '<f:message key="generics.datatable.zeroRecords" />',
				"sInfo": 'Affichage _START_ à _END_ sur _TOTAL_ élément(s)',
				"sInfoEmpty": 'Affichage 0 à 0 sur 0 élément(s)',
				"sInfoFiltered": '(filtré de _MAX_ éléments au total)',
				"oPaginate":{
					"sFirst":    'Premier',
					"sPrevious": 'Précédent',
					"sNext":     'Suivant',
					"sLast":     'Dernier'
				}
			},
// 			"sScrollY": "350px",
			"bJQueryUI": true,
			"bAutoWidth": false,
			"bFilter": false,
			"bPaginate": false,
			"sPaginationType": "full_numbers",

"aaSorting": [[1,'asc']],  

			"sDom" : '<r>t<<l><ip>>',
			"aoColumnDefs": [
				
{'bVisible': false, 'bSortable': false, 'aTargets': [0]},

{'bVisible': true, 'bSortable': false, 'aTargets': [1]}

			] 
		});
		
		//bindHover(dataTable);
	});
	});
});	
	
</script>