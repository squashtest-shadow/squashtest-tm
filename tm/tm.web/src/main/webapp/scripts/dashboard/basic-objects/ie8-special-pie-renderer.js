/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This helper will render pie charts that should display either 0% or 100% while the current browser is IE8. 
 * Indeed such charts just won't display. However the expected result is relatively simple to render by other 
 * means with a simple div, the proper background-color and a mask to make the div look round. 
 *
 * A proper jqplot plugin like jQuery.jqplot.IE8PieRenderer would have been better though.
 *
 */


define(['jquery'], function($){

	function createImg(conf, view){
	
		var cst_minmargin = 33;
		
		var x_center = view.width() / 2,
			y_center = 145;		// instead of (300 /2 = 150). The center is a bit off.
		
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
		
		return img;
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
			var img = createImg(conf, view);
			view.append(img);
		} 
		
		
	}
	
});