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
		NodeMerger merger = new NodeMerger();
		merger.setMergingContext(this);
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
	

	/*
	 * This class is an adapter to help with the API differences between Libraries and Folders  
	 */
	
	private static class DestinationManager {
		
		protected TestCaseLibraryMerger context;
		
		protected TestCaseLibrary parentLibrary ;
		protected TestCaseFolder parentFolder;

		
		public void setMergingContext(TestCaseLibraryMerger merger){
			this.context=merger;
		}
		
		public void setDestination(TestCaseLibrary library){
			this.parentLibrary=library;
			this.parentFolder=null;
		}
		
		public void setDestination(TestCaseFolder folder){
			this.parentFolder=folder;
			this.parentLibrary=null;
		}
		
		
		protected Collection<TestCaseLibraryNode> getDestinationContent(){
			if (parentLibrary!=null){
				return parentLibrary.getRootContent();
			}else{
				return parentFolder.getContent();
			}
		}
		
		
		protected void persistTestCase(TestCase tc){
			if (parentLibrary!=null){
				context.service.addTestCaseToLibrary(parentLibrary.getId(), tc);
			}else{
				context.service.addTestCaseToFolder(parentFolder.getId(), tc);
			}			
		}
		
		protected void persistFolder(TestCaseFolder folder){
			if (parentLibrary!=null){
				context.service.addFolderToLibrary(parentLibrary.getId(), folder);
			}else{
				context.service.addFolderToFolder(parentFolder.getId(), folder);
			}					
		}
		
		protected void applyConfigurationTo(DestinationManager otherManager){
			otherManager.setMergingContext(context);
			
			if (parentLibrary!=null){
				otherManager.setDestination(parentLibrary);
			}else{
				otherManager.setDestination(parentFolder);
			}	
		}
		
		
	}
	
	
	
	
	private static class NodeMerger extends DestinationManager implements TestCaseLibraryNodeVisitor{


		@Override
		public void visit(TestCase visited) {
			TestCaseMerger tcMerger = new TestCaseMerger();
			
			applyConfigurationTo(tcMerger);
			tcMerger.setTransientTestCase(visited);
			
			tcMerger.merge();
			
		}
		
		@Override
		public void visit(TestCaseFolder visited) {
			FolderMerger fMerger = new FolderMerger();
			
			applyConfigurationTo(fMerger);
			fMerger.setTransientFolder(visited);		
			
			fMerger.merge();
		}
		
		
	}
	
	
	private static class TestCaseMerger extends DestinationManager{
		
		private TestCase toMerge;
		
		public void setTransientTestCase(TestCase tc){
			toMerge=tc;
		}

	
		public void merge(){
			Collection<String> names = collectNames(getDestinationContent());
			
			if (names.contains(toMerge.getName())){
				String newName = generateUniqueName(names, toMerge.getName());
				toMerge.setName(newName);
				context.summary.incrWarnings();				
			}
			
			persistTestCase(toMerge);
		}
		
	}

	
	private static class FolderMerger extends DestinationManager implements TestCaseLibraryNodeVisitor{
		
		private TestCaseFolder toMerge;


		public void setTransientFolder(TestCaseFolder folder){
			this.toMerge=folder;
		}
		
		public void merge(){
			
			Collection<String> names = collectNames(getDestinationContent());
			
			if (names.contains(toMerge.getName())){
				TestCaseLibraryNode conflictingNode = getByName(getDestinationContent(), toMerge.getName());	
				conflictingNode.accept(this);
			}
			else{
				persistFolder(toMerge);
			}
					
		}
		
		//in the case of a conflict with an existing test case we have to rename the transient folder then persist it
		@Override
		public void visit(TestCase persisted) {
			Collection<String> allNames = collectNames(getDestinationContent());
			
			String newName = generateUniqueName(allNames, toMerge.getName());
			toMerge.setName(newName);
			
			context.summary.incrWarnings();
			
			persistFolder(toMerge);
			
		}
		
		
		//in the case of a conflict with an existing folder it's fine : we don't have to persist it.
		//However we must handle the transient content and merge them in turn : we notify the context that it must now merge it.
		@Override
		public void visit(TestCaseFolder persisted) {
			FolderPair pair = new FolderPair(persisted, toMerge);
			context.nonTreated.add(pair);
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
