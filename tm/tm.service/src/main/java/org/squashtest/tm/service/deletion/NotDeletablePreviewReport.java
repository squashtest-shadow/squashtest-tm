/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.service.deletion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;



public class NotDeletablePreviewReport implements SuppressionPreviewReport {

	private static final String NODES_NAMES_MESSAGE_KEY = "squashtm.deletion.preview.notdeletable.whichnodes";
	private static final String WHY_MESSAGE_KEY = "squashtm.deletion.preview.notdeletable.why";
	
	
	private final List<String> nodeNames = new ArrayList<String>();
	private final List<String> why = new ArrayList<String>();
	
	@Override
	public String toString(MessageSource source, Locale locale) {
		StringBuilder builder = new StringBuilder();
		
		if (! nodeNames.isEmpty()){
		
			builder.append(source.getMessage(NODES_NAMES_MESSAGE_KEY, null, locale));
			builder.append(" : ");
			builder.append(listToString(nodeNames));
			builder.append("\n\n");
			
			builder.append(source.getMessage(WHY_MESSAGE_KEY, null, locale));
			builder.append(" : ");
			builder.append(listToString(why));
			builder.append("\n");
			
		}
		return builder.toString();
		
	}
	
	
	public void addName(String name){
		nodeNames.add(name);
	}
	
	public void addWhy(String why){
		this.why.add(why);
	}
	
	private String listToString(List<String> list){
		StringBuilder builder = new StringBuilder();
		
		builder.append(list.get(0));
		
		for (int i=1;i<list.size();i++){
			builder.append(", "+list.get(i));
		}
		
		return builder.toString();
		
	}
	
	
	
}
