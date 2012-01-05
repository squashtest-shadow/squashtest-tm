package org.squashtest.csp.tm.internal.utils.zip;

/**
 * thrown when a zip archive cannot be read (or maybe the file is no zip archive)
 * 
 * @author bsiri
 *
 */
public class ZipCorruptedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015267434405584486L;

	
	public ZipCorruptedException(Throwable arg0) {
		super(arg0);
	}
	
	

}
