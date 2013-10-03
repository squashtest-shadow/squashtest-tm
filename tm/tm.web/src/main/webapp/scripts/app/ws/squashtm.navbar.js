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
var squashtm = squashtm || {};

define([ "jquery" ], function($) {
	/*var highlightedButton = "";

	function navLinkOn(linkName) {
		var thisLink = $('#' + linkName);
		var imgName = $(thisLink).find('img').attr("src");
		if (imgName) {
			imgName = imgName.replace("_off", "_on");
			$(thisLink).find('img').attr("src", imgName);
		}
	}

	function navLinkOff(linkName) {
		var thisLink = $('#' + linkName);
		var imgName = $(thisLink).find('img').attr("src");
		if (imgName) {
			imgName = imgName.replace("_on", "_off");
			$(thisLink).find('img').attr("src", imgName);
		}
	}

	function initHighlighted(linkName) {

		// ** linkName is true if defined and not empty string 
		if (linkName) {
			highlightedButton = linkName + '-link';
			navLinkOn(highlightedButton);
		}
	}*/

	squashtm.navbar = {
	/*	initHighlighted : initHighlighted,
		highlightOn : function(linkName) {
			if (linkName !== highlightedButton) {
				navLinkOn(linkName);
			}
		},
		highlightOff : function(linkName) {
			if (linkName !== highlightedButton) {
				navLinkOff(linkName);
			}
		}*/
	};

	/*$(function() {
		$(".nav_btn").hover(function() {
			var linkName = $(this).attr("id");
			squashtm.navbar.highlightOn(linkName);
		}, function() {
			var linkName = $(this).attr("id");
			squashtm.navbar.highlightOff(linkName);
		});
	});*/

	return squashtm.navbar;
});