/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
 * Issue 2195
 * 
 * Context : executing an execution in the popup or the OER using IE8.
 * 
 * A keydown 'enter' anywhere in the documents triggers a click event on the button "stop".
 * I don't know whether the event just bubbles to the button albeit being in a totally separate
 * subtree, or if the button just kept the focus althgouht the user is being editing something else. 
 * I don't know and I'm not surprised anymore, all that count is that we must ensure that the window wont 
 * close unexpectedly. 
 * 
 * The following function will intercept any Enter event that bubbles up to the document and cancel it. 
 * Sometimes this code run on IE8 will cancel the event before the legitimate handlers kick in (with complete 
 * disregard of which handler has precedence), that's theoretically uncool but it serves our purposes here.
 *
 * Ripped from squashtest/jquery.squash.plugin#noBackspaceNavigation().
 * 
 */
define(["jquery"], function(){
	return function(){
		$(document).bind('keydown', function(event) {
			var doPrevent = false;
			if (event.keyCode === 13) {
				var d = event.srcElement || event.target;
				if ((d.tagName.toUpperCase() === 'INPUT' && (d.type.toUpperCase() === 'TEXT' || d.type
						.toUpperCase() === 'PASSWORD')) ||
						d.tagName.toUpperCase() === 'TEXTAREA') {
					doPrevent = d.readOnly || d.disabled;
				} else {
					doPrevent = true;
				}
			}

			if (doPrevent) {
				event.preventDefault();
			}
		});		
	};
});