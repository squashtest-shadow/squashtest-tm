/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
 * 
 *	@author bsiri
 * 
 * 
 */

/*
 * needed if you want to (inaccurately) support web browsers which can't word-break 
 * 
 * @params : 
 *  maxWidth : the width the text should not exceed
 *  domElement : the element containing the text, as a DOM entity
 *  
 * @returns : the replacement text of for the given element, that should be "appended" to its content (do not use "set text") 
 */

//@deprecated
function brutalHyphenation(maxWidth, domElement) {

	var jqelt = $(domElement);
	var eltWidth = jqelt.width();
	var eltText = jqelt.text();
	var eltTextLength = eltText.length;

	var newContent = eltText;

	// first step : approximate how many characters it takes to reach the
	// maximal width.
	// it will allow us to use the character as a width unit.
	// note : we shorten the maxWidth just to be sure.

	var maxLength = (eltTextLength * maxWidth) / eltWidth;

	// should be true if the browser doesn't support word-break, i.e. IE8
	if (eltText.length > maxLength) {

		var tempText = eltText;
		var builder = "";
		var counter = 0;
		while (tempText.length > maxLength) {
			builder += tempText.substring(0, maxLength) + "<br//>";
			tempText = tempText.substring(maxLength);
			counter++;
		}

		newContent = builder + tempText; // we're adding the rest to it
	}

	return newContent;

}

/*
 * truncate a text to the given text length and add an ellipsis if needed.
 * 
 */
// @deprecated
function simpleHyphenation(maxLength, text) {

	var newContent = text;

	if (text.length > maxLength) {
		newContent = text.substring(0, (maxLength - 3)) + "...";
	}

	return newContent;
}

function openAttachmentIfNotEmpty() {
	var imgs = $("#attachment-container img");

	if (imgs.length != 0) {
		$("#attachment-panel").togglePanel("openContent");
	}
}

function hyphenateAttachement() {
	var attachmentsCaption = $(".div-attachments-item  a");
	if (attachmentsCaption.length != 0) {
		var i = 0;
		var itemWidth = $(".div-attachments-item:first").width();
		for (i = 0; i < attachmentsCaption.length; i++) {
			var elt = attachmentsCaption[i];
			var content = brutalHyphenation(itemWidth, elt);
			$(elt).empty();
			$(elt).append(content);

		}
	}
}

function handleNotFoundImages(defaultImageUrl) {
	var imgs = $("#attachment-container img");

	if (imgs.length == 0)
		return;

	// reloading the image to re-get the error event if needed. If you find it
	// ugly, well, you're right !
	$(imgs).each(function() {
		$(this).load().error(function() {
			this.src = defaultImageUrl;
		});
	});

}
