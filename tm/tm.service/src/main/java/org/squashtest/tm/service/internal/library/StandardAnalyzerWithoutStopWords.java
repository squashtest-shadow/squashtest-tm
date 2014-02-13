/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.library;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.util.Version;

public class StandardAnalyzerWithoutStopWords extends Analyzer{

	private StandardAnalyzer standardAnalyzer;

	public StandardAnalyzerWithoutStopWords(Version version){
		Set<String> stopwords = new HashSet<String>();
		standardAnalyzer = new StandardAnalyzer(version, stopwords);
	}
	
	public StandardAnalyzerWithoutStopWords(){
		Set<String> stopwords = new HashSet<String>();
		standardAnalyzer = new StandardAnalyzer(Version.LUCENE_36, stopwords);
	}

	public int	getMaxTokenLength(){
		return standardAnalyzer.getMaxTokenLength(); 			
	}

	public void setMaxTokenLength(int length){
		standardAnalyzer.setMaxTokenLength(length); 	
	}
	
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException{
		return standardAnalyzer.reusableTokenStream(fieldName, reader);
	}
	
	public TokenStream tokenStream(String fieldName, Reader reader){
		return standardAnalyzer.tokenStream(fieldName, reader);
	}
	
	public int getOffsetGap(Fieldable field){
		return standardAnalyzer.getOffsetGap(field);
	}
	
	public int getPositionIncrementGap(String fieldName){
		return standardAnalyzer.getPositionIncrementGap(fieldName);
	}
	
	public Set<?> getStopwordSet(){
		return standardAnalyzer.getStopwordSet();
	}
}
