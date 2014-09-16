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
define(['jquery'], function($) {
	
	/*it is written that way so that only the first '=' will match 
	as a separator, allowing '=' to be a valid part of the value as well.
	It could have been written as 'atom.split(/\s*=(.+)?/,2);' but IE8 definitely 
	didn't want it. */
	function _parseAssignation(atom) {
		
		var name, value;
		
		var posequals = atom.indexOf('=');	//index of the first '='
		
		//'unary' assignation : set 'true' as value
		if (posequals === -1){
			name = $.trim(atom);
			value = true;
		}
		//'binary' assignation
		else{
			var members = [ atom.substr(0, posequals), atom.substr(posequals+1)];
			name = $.trim(members[0]);
			value = $.trim(members[1]);
		}
		
		return {
			name : name,
			value : value
		};
	}

	function _parseSequence(seq) {
		var result = {};
		var statements = seq.split(/\s*,\s*/);
		var i = 0, length = statements.length;

		for (i = 0; i < length; i++) {
			var stmt = statements[i];
			var parser = (stmt.indexOf(',') !== -1) ? _parseSequence : _parseAssignation;
			var parsed = parser(stmt);
			result[parsed.name] = parsed.value;
		}

		return result;
	}
	
	
	return {
		parse : _parseSequence
	};
	
});