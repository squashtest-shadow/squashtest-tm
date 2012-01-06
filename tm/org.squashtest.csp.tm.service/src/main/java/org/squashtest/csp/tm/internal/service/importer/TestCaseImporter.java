package org.squashtest.csp.tm.internal.service.importer;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.library.NodeReference;
import org.squashtest.csp.tm.domain.library.structures.StringPathMap;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReaderFactory;
import org.squashtest.csp.tm.internal.utils.archive.Entry;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;


@Component
public class TestCaseImporter {

	@Inject
	private TestCaseLibraryDao dao;
	
	@Inject
	private TestCaseLibraryNavigationService service;
	
	@Inject
	private ArchiveReaderFactory factory;
	
	
	public ImportSummary importExcelTestCases(InputStream archiveStream, Long libraryId){
		
		//init
		StringPathMap pathMap = initPathMap(libraryId);
		
		ArchiveReader reader = factory.createReader(archiveStream);
		
		while(reader.hasNext()){
			
			Entry entry = reader.next();
			
			if (entry.isDirectory()){
				
			}
			
			
		}

		//now, uh... FIXME.
		return null;
		
	}
	
	/**
	 * 
	 * <ol>
	 * 	<li>That method will find the reference of a node given its path in the StringPathMap.</li>
	 * 	<li>In particular of 1/ : if the node is unknown, this node and all its unknown parents will be loaded from the 
	 *   database and added to the map.</li>
	 * 	<li>In particular of 2/ : if this node or its unknown parents cannot be found in the database they will be 
	 * 		created as TestCaseFolders.</li>
	 * 
	 * </ol> 
	 * 
	 * <p>Eventually returns a NodeReference corresponding to the given path.</p>
	 * 
	 * @return
	 */
	private NodeReference findOrCreateParent(StringPathMap map, String path){
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
