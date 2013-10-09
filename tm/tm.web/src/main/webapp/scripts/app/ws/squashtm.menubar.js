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
/**
 * this unit contains the various code directly related to the tag <component:_menu-bar>
 * 
 * requires jquery to be loaded prior use.
 * 
 * 
 * @author bsiri
 * @author Gregory Fouquet
 */

/**
 * The parameters of that function will be passed as they are to more specific
 * init functions, see code below. Dont forget to add your own parameters in
 * that list, please do not overload the existing one.
 * 
 */
var squashtm = squashtm || {};

define([ "jquery" ], function($) {


	function initMainMenuBar(objFilter) {
		//nothing left to do here, but that might change later
	}

	squashtm.menubar = {
		init : initMainMenuBar
	};

	return squashtm.menubar;
});
