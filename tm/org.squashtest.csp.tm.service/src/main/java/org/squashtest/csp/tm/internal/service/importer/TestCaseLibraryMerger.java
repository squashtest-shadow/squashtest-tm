package org.squashtest.csp.tm.internal.service.importer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.search.NotTerm;

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;

/**
 * <p>The idea behind the implementation of this class is the following :</p> 
 * 
 * parameters :
 * <ul>
 * 	<li>the receiving persistent container : D <li>
 * 	<li>the detached container containing the transient entities we want to persist : S</li>
 * </ul>
 * 
 * <ul>
 * 	<li>for each n in S :</li>
 * 		<ul>
 * 			<li>if n is a TestCase : </li>
 * 			<ul>
 * 				<li>if name(n) already exists in names(D) :</li>
 * 				<ul>
 * 					<li>rename n with a available name in D</li>
 * 					<li>add a warning regarding this operation</li>
 * 				</ul>
 * 				<li>in any case, persist n into D</li>
 * 			</ul>
 * 			<li>if n is a folder :</li>
 * 			<ul>
 * 				<li>if name(n) already exists in names(D) :</li>
 * 				<ul>
 * 					<li>fetch node p in D where name(p) = name(n)</li>
 * 					<li>if p is a TestCase :</li>
 * 					<ul>
 * 						<li>rename n with an available name in D</li>
 * 						<li>add a warning regarding this operation</li>
 * 						<li>persist n into D</li>
 * 					</ul>
 * 					<li>if p is a Folder</li>
 * 					<ul>
 * 						<li>call this function recursively using D <- p and S <- n </li>
 * 					</ul>
 * 				</ul>
 * 			</ul>
 * 		</ul>
 * </ul>
 * 
 * @author bsiri
 *
 */

/*
 * Node : the use of visitors and the distinct interfaces between libraries and folders made the following implementation unnecessarily complex.
 */

class TestCaseLibraryMerger {

	
	private TestCaseLibraryNavigationService service;
	
	private ImportSummaryImpl summary = new ImportSummaryImpl();
	
	private LinkedList<FolderPair> nonTreated = new LinkedList<FolderPair>();
		
	
	public TestCaseLibraryMerger(){
		super();
	}
	
	public TestCaseLibraryMerger(TestCaseLibraryNavigationService service){
		this.service=service;
	}
	
	

	public void setLibraryService(TestCaseLibraryNavigationService service){
		this.service = service;
	}
	
	public ImportSummary getSummary(){
		return summary;
	}
	
	
	/**
	 * the Library is the root of the hierarchy, and that's where we're importing our data. the data that couldn't be added to the root of the library
	 * (mostly duplicate folders) will be treated in additional loops (see #mergerIntoFolder)
	 * 
	 * @param dest
	 * @param src
	 */
	public void mergeIntoLibrary(TestCaseLibrary dest, TestCaseFolder src){
		
		//phase 1 : add the content of the root of the library
		Merger merger = new Merger(this);
		merger.setDestination(dest);
		
		for (TestCaseLibraryNode node : src.getContent()){
			node.accept(merger);
		}
		
		//phase 2 : if some source folder already exists, then no need to persist it, but we must merge its content instead with the content of the 
		//corresponding persistent entity.
		FolderPair pair;
		
		while(! nonTreated.isEmpty()){
			
			pair = nonTreated.removeFirst();
			
			merger.setDestination(pair.dest);
			
			for (TestCaseLibraryNode node : pair.src.getContent()){
				node.accept(merger);
			}
			
		}
		
				
		
		
	}
	


	
	/* ******************************** private classes ************************************ */
	
	private static class FolderPair {
		TestCaseFolder dest;
		TestCaseFolder src;
		
		public FolderPair(TestCaseFolder dest, TestCaseFolder src){
			this.dest=dest;
			this.src=src;
		}
		
	}
	
	
	
	private static class Merger implements TestCaseLibraryNodeVisitor{
		
		TestCaseLibraryMerger context;
		
		TestCaseLibrary parentLibrary ;
		TestCaseFolder parentFolder;
		
		
		public Merger(TestCaseLibraryMerger merger){
			this.context = merger;
		}
		
		public void setDestination(TestCaseLibrary library){
			this.parentLibrary=library;
			this.parentFolder=null;
		}
		
		public void setDestination(TestCaseFolder folder){
			this.parentFolder=folder;
			this.parentLibrary=null;
		}
		
		
		@Override
		public void visit(TestCase visited) {
			Collection<String> names = collectNames(getDestinationContent());
			
			if (names.contains(visited.getName())){
				String newName = generateUniqueName(names, visited.getName());
				visited.setName(newName);
				context.summary.incrWarnings();				
			}
			
			persistTestCase(visited);
			
		}
		
