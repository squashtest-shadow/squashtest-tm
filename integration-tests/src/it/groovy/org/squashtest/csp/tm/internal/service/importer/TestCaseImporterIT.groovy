package org.squashtest.csp.tm.internal.service.importer

import java.nio.charset.Charset;

import javax.inject.Inject;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.CharSet;
import org.apache.xml.serialize.Encodings;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
class TestCaseImporterIT extends DbunitServiceSpecification {

	@Inject
	TestCaseLibraryNavigationService service;
	
	TestCaseImporter importer = new TestCaseImporter();
	
	def setupSpec(){
		Collection.metaClass.contentEquals = { arg -> 
			return delegate.containsAll(arg) && arg.containsAll(delegate) 
		}
		
	}
	
	def setup(){
		importer.service = service
	}
	
	
	
	@DataSet("TestCaseImporterIT.setup.xml")
	def "should import a hierarchy in an empty library"(){
		
		given :
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("import/import-cas-test.zip") 
		
		when :
			def summary = importer.importExcelTestCases(stream, 1l, "Cp858")
		
		then :
			summary.getTotalTestCases() == 13
			summary.getSuccess()  == 13
			summary.getWarnings() == 0
			summary.getFailures() == 0
	
			def rContent = service.findLibrary(1l).rootContent
			
			def names = rContent*.name as Set			
			names ==  ["configuration pas de tir", "prerequis", "échec de la connexion", "succès de la connexion" ] as Set
			
			def confContent = rContent.find{it.name=="configuration pas de tir"}.content
			def confNames = confContent*.name as Set
			confNames == ["approvisionnement fuel", "connexion fusée - pas de tir", 
				"obtenir le go du contrôle", "obtenir le go du technique"] as Set
			
			def fuelFuseeContentNames = confContent.findAll{it instanceof TestCaseFolder }*.content.flatten()*.name as Set
			fuelFuseeContentNames == ["cas limite - fuite de carburant", "remplissage de la soute", "vérification des citernes", "activer robot étage 1", 
									"activer robot étage 2", "activer robot étage 3", "cas limite - blocage robot", "vérifier l'erreur d'alignement"] as Set
	}
	
	
	/*
	 * will check what happen when conflicts happen between transient tc vs persited (tc || folder), transient folder vs persisted folder
	 * and transient folder vs persisted tc
	 * 
	 */
	
	/*
	 * already persisted entities : 
	 * - /configuration pas de tir : folder
	 * - /prerequis : test case
	 * - /configuration pas de tir/approvisionnement fuel : folder
	 * - /configuration pas de tir/obtenir le go du technique : test case
	 * - /configuration pas de tir/connexion fusée - pas de tir : test case
	 * - /configuration pas de tir/approvisionnement fuel/cas limite - fuite de carburant
	 * 
	 */
	
