package org.squashtest.csp.tm.internal.service.importer;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.library.NodeReference;
import org.squashtest.csp.tm.domain.library.structures.StringPathMap;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;


@Component
public class TestCaseImporter {

	@Inject
	private TestCaseLibraryDao dao;
	
	@Inject
	private TestCaseLibraryNavigationService service;
	
	
	public ImportSummary importExcelTestCases(InputStream archiveStream, Long libraryId){
		
		//init
		StringPathMap pathMap = initPathMap(libraryId);
		
		

		//now, uh... FIXME.
		return null;
		
	}
	
	/**
	 * Will return a path map populated with the root content of the given test case library.
	 * 
	 * @param libraryId
	 * @return
	 */
	private StringPathMap initPathMap(Long libraryId){
		
		StringPathMap map = new StringPathMap();
		
		TestCaseLibrary library = dao.findById(libraryId);
		NodeReference libReference = new NodeReference(library.getId(), "/");
		map.put("/", libReference);
		
		
		List<NodeReference> rootContent = dao.findRootContentReferences(libraryId);
		
		
		for (NodeReference ref : rootContent){
			map.put("/"+ref.getName(), ref);
		}
		
		return map;
	}
	
	
}