		@Override
		public void visit(TestCaseFolder visited) {
			FolderNameConflictResolver resolver = new FolderNameConflictResolver(context, visited);
			
			if (parentLibrary!=null){
				resolver.uses(parentLibrary);
			}else{
				resolver.uses(parentFolder);
			}
			
			resolver.resolve();
		}
		
		
		
		
		private Collection<TestCaseLibraryNode> getDestinationContent(){
			if (parentLibrary!=null){
				return parentLibrary.getRootContent();
			}else{
				return parentFolder.getContent();
			}
		}
		
		
		private void persistTestCase(TestCase tc){
			if (parentLibrary!=null){
				context.service.addTestCaseToLibrary(parentLibrary.getId(), tc);
			}else{
				context.service.addTestCaseToFolder(parentFolder.getId(), tc);
			}			
		}
	}
	
	
	
	
	private static class FolderNameConflictResolver implements TestCaseLibraryNodeVisitor{
		
		TestCaseLibraryMerger context;
		TestCaseFolder transientFolder;
		
		TestCaseLibrary parentPersistentLibrary=null;
		TestCaseFolder parentPersistentFolder=null;

		public FolderNameConflictResolver(TestCaseLibraryMerger merger, TestCaseFolder transientFolder){
			this.context=merger;
			this.transientFolder=transientFolder;			
		}
		
		public void uses(TestCaseLibrary lib){
			parentPersistentLibrary=lib;
			parentPersistentFolder=null;
			
		}
		
		public void uses(TestCaseFolder fold){
			parentPersistentFolder=fold;
			parentPersistentLibrary=null;
		}
		
		public void resolve(){
			
			TestCaseLibraryNode conflictingNode = getByName(getPersistentChildren(), transientFolder.getName());			
			
			conflictingNode.accept(this);
			
		}
		
		//in the case of a conflict with an existing test case we have to rename the transient folder then persist it
		@Override
		public void visit(TestCase persisted) {
			Collection<String> allNames = collectNames(getPersistentChildren());
			
			String newName = generateUniqueName(allNames, transientFolder.getName());
			transientFolder.setName(newName);
			
			context.summary.incrWarnings();
			
			persistFolder();
			
		}
		
		
		//in the case of a conflict with an existing folder, then we don't want to rename and persist the transient folder : we want to 
		//persist its children into the persistent one.
		@Override
		public void visit(TestCaseFolder persisted) {
			FolderPair pair = new FolderPair(persisted, transientFolder);
			context.nonTreated.add(pair);
		}
		
		
		
		private Collection<TestCaseLibraryNode> getPersistentChildren(){
			if (parentPersistentLibrary!=null){
				return parentPersistentLibrary.getRootContent();
			}else{
				return parentPersistentFolder.getContent();
			}
		}
		
		
		private void persistFolder(){
			if (parentPersistentLibrary!=null){
				context.service.addFolderToLibrary(parentPersistentLibrary.getId(), transientFolder);
			}else{
				context.service.addFolderToFolder(parentPersistentFolder.getId(), transientFolder);
			}			
		}
	}
	 
		
	/* ******************************** util functions ************************************* */
	
	
	private static Collection<String> collectNames(Collection<TestCaseLibraryNode> nodes){
		List<String> res = new LinkedList<String>();
		
		for (TestCaseLibraryNode node : nodes){
			res.add(node.getName());
		}
		
		return res;
	}
	
	
	/* 
	 * mostly copy pasta from AbstractLibraryNavigationService#generateUniqueCopyNumber(List<String>)
	 *  
	 */
	private static String generateUniqueName(Collection<String> pickedNames, String baseName){
		
		int higherIndex = 0;
		//we want to match one or more digits following the first instance of substring -Import
		Pattern pattern = Pattern.compile(baseName+"-import(\\d+)");
	
		for (String copyName : pickedNames) {
			
			Matcher matcher = pattern.matcher(copyName);
			
			if (matcher.find()){
								
				String index = matcher.group(1);

				if (higherIndex < Integer.parseInt(index)) {
					higherIndex = Integer.parseInt(index);
				}			
			}

		}
		
		int newIndex = higherIndex + 1;
		return baseName+"-import"+newIndex;
	}
	
	private static TestCaseLibraryNode getByName(Collection<TestCaseLibraryNode> hayStack, String needle){
		for (TestCaseLibraryNode node : hayStack ){
			if (node.getName().equals(needle)){
				return node;
			}
		}
		throw new RuntimeException("that method should never have been called if not preceeded by a preventive call to "+
				"collectName().contains() or if this preventive call returned false - something is wrong with your code dude ");
		
	}
	
	
}
