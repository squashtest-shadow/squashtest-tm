/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This helper will render pie charts that should display either 0% or 100% while the current browser is IE8. 
 * Indeed such charts just won't display. However the expected result is relatively simple to render by other 
 * means with a simple div, the proper background-color and a mask to make the div look round. 
 *
 * A proper jqplot plugin like jQuery.jqplot.IE8PieRenderer would have been better though.
 * 
 *
 */


define(['jquery'], function($){
	
	function getCenter(view){
		return [ 
		   view.width()/2,
		   145				// instead of (300 /2 = 150). The center is a bit off.
		];
	}

	function createImg(conf, view){
	
		var cst_minmargin = 33;
		
		var center = getCenter(view),
			x_center = center[0],
			y_center = center[1];		
		
		var radius = Math.min(x_center - cst_minmargin, y_center - cst_minmargin);
		
		var diameter = radius * 2,
			marginleft = x_center - radius,
			margintop = y_center - radius;
		
		var img =  $('<img/>', {
			'src' : squashtm.app.contextRoot+'/images/dashboard-mask.png'
		});
		
		var css = {
			'background-color' : conf.seriesColors[0],
			'height' : diameter,
			'width' : diameter,
			'margin-top' : margintop,
			'margin-left' : marginleft
		};
		
		img.css(css);
		
		view.append(img);
	}
	
	// partly ripped from the jqplot PieRenderer
	function createCaption(conf, view){
		try{
			var center = getCenter(view),
			x_center = center[0],
			y_center = center[1];	
			
			var label = conf.seriesDefaults.rendererOptions.dataLabels;
			var elem = $('<div class="jqplot-pie-series jqplot-data-label" style="position:absolute;">' + label + '</div>');
				
			view.append(elem);
			
			// small css fix
			var half_w_elem = elem.width()/2,
				half_h_elem = elem.height()/2;
			
			elem.css({top : y_center - half_h_elem, left : x_center - half_w_elem});
			
		}
		catch(exception){
			if (window.console && window.console.log){
				console.log("ie8-special-pie-renderer : unable to display caption because of exception : " + exception);
			}
		}
	}
	
	return {

		/*
		 * conf : a valid configuration object for jqplot pie. See pie-view.getConf().
		 * view : the div where we want to 'plot' our data as a jQuery object.
		 * 
		 * returns : void
		 */
		render : function(conf, view){
			view.empty();
			createImg(conf, view);
			createCaption(conf, view);
		} 
		
		
	};
	
});