package org.squashtest.csp.tm.internal.utils.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	
	public String getName(){
		//todo : ensure that the name is cropped from the suffix (folders are suffixed with /)
		return stripSuffix(currentEntry.getName());
	}
	
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

	private String stripSuffix(String original){
		return (original.charAt(original.length()-1)=='/') ? 
				original.substring(0, original.length()-1) 
				: original;  
	}
	
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
	
	/*
	
	public static void copyDirFromStream(InputStream inStream, File output) throws IOException{
		ZipInputStream zipStream = new ZipInputStream(inStream);
		ZipEntry entry;
		
		while((entry=zipStream.getNextEntry())!=null){
			File next= new File(output, entry.getName());
			
			if (entry.isDirectory()){
				if (!next.exists()){
					next.mkdirs();
				}
			}else{
				next.createNewFile();
				
				byte[] buffer = new byte[2048];
				PrintStream outStream = new PrintStream(next);
				int nb;
				
				while((nb=zipStream.read(buffer, 0, 2048))!=-1){
					outStream.write(buffer, 0, nb);
				}
				
				outStream.flush();
				outStream.close();
				
			}
		}
		
		zipStream.close();
		
	}
	*/

	
}
