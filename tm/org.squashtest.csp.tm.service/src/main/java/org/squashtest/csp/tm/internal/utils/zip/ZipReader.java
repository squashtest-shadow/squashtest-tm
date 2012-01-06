package org.squashtest.csp.tm.internal.utils.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * TODO : make an interface for it.
 */
public class ZipReader {
	
	private ZipInputStream zipStream;
	
	private ZipEntry currentEntry;
	
	public ZipReader(InputStream stream){
		setStream(stream);
	}
	
	public void setStream(InputStream stream){
		zipStream = new ZipInputStream(stream);
	}

	
	public boolean selectNextEntry(){
		try{
			currentEntry = zipStream.getNextEntry();
			return (currentEntry!=null);
		}catch(IOException ex){
			throw new ZipCorruptedException(ex);
		}
	}
	
	public boolean isDirectory(){
		return currentEntry.isDirectory();
	}
	
	public boolean isFile(){
		return (! currentEntry.isDirectory());
	}
	
	/* ****************** stream methods ****************** */
	
	
	public void close(){
		try{
			zipStream.close();
		}catch(IOException ex){
			throw new ZipCorruptedException(ex);
		}
		
	}

	public InputStream getEntryAsStream(){
		return new UnclosableStream(zipStream);
	}

	
	/* ***************** naming methods ******************* */
	
	private String strippedName(){
		return stripSuffix(currentEntry.getName());
	}
	
	public String getName(){
		return "/"+strippedName();
	}
	
	public String getShortName(){
		return getName().replaceAll(".*/", "");
	}
	
	public String getParent(){
		return "/"+strippedName().replaceAll("/?[^/]*$", "");
	}

	private String stripSuffix(String original){
		return (original.charAt(original.length()-1)=='/') ? 
				original.substring(0, original.length()-1) 
				: original;  
	}
	
	/* ****************** extra ***************************** */
	
	public static class UnclosableStream extends InputStream{

		private InputStream innerStream;
		
		public UnclosableStream(InputStream stream){
			innerStream = stream;
		}
		
		@Override
		public int read() throws IOException {
			return innerStream.read();
		}
		
		@Override
		public void close(){
			// :P
		}
		
	}
	
	
}
