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
package org.squashtest.tm.api.report.spring.view.jasperreports;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsCsvView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsHtmlView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsXlsView;

/**
 * Extended {@link JasperReportsMultiFormatView}. It manages the same formats as {@link JasperReportsMultiFormatView}
 * plus DOCX and ODS reports. This class is intended to reduce XML configuration in reports plugins.
 * 
 * The default mappings are:
 * <p><ul>
 * <li><code>csv</code> - <code>JasperReportsCsvView</code></li>
 * <li><code>html</code> - <code>JasperReportsHtmlView</code></li>
 * <li><code>pdf</code> - <code>JasperReportsPdfView</code></li>
 * <li><code>xls</code> - <code>JasperReportsXlsView</code></li>
 * <li><code>ods</code> - <code>JasperReportsOdsView</code></li>
 * <li><code>docx</code> - <code>JasperReportsDocxView</code></li>
 * </ul>
 * 
 * @author Gregory Fouquet
 * 
 */
public class JasperReportsExtMultiFormatView extends JasperReportsMultiFormatView {
	public JasperReportsExtMultiFormatView() {
		super();
		configureFormatMappings();
		configureContentDispositionMapping();
	}

	/**
	 * 
	 */
	private void configureContentDispositionMapping() {
		Properties mappings = new Properties();
		String resource = "org/squashtest/tm/api/report/spring/view/jasperreports/content-disposition-mappings.properties";
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		try {
			mappings.load(is);
			setContentDispositionMappings(mappings);
		} catch (IOException e) {
			throw new IllegalStateException("Expected resource not found : " + resource, e);
		}
	}

	private void configureFormatMappings() {
		Map<String, Class<? extends AbstractJasperReportsView>> formatMappings = new HashMap<String, Class<? extends AbstractJasperReportsView>>(
				6);
		formatMappings.put("csv", JasperReportsCsvView.class);
		formatMappings.put("html", JasperReportsHtmlView.class);
		formatMappings.put("pdf", JasperReportsPdfView.class);
		formatMappings.put("xls", JasperReportsXlsView.class);
		formatMappings.put("docx", JasperReportsDocxView.class);
		formatMappings.put("ods", JasperReportsOdsView.class);
		
		setFormatMappings(formatMappings);
	}
}
