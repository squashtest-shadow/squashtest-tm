/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.annotation;

/**
 * <p>
 * 	Will postprocess arbitrary tuples - namely arrays of objects - and selectively picks only some elements
 * 	at the choosen indexes. The indexes are 0-based. 
 * </p>
 * 
 * <p>
 * 	If the database result is a unique object (Iterables excepted), the result will still be a unique "payload" (see below). 
 *  Conversely if the database result is an (Iterable), the result will be a list of "payload"- I'm sorry of you if you expected a Set or else.  
 * </p>
 * 
 * <p>If only one index is specified, and thus you requested only one element of the tuple, the "payload" will be that object alone.
 * If you requested more than one, the payload will be a Object[]
 * </p>
 * 
 * <p>
 * 	If no arg is supplied, the default args will be {0}, ie the payload will be the first element of a tuple.
 * </>
 * 
 * 
 * @author bsiri
 *
 */
public class ListOfTupleComponentsPicker implements ResultPostProcessor {

	@Override
	public Object postprocess(Object dbresult, Object[] indexesToPick) {
		
		int[] indexes = mkIndexes(indexesToPick); 
		
		Object res;
		
		// test whether it is a single result
		if (dbresult instanceof Object[]){
			
		}
		
		return null;
		
	}
	
	private int[] mkIndexes(Object[] indexesToPick){
		int[] res;
		if (indexesToPick == null){
			res = new int[]{0};
		}
		else{
			res = new int[indexesToPick.length];
			for (int i=0; i<indexesToPick.length;i++){
				res[i] = (int)indexesToPick[i];
			}
		}
		return res;
	}

}
