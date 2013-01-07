/**
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
package org.squashtest.tm.web.internal.util;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.Tag;

import org.springframework.web.util.HtmlUtils;

public final class HTMLCleanupUtils {
	
	private HTMLCleanupUtils(){
		
	}

	public static String htmlToText(String html){
		
		String replacedDescription = html.replaceFirst("\n", "");
		Source htmlSource = new Source(replacedDescription);
		Segment htmlSegment = new Segment(htmlSource, 0, replacedDescription.length());
		Renderer htmlRend = new Renderer(htmlSegment);
		String encoded = htmlRend.toString();
		return encoded.trim();
	
	}
	
	/* note : Unescape is idempotent when applied on unescaped data. We use that trick to prevent double html encoding*/ 
	public static String forceHtmlEscape(String html){
		String unescaped = HtmlUtils.htmlUnescape(html);	
		return HtmlUtils.htmlEscape(unescaped);
	}
	
	/* naive implementation, needs numerous improvements */
	public static String stripJavascript(String html){
		
		Source source = new Source(html);
		OutputDocument output = new OutputDocument(source);
		
		for (Tag tag : source.getAllStartTags()){
			if (tag.getName().equals(HTMLElementName.SCRIPT)){
				output.remove(tag.getElement());
			}
		}
		
		return output.toString();
	}

}
