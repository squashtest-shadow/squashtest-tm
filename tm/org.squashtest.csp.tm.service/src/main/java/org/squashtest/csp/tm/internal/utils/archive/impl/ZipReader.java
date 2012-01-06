package org.squashtest.csp.tm.internal.utils.archive.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.Entry;

/*
 * TODO : make an interface for it.
 */
public class ZipReader implements ArchiveReader {
	
	private ZipInputStream zipStream;
	
	
	private ZipReaderEntry currentEntry;
	private ZipReaderEntry nextEntry;
	
	
	public ZipReader(InputStream stream){
		setStream(stream);
	}
	
	@Override
	public void setStream(InputStream stream){
		zipStream = new ZipInputStream(stream);
	}

	@Override
	public void close(){
		try{
			zipStream.close();
		}catch(IOException ex){
			throw new ZipCorruptedException(ex);
		}
		
	}

	/* ****************** nested entry impl****************** */
	
	private static class ZipReaderEntry implements Entry{
		
		private ZipInputStream zipStream;
		private ZipEntry entry;
		
		private ZipReaderEntry(ZipInputStream stream, ZipEntry entry){
			this.zipStream=stream;
			this.entry = entry;
		}


		@Override
		public String getName(){
			return "/"+strippedName();
		}
		
		@Override
		public String getShortName(){
			return getName().replaceAll(".*/", "");
		}
		
		@Override
		public String getParent(){
			return "/"+strippedName().replaceAll("/?[^/]*$", "");
		}

		@Override
		public boolean isDirectory(){
			return entry.isDirectory();
		}
		
		@Override
		public boolean isFile(){
			return (! entry.isDirectory());
		}
		

		@Override
		public InputStream getStream() {
			return new UnclosableStream(zipStream);
		}
		
		
		private String strippedName(){
			return stripSuffix(entry.getName());
		}
		
		private String stripSuffix(String original){
			return (original.charAt(original.length()-1)=='/') ? 
					original.substring(0, original.length()-1) 
					: original;  
		}
		
	}
	
	
	
	/* ****************** extra ***************************** */
	
	private static class UnclosableStream extends InputStream{

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
	
	
	/* ************** iterator impl ************************ */

	@Override
	public boolean hasNext() {
		readNext();
		return (nextEntry!=null);
	}

	@Override
	public Entry next() {
		if (nextEntry == null) readNext();
		currentEntry = nextEntry;
		nextEntry=null;
		return currentEntry;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private void readNext(){
		try{
			ZipEntry entry = zipStream.getNextEntry();
			if (entry!=null){
				nextEntry= new ZipReaderEntry(zipStream, entry);
			}else{
				nextEntry=null;
			}
		}catch(IOException ex){
			throw new ZipCorruptedException(ex);
		}
	}

	
}
