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
<%@ tag body-content="empty" %>

<script type="text/javascript">
	/*$(function(){
		var togglePanels = $('.toggle-panel');
		
		if (togglePanels.length>0){
			var i=0;
			for (i=0;i<togglePanels.length;i++){
				decorateTogglePanel($(togglePanels[i]));
			}
		}
		
	});
	
function(){
		//look
		
		this.addClass( "ui-accordion ui-widget ui-helper-reset ui-accordion-icons" );
		var panelHead = this.find('h3');
		panelHead.addClass( "ui-accordion-header ui-helper-reset ui-state-default ui-state-hover ui-state-active ui-corner-top" );

		var collapsedPanelHead = panelHead.not( ".open" );
		collapsedPanelHead.toggleClass( "ui-state-active ui-corner-top ui-corner-all" );
		
		panelHead.next().addClass( "ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active" );
		collapsedPanelHead.next().hide();
		
		// behaviour
		var toggleControl = function (){
			var isActive = this.hasClass("ui-state-active");
			
			//the following line is for debug only. Remove that when done.
			var inputs=this.find(":button, .button");			
			
			if ( isActive == true ){
				this.find(":button, .button").button("option","disabled",true);
			}
			else{
				this.find(":button, .button").button("option","disabled",false);				
			}
			
		};
		
		panelHead.click(function(event) {
			//prevent the stuck bug, bug #9
			event.stopImmediatePropagation();
			var isTriggered = $(this).next().hasClass("ui-effects-wrapper");
			if (isTriggered == true ) return;
			
			this.next().toggle( 'blind', 500 );
			
			toggleControl.call(this);
			
			this.toggleClass( "ui-state-active ui-corner-top ui-corner-all" );
		});		
		
		this.toggle = function(){
			panelHead.click();
		}
		
		return this;
		
	}	
	
	*/
</script>