	/*
	 * imported entities : see the resource zip file
	 * 
	 */
	@DataSet("TestCaseImporterIT.setup.xml")
	def "should import a hierarchy in a library where conflicts will happen"(){
		given :
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("import/import-cas-test.zip") 
		
		when :
			def summary = importer.importExcelTestCases(stream, 2l, "Cp858")
		
		then :
			summary.getTotalTestCases() == 13
			summary.getSuccess()  == 13
			summary.getWarnings() == 4
			summary.getFailures() == 0
	
			def rContent = service.findLibrary(2l).rootContent
			
			def names = rContent*.name as Set			
			names ==  ["configuration pas de tir",  "prerequis", "prerequis-import1", "échec de la connexion", "succès de la connexion" ] as Set
			
			def confContent = rContent.find{it.name=="configuration pas de tir"}.content
			def confNames = confContent*.name as Set
			confNames == ["approvisionnement fuel", "connexion fusée - pas de tir", "connexion fusée - pas de tir-import1",
				"obtenir le go du contrôle", "obtenir le go du technique", "obtenir le go du technique-import1"] as Set
			
			def fuelFuseeContentNames = confContent.findAll{it instanceof TestCaseFolder }*.content.flatten()*.name as Set
			fuelFuseeContentNames == ["cas limite - fuite de carburant", "cas limite - fuite de carburant-import1","remplissage de la soute", "vérification des citernes", "activer robot étage 1", 
									"activer robot étage 2", "activer robot étage 3", "cas limite - blocage robot", "vérifier l'erreur d'alignement"] as Set
	}
		

	
	/*
	def "test multiple encodings"(){
		
		given :
			//those are all the encoding supported by java.io and java.lang API for java 1.6.
			//should try the names for java.nio since Apache uses its own charset.
			def encodings = ["Cp858", "Cp437", "Cp775", "Cp850", "Cp852", "Cp855", "Cp857", "Cp862", "Cp866", "ISO8859_1", "ISO8859_2", "ISO8859_4", "ISO8859_5", "ISO8859_7", "ISO8859_9", "ISO8859_13", 
				"ISO8859_15", "KOI8_R", "KOI8_U", "ASCII", "UTF8", "UTF-16", "UnicodeBigUnmarked", "UnicodeLittleUnmarked", "UTF_32", "UTF_32BE", "UTF_32LE", "UTF_32BE_BOM", "UTF_32LE_BOM", "Cp1250", 
				"Cp1251", "Cp1252", "Cp1253", "Cp1254", "Cp1257", "UnicodeBig", "Cp737", "Cp874", "UnicodeLittle", "Big5", "Big5_HKSCS", "EUC_JP", "EUC_KR", "GB18030", "EUC_CN", "GBK", "Cp838", "Cp1140", 
				"Cp1141", "Cp1142", "Cp1143", "Cp1144", "Cp1145", "Cp1146", "Cp1147", "Cp1148", "Cp1149", "Cp037", "Cp1026", "Cp1047", "Cp273", "Cp277", "Cp278", "Cp280", "Cp284", "Cp285", "Cp297", 
				"Cp420", "Cp424", "Cp500", "Cp860", "Cp861", "Cp863", "Cp864", "Cp865", "Cp868", "Cp869", "Cp870", "Cp871", "Cp918", "ISO2022CN", "ISO2022JP", "ISO2022KR", "ISO8859_3", "ISO8859_6", 
				"ISO8859_8", "JIS_X0201", "JIS_X0212-1990", "SJIS", "TIS620", "Cp1255", "Cp1256", "Cp1258", "MS932", "Big5_Solaris", "EUC_JP_LINUX", "EUC_TW", "EUC_JP_Solaris", "Cp1006", "Cp1025", 
				"Cp1046", "Cp1097", "Cp1098", "Cp1112", "Cp1122", "Cp1123", "Cp1124", "Cp1381", "Cp1383", "Cp33722", "Cp834", "Cp856", "Cp875", "Cp921", "Cp922", "Cp930", "Cp933", "Cp935", "Cp937", 
				"Cp939", "Cp942", "Cp942C", "Cp943", "Cp943C", "Cp948", "Cp949", "Cp949C", "Cp950", "Cp964", "Cp970", "ISCII91", "ISO2022_CN_CNS", "ISO2022_CN_GB", "x-iso-8859-11", "x-JIS0208", 
				"JISAutoDetect", "x-Johab", "MacArabic", "MacCentralEurope", "MacCroatian", "MacCyrillic", "MacDingbat", "MacGreek", "MacHebrew", "MacIceland", "MacRoman", "MacRomania", "MacSymbol", 
				"MacThai", "MacTurkish", "MacUkraine", "MS950_HKSCS", "MS936", "PCK", "Cp50220", "Cp50221", "MS874", "MS949", "MS950", "x-windows-iso2022jp"]
			
			File file = new File("C:\\Documents and Settings\\bsiri\\Mes documents\\tests\\plop2\\�.zip")
			
		when :
		
			encodings.each{
				try{
					InputStream stream = new FileInputStream(file)
					def zipstr = new ZipArchiveInputStream(stream, it, false)
					def entry = zipstr.getNextEntry()
					def entryname = entry.getName()
					
					if (entryname=="�.txt") println "$it"
					zipstr.close()
				}catch(Exception e){
				//println "$it : thrown exception ${e.message}"
				}
				
			}
			
		then : true
		
		
	}
	
	def "test charset 2"(){
		
		when :
			def str1 = "���"
			def bytes = str1.getBytes("Cp858")
			
			def str2 = new String(bytes, "UTF8")
		
			println str1
			println str2
			
		then :true
		
	}
	*/
	
	
}
