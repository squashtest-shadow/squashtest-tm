/**
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
package org.squashtest.tm.web.internal.report.services

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.requirement.ExportRequirementData
import org.squashtest.tm.domain.requirement.RequirementCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.web.internal.report.service.JasperReportsService

import spock.lang.Specification


public class JasperReportsServiceImplTest extends Specification {
	
	private JasperReportsService jrService = new JasperReportsService();
	
	
	def "should export some requirements"(){
		
		given :	
			URL fileURL = getClass().getClassLoader().getResource("requirement-export.jasper");
			File file = new File(fileURL.getFile());
			InputStream reportStream = new FileInputStream(file);
			
			
			def data1 = generateExportData("a", RequirementCriticality.MAJOR, RequirementCategory.BUSINESS, "a", "a", "a", "a");
			def data2 = generateExportData("b", RequirementCriticality.MINOR, RequirementCategory.NON_FUNCTIONAL, "b", "b", "b", "b");
			def data3 = generateExportData("c", RequirementCriticality.UNDEFINED,RequirementCategory.FUNCTIONAL, "c", "c", "c", "c");
			
			def dataSource = [data1, data2, data3];		
			
		when :
		
			InputStream resStream = jrService.getReportAsStream(reportStream, "csv", dataSource, new HashMap(), new HashMap());		
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(resStream));
			
			def strData1 = reader.readLine();
			def strData2 = reader.readLine();
			def strData3 = reader.readLine();
			
		then :

			
			strData1 == "a,a,a,MAJOR,a,a,BUSINESS"
			strData2 == "b,b,b,MINOR,b,b,NON_FUNCTIONAL"
			strData3 == "c,c,c,UNDEFINED,c,c,FUNCTIONAL"
	
		
		
	}
	
	private ExportRequirementData generateExportData(String name, RequirementCriticality crit, RequirementCategory cat, String project, String foldername, String ref, String desc){
		ExportRequirementData data = new ExportRequirementData();
		data.setId(1l)
		data.setCriticality(crit)
		data.setCategory(cat)
		data.setDescription(desc)
		data.setFolderName(foldername)
		data.setName(name)
		data.setProject(project)
		data.setReference(ref)
		return data;
	}
	
}
