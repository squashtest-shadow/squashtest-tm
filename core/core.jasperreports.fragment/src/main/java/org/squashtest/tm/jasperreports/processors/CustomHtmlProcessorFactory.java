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
package org.squashtest.tm.jasperreports.processors;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Enumeration;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;

import net.sf.jasperreports.engine.util.JEditorPaneHtmlMarkupProcessor;
import net.sf.jasperreports.engine.util.MarkupProcessor;
import net.sf.jasperreports.engine.util.MarkupProcessorFactory;


/**
 * As a solution to issue https://ci.squashtest.org/mantis/view.php?id=2293 this implementation 
 * will handle &lt;strong&gt; and &lt;em&gt; instead of their obsolete versions. This implementation must 
 * be supplied in Jasper Report configuration, like 
 * net.sf.jasperreports.markup.processor.factory.html=org.squashtest.tm.web.internal.controller.report.CustomHtmlProcessorFactory
 * 
 * @author bsiri
 *
 */
public class CustomHtmlProcessorFactory extends JEditorPaneHtmlMarkupProcessor implements MarkupProcessorFactory {

	private static CustomHtmlProcessorFactory custom_instance;
	
	

	@Override
	public MarkupProcessor createMarkupProcessor(){
		if (custom_instance == null)		{
			custom_instance = new CustomHtmlProcessorFactory();
		}
		return custom_instance;
	}
	
	
	//slightly scrapped from JEditorPanelHtmlMarkupProcessor
	protected Map<Attribute,Object> getAttributes(AttributeSet attrSet){
		
		Map<Attribute, Object> attributes = super.getAttributes(attrSet);
		
		//checks for attributes WEIGHT and POSTURE. If they were not set, checks whether some HTML.Tag named "strong" of "em" exists in the 
		//attribute set.
		
		if (! attributes.containsKey(TextAttribute.WEIGHT) &&
		   hasHtmlTag(attrSet, HTML.Tag.STRONG)){
			attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		}
	
		if (! attributes.containsKey(TextAttribute.POSTURE) &&
				hasHtmlTag(attrSet, HTML.Tag.EM)){
			attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}
		
		return attributes;
	}
	
	public boolean hasHtmlTag(AttributeSet attrSet, HTML.Tag tag){
		Enumeration<?> attrNames = attrSet.getAttributeNames();  
		while (attrNames.hasMoreElements()){
			Object obj = attrNames.nextElement();
			if (tag.equals(obj)){
				return true;
			}
		}
		return false;
	}
	
}
