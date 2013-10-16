/**
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
package org.squashtest.tm.web.internal.jsp.tags;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;

/**
 * <p>That tag will handle the body and add additional css classes to any element that is part of a jquery tab. Example of the expected body is 
 * the following (note which classes or id are used in which situations):</p>
 *
 *
 * <p>&lt; div class="fragment-tabs" &gt;</p>
 * 
 * <p>&lt; ul class="tab-menu"&gt;</p>
 * <p>&lt; li &gt;</p>
 * <p>&lt; a href="#tab1"&gt;tab1 &lt; /a&gt;</p>
 * <p>&lt; /li &gt;</p>
 * 
 * <p>&lt; li &gt;</p>
 * <p>&lt; a href=&gt;tab2 &lt; /a &gt;</p>
 * <p>&lt; /li &gt;</p>
 * 
 * <p>&lt; a href="#tab2"/ul &gt;</p>
 *
 * 
 * <p>&lt; div id="tab1" &gt; content 1 &lt; /div &gt;</p>
 * 
 * <p>&lt; div id="tab2" &gt; content 2 &lt; /div &gt;</p>
 * 
 *<p>&lt; /div &gt;</p>
 *
 *
 *
 *<p>Note that we got here an opening &lt;div&gt; but not the closing one. We only need to </p>
 *
 * <p>@author bsiri</p>
 *
 */
public class JQueryTabsHeader extends SimpleTagSupport {

	private static final String MAIN_DIV_FETCH_CLASS = "fragment-tabs";
	private static final String MAIN_MENU_FETCH_CLASS = "tab-menu";
	
	private static final String MAIN_DIV_ADDITIONAL_CLASSES = "ui-tabs ui-widget ui-widget-content ui-corner-all";
	private static final String MAIN_MENU_ADDITIONAL_CLASSES = "ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all";
	
	private static final String MAIN_MENUITEM_ADDITIONAL_CLASSES = "ui-state-default ui-corner-top";
	private static final String MAIN_MENUITEM_ACTIVE_ADD_CLASSES = "ui-tabs-active ui-state-active";
	private static final String MAIN_MENULINK_ADDITIONAL_CLASSES = "ui-tabs-anchor";
	
	private static final String CONTENT_ADDITIONAL_CLASSES = "ui-tabs-panel ui-widget-content ui-corner-bottom";
	private static final String DISPLAY_NONE = "not-displayed";
	
	private static final String CLASS_ATTRIBUTE = "class";
	
	private static final Pattern CLASS_MATCHER = Pattern.compile("(class=\"[^\"]+)");
	
	
	private Collection<String> divContentIds = new LinkedList<String>();
	private int activeContentIndex = 0; 
	
	
	private Source source;
	private OutputDocument output;
	
	@Override
	public void doTag() throws JspException, IOException {
		
		JspFragment body = getJspBody();
		JspContext context = getJspContext();
		
		StringWriter writer = new StringWriter();
		
		body.invoke(writer);
		String strBody = writer.toString();
		
		source = new Source(strBody);
		output = new OutputDocument(source);
		
		modify();
		
		context.getOut().println(output.toString());
		
	}
	
	
	private void modify(){
		processMainDiv();
	}
	
	
	private void processMainDiv(){
		List<Element> elements = source.getAllElementsByClass(MAIN_DIV_FETCH_CLASS);
		for (Element elt : elements){
			String processed = addClasses(elt, MAIN_DIV_ADDITIONAL_CLASSES);
			output.replace(elt.getStartTag(), processed);
		}
	}
	
	private String addClasses(Element elt, String additionalClasses){
		String html = elt.getStartTag().toString();
		return CLASS_MATCHER.matcher(html).replaceFirst("$1 "+additionalClasses);		
	}
	
}
