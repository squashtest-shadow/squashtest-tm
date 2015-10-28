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

// ******** utilities ***********

function getVisibles(){
	return $('.attachment-item').not('.not-displayed').find('input[type="file"]');
}

function showNext(){
	$('.not-displayed').first().removeClass('not-displayed');
}

function allVisiblesAreSet(){
	var allV = getVisibles();
	return allV.filter(function(){return this.value !== '';}).length === allV.length;
}


// ******** submit hook *********

function onBeforeSubmit(){
	$("form").find('div.not-displayed').remove();
	return true;
}


// ******** event binding ********

//show next one if the fileinput changed was the last visible one
$(document).on('change', 'input[type="file"]', function(){
	if (allVisiblesAreSet()){
		showNext();
	}
});

// hide the fileinput next to that 'remove' button, only if there 
// still will be other fileinput available
$(document).on('click', '.sq-btn', function(){
	var prevFile = $(this).prev();
	
	if ( (prevFile.val() !== '')  &&  (getVisibles().length > 1)){
		$(this).parent().addClass('not-displayed');
	}
});
	




















