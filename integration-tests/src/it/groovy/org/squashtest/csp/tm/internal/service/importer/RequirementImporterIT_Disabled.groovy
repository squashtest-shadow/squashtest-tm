/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.CharSet;
import org.apache.xml.serialize.Encodings;
import org.hibernate.SessionFactory;
import org.jboss.util.propertyeditor.ClassEditor;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
class RequirementImporterIT_Disabled extends DbunitServiceSpecification {

	@Inject
	RequirementLibraryNavigationService service;
	
	RequirementImporter importer = new RequirementImporter();
	
	@Inject
	SessionFactory sessionFactory;
	
	
	def setup(){
		importer.service = service
		importer.sessionFactory = sessionFactory
		
	}
	
	
	
	@DataSet("RequirementImporterIT.setup.xml")
	def "should import a hierarchy in an empty library"(){
		
		given :
		Class classe = this.getClass()
		ClassLoader classLoader = classe.getClassLoader()
		InputStream stream = classLoader.getResourceAsStream("import/import-requirement.xlsx")
		
		when :
			def summary = importer.importExcelRequirements( stream, 1L)
		
		then : 
			summary.getTotal() == 7
			summary.getSuccess()  == 7
			summary.getRenamed() == 1
			summary.getFailures() == 0
	
//			def rContent = service.findLibrary(1l).rootContent
//			
//			def names = rContent*.name as Set			
//			names ==  ["configuration pas de tir", "prerequis", "échec de la connexion", "succès de la connexion" ] as Set
//			
//			def confContent = rContent.find{it.name=="configuration pas de tir"}.content
//			def confNames = confContent*.name as Set
//			confNames == ["approvisionnement fuel", "connexion fusée - pas de tir", 
//				"obtenir le go du contrôle", "obtenir le go du technique"] as Set
//			
//			def fuelFuseeContentNames = confContent.findAll{it instanceof TestCaseFolder }*.content.flatten()*.name as Set
//			fuelFuseeContentNames == ["cas limite - fuite de carburant", "remplissage de la soute", "vérification des citernes", "activer robot étage 1", 
//									"activer robot étage 2", "activer robot étage 3", "cas limite - blocage robot", "vérifier l'erreur d'alignement"] as Set
	}
	
	
	
}
