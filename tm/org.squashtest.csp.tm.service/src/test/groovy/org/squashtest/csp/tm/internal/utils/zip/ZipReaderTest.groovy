package org.squashtest.csp.tm.internal.utils.zip

import org.apache.poi.hssf.record.formula.functions.T
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

import spock.lang.Specification


class ZipReaderTest extends Specification{
	
	def "should browse zip and bring names"(){
		
		given :
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("binaries/xls.zip");
		
		and : 
			def reader = new ZipReader(stream)
		
		when :
			def names = []
			
			while(
				reader.selectNextEntry()){;
				names << [ reader.getName(), reader.getShortName(), reader.getParent(), reader.isFile() ]
			}
		
		then :
			names.containsAll([
					["/test1.xlsx", "test1.xlsx", "/", true],
					["/test2.xlsx", "test2.xlsx", "/", true],
					["/folder", "folder", "/", false],
					["/folder/test3.xlsx", "test3.xlsx", "/folder", true]
				
				])
	}

	

	
	def "should create valid workbook"(){
		given :
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("binaries/xls.zip");
		
		and :
			def reader = new ZipReader(stream)
			
		when :
			def books = [];
			while(reader.selectNextEntry()){
				if (reader.isFile()){
					Workbook workbook = WorkbookFactory.create(reader.getEntryAsStream());
					books << workbook
				}
			}
			
			
		then :
			books.size() == 3
			books.collect{ it.getSheetAt(0).getRow(0).getCell(0).getStringCellValue() } == [ "qsdqs", "rty", "azer" ]
	}

}
