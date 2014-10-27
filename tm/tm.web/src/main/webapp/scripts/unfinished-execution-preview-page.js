/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
 * That page doesn't have the whole init code for templates/execute-execution-preview.html.
 * Some code was left behind, do that when time 
 */

define([ "jquery", "module", "jquery.squash.squashbutton", 
		"jquery.squash" ], 
		function($, module) {
		
			var conf = module.config();
		
			var clickHandlers = {
				stop : function() {
					if (conf.optimized) {
						parent.squashtm.ieomanager.closeWindow();
					} else {
						window.close();
					}
				},
		
				begin : function(event) {
					if (conf.optimized) {
						event.preventDefault();
						parent.squashtm.ieomanager.navigateNext();
						return false;
					} else {
						// nothing special
					}
				},
		
				links : function(event) {
					if (conf.optimized) {
						event.preventDefault();
						var url = $(this).attr('href');
						parent.squashtm.ieomanager.fillRightPane(url);
						return false;
					} else {
						// nothing special
					}
				}
			};
		
			var stopButton = $("#execute-stop-button");
			var stopIcon = stopButton.data('icon');
			stopButton.removeAttr('data-icon');
			stopButton.squashButton({
				'text' : false,
				'icons' : {
					'primary' : stopIcon
				}
			}).click(clickHandlers.stop);
		
			var beginButton = $("#execute-begin-button");
			var beginIcon = beginButton.data('icon');
			beginButton.removeAttr('data-icon');
			beginButton.squashButton({
				'icons' : {
					'secondary' : beginIcon
				}
			}).click(clickHandlers.begin);
		
		
			// when OER
			$(".load-links-right-frame a").click(clickHandlers.links);
		
			// issue #2069
			$.noBackspaceNavigation();
		


});
