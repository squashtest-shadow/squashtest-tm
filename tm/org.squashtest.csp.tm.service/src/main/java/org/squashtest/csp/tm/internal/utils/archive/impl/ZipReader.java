/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
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
		private String name;
		private boolean isDirectory;
		
		
		private ZipReaderEntry(ZipInputStream stream, String name, boolean isDirectory){
			this.zipStream = stream;
			this.name = stripSuffix(name);
			this.isDirectory=isDirectory;
		}
		
		private ZipReaderEntry(ZipInputStream stream, ZipEntry entry){
			this(stream, "/"+entry.getName(), entry.isDirectory());
		}


		@Override
		public String getName(){
			return name;
		}
		
		@Override
		public String getShortName(){
			return getName().replaceAll(".*/", "");
		}
		
		@Override
		public Entry getParent(){
			return new ZipReaderEntry(null, getParentString(), true);
		}
		
		
		//the parent of the root is itself
		private String getParentString(){
			String res = getName().replaceAll("/[^/]*$", "");
			if (res.equals("")){
				res = "/";
			}
			return res;
		}

		@Override
		public boolean isDirectory(){
			return isDirectory;
		}
		
		@Override
		public boolean isFile(){
			return (! isDirectory);
		}
		

		@Override
		public InputStream getStream() {
			if (isFile()){
				return new UnclosableStream(zipStream);
			}else{
				return null;
			}
		}

		
		private String stripSuffix(String original){			
			String res = (original.charAt(original.length()-1)=='/') ? 
					original.substring(0, original.length()-1) 
					: original;
					
			if (res.equals("")){
				res = "/";
			}
			return res;
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
