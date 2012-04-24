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
<%@ tag description="activation of jquery-ui tabs" %>
<script type="text/javascript">
<%-- The dialog creates and kills ckeditor instances each times it is used, otherwise it does not work --%>
$(function() {
	$( '.fragment-tabs' ).tabs();
	resizeTabs();
});
var contentDiv;
var parentTop;
var viewportHeight;
var computedHeight;
function resizeTabs()
{
    // Get elements and necessary element heights
     contentDiv = $('.fragment-tabs > div');
     parentTop = $(contentDiv[0]).offset().top;
     viewportHeight = document.getElementsByTagName('body')[0].clientHeight;
     var computedHeight = viewportHeight - parentTop -20;
	    for(var i=0; i<contentDiv.length; i++){    	        	  
	    	   $(contentDiv[i]).css('height', computedHeight); 
	    }
	
}
window.onresize = resizeTabs;

</script>
