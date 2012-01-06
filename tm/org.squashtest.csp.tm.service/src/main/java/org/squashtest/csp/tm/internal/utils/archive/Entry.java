package org.squashtest.csp.tm.internal.utils.archive;

import java.io.InputStream;


/**
 * <p>An Entry must be capable of :</p>
 * <ul>
 * 	<li>Give its path. It shall not contain trailing '/' even for folders.</li>
 * 	<li>Give the parent node path.</li>
 * 	<li>Give the filename, without trailing '/', even for folders.</li>
 * 	<li>Tell if its a directory or a file</li>
 * 	<li>Give an InputStream on that entry</li>
 * 	<li>the root of the hierarchy this entry belongs to is always known as '/'</li> 
 * </ul>
 * 
 * 
 * @author bsiri
 *
 */
public interface Entry {

	public String getName();
	
	public String getShortName();
	
	public Entry getParent();
	
	public boolean isDirectory();
	
	public boolean isFile();
	
	/**
	 * must return null if it's a directory, otherwise must return the stream
	 * @return
	 */
	public InputStream getStream();

}
