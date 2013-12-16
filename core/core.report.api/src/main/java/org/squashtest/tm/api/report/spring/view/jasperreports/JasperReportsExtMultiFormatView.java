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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperPrint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>
 * <ul>
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

	private static final Logger LOGGER = LoggerFactory.getLogger(JasperReportsExtMultiFormatView.class);
	private String reportFileName;

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

	@Override
	protected void renderReport(JasperPrint populatedReport, Map<String, Object> model, HttpServletResponse response)
			throws Exception {
		if (reportFileName != null) {
			Pattern pattern = Pattern.compile("(\\Q${\\E([A-Za-z:]+)\\Q}\\E)");
			Matcher matcher1 = pattern.matcher(reportFileName);
			Properties contentDispositionMappings = getContentDispositionMappings();
			Properties safeCopy = (Properties) contentDispositionMappings.clone();
			if (matcher1.find()) {
				for (Entry<Object, Object> mappingsEntry : contentDispositionMappings.entrySet()) {
					String val = processPlaceHolders(pattern, mappingsEntry);
					mappingsEntry.setValue(val);
				}
			}
			setContentDispositionMappings(contentDispositionMappings);
			super.renderReport(populatedReport, model, response);
			setContentDispositionMappings(safeCopy);
		}
	}

	private String processPlaceHolders(Pattern pattern, Entry<Object, Object> mappingsEntry) {
		String val = (String) mappingsEntry.getValue();
		Matcher matcher = pattern.matcher(val);
		while (matcher.find()) {
			StringBuilder builder = new StringBuilder(val);
			String param = matcher.group(2);
			String dateParamPrefix = "date:";
			if (param.startsWith(dateParamPrefix)) {
				val = processDatePattern(val, matcher, builder, param, dateParamPrefix);
				matcher = pattern.matcher(val);
			}
		}
		return val;
	}

	private String processDatePattern(String val, Matcher matcher, StringBuilder builder, String param,
			String dateParamPrefix) {
		String dateformat = param.substring(dateParamPrefix.length());
		Date date = new Date();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
			String dateString = sdf.format(date);
			int startParamChain = matcher.start(1);
			int endParamChain = matcher.end(1);
			String beforeParam = builder.substring(0, startParamChain);
			String afterParam = builder.substring(endParamChain);

			val = beforeParam + dateString + afterParam;

		} catch (IllegalArgumentException e) {
			LOGGER.warn("The report does not specify a valid date format.", e);
		}
		return val;
	}

	public void setReportFileName(String reportFileName) {
		if (reportFileName != null) {
			Properties contentDispositionMappings = getContentDispositionMappings();
			if (reportFileName != null) {
				for (Entry<Object, Object> mappingsEntry : contentDispositionMappings.entrySet()) {
					String val = (String) mappingsEntry.getValue();
					String val2 = val.replace("report", reportFileName);
					mappingsEntry.setValue(val2);
				}
			}
		}
		this.reportFileName = reportFileName;
	}

	public String getReportFileName() {

		return reportFileName;
	}
}
