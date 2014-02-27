/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.api.widget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.squashtest.tm.core.foundation.i18n.Labelled;
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * Implementation of {@link NavigationButton} which provides internationalized properties using the context's message
 * source. Has to be configured using Spring.
 * 
 * @author mpagnon
 * 
 */
public class InternationalizedNavigationMenuItem extends Labelled implements NavigationButton, InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(InternationalizedNavigationMenuItem.class);

	private String tooltipKey;
	private String url;
	private String imageOnUrl;
	private String imageOffUrl;

	public InternationalizedNavigationMenuItem() {
		super();
	}

	/**
	 * Tooltip is internationalized.
	 * 
	 * @see org.squashtest.tm.api.widget.NavigationButton#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return getMessage(tooltipKey);
	}

	/**
	 * @see org.squashtest.tm.api.widget.NavigationButton#getUrl()
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * @param tooltipKey
	 *            the tooltipKey to set
	 */
	public void setTooltipKey(String tooltipKey) {
		this.tooltipKey = tooltipKey;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	private void checkBeanState() {
		Assert.propertyNotBlank(url, "url property should not be blank");
		Assert.propertyNotBlank(tooltipKey, "tooltipKey property should not be null");

	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public final void afterPropertiesSet() {
		checkBeanState();

	}

	/**
	 * @see NavigationButton#getImageOnUrl()
	 */
	@Override
	public String getImageOnUrl() {
		return imageOnUrl;
	}

	/**
	 * 
	 * @param onImageUrl
	 *            : the image of the activated button to set
	 */
	public void setImageOnUrl(String onImageUrl) {
		this.imageOnUrl = onImageUrl;
	}

	/**
	 * @see NavigationButton#getImageOffUrl()
	 */
	@Override
	public String getImageOffUrl() {
		return imageOffUrl;
	}

	/**
	 * 
	 * @param offImageUrl
	 *            : the image of the inactive button to set
	 */
	public void setImageOffUrl(String offImageUrl) {
		this.imageOffUrl = offImageUrl;
	}
}
