/**
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
package org.squashtest.tm.web.thymeleaf.processor.attr;

import java.util.List;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;

/**
 * @author mpagnon
 * 
 */
/**
 * <p>This class was useful for stuffing the DOM with css classes back when we hadn't less yet.
 * Now the preferred way to declare toggle panels is :</p>
 * 
 * <p>{@code <div class="sq-tg (expand|collapse)">} </p>
 * <p>{@code <div class="tg-head">}</p>
 * <p>{@code <h3>your title</h3>}</p>
 * <p>{@code <div class="tg-toolbar">}</p>
 * <p>{@code <input class="sq-btn" value="your input"/>}</p>
 * <p>{@code </div>}</p>
 * <p>{@code </div>}</p>
 * <p>{@code <div class="tg-body">}</p>
 * <p>{@code <span>your content</span>}</p>
 * <p>{@code </div>}</p>
 * <p>{@code </div>}</p>
 * 
 *
 *<p> To my knowledge, as of 1.10.0 this processor isn't used anywhere in the core but it still might be used by plugins, that's why
 *it had been deprecated instead of being outright ditched.</p>
 * 
 * @author bsiri
 *
 */
@Deprecated
public class SquashTogglePanelAttrProcessor extends AbstractChildrenModifierAttrProcessor {

	/**
	 * Creates a processor for the <code>tooglePanel</code> attribute
	 */
	public SquashTogglePanelAttrProcessor() {
		super("togglePanel");
	}

	@Override
	protected List<Node> getModifiedChildren(Arguments arguments, Element element, String attributeName) {
		final String attributeValue = element.getAttributeValue(attributeName);
		boolean opened = attributeValue.equals("opened");
		// wrapper
		Element wrapper = element.getFirstElementChild();
		setClassAttribute(wrapper, "toggle-panel ui-accordion ui-widget ui-helper-reset ui-accordion-icons");
		// header
		String headerClass = "ui-accordion-header ui-helper-reset ui-state-default ";
		if (opened) {
			headerClass += "tg-open ui-state-focus ui-corner-top";
		} else {
			headerClass += "ui-state-active ui-corner-all";
		}
		Element header = wrapper.getFirstElementChild();
		setClassAttribute(header, headerClass);
		// header content
		Element headerContent = header.getFirstElementChild();
		headerContent.setAttribute("style", "overflow:hidden");
		List<Element> headerContentChildren = headerContent.getElementChildren();
		// title
		Element title = headerContentChildren.get(0);
		setClassAttribute(title, "snap-left");
		// buttons
		if (headerContentChildren.size() > 1) {
			Element buttons = headerContentChildren.get(1);
			setClassAttribute(buttons, "snap-right");
		}
		// main panel
		Element mainPanel = wrapper.getElementChildren().get(1);
		setClassAttribute(mainPanel,
				"toggle-panel-main ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active");
		mainPanel.setAttribute("data-init-open", "" + opened);
		mainPanel.setAttribute("data-prerendered", "true");
		if (!opened) {
			mainPanel.setAttribute("style", "display:none");
		}
		return element.getChildren();
	}

	/**
	 * Will add the value parameter to the class of the given element.<br/>
	 * <ul>
	 * <li>If a th:class attribute is found, will append "+ ${' value'}" to it,</li>
	 * <li>else, if a class attribute is found , will append " value" to it,</li>
	 * <li>else will set the class attribute of the element with the given value.</li>
	 * </ul>
	 * 
	 * @param element
	 *            : the {@link Element} to set the class of
	 * @param value
	 *            : the classes names to add to the element
	 */
	private void setClassAttribute(Element element, String value) {
		String finalValue;
		String attributeName = "th:class";
		String thClassValue = element.getAttributeValue(attributeName);
		if (thClassValue != null) {
			finalValue = thClassValue + "+ ${' " + value + "'}";
		} else {
			attributeName = "class";
			String classValue = element.getAttributeValue(attributeName);
			if (classValue != null) {
				finalValue = classValue + " " + value;
			} else {
				finalValue = value;
			}
		}
		element.setAttribute(attributeName, finalValue);

	}

	@Override
	public int getPrecedence() {
		// A value of 10000 is higher than any attribute in the
		// SpringStandard dialect. So this attribute will execute
		// after all other attributes from that dialect, if in the
		// same tag.
		return 10000;
	}

	@Override
	protected boolean getReplaceHostElement(Arguments arguments, Element element, String attributeName) {
		return true;
	}

}
