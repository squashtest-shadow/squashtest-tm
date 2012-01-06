package org.squashtest.csp.tm.domain.library.structures;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StringPathMap<T> {
	
	private Map<String, T> map = new HashMap<String, T>();

	
	public void put(String path, T ref){
		map.put(path, ref);
	}
	
	public T getMappedElement(String path){
		return map.get(path);
	}
	
	public String getPath(T needle){
		for (Entry<String, T> entry: map.entrySet()){
			T ref = entry.getValue();
			if (ref.equals(needle)){
				return entry.getKey();
			}
		}
		return null;
	}
	
	
	/**
	 * given a path, will return paths corresponding to children present in the map. 
	 * 
	 * @param path
	 * @return
	 */
	public List<String> getKnownChildrenPath(String path){
		
		if (! map.containsKey(path)){
		
			return Collections.emptyList();
		
		}else{
			List<String> children = new LinkedList<String>();
			for (String p : map.keySet()){
				if (p.matches("^"+path+"/?[^/]+$")){
					children.add(p);
				}
			}
			return children;			
		}
	}
	
	
	/**
	 * Given a path begining with a '/', will return all the names composing the path. The first returned elements will always be 
	 * "/", which means the root of course.
	 * 
	 */
	public static List<String> tokenizePath(String path){
		List<String> tokens = new LinkedList<String>();
	
		String[] toks = path.split("/");
		
		tokens.add("/");
		tokens.addAll(Arrays.asList(toks));
		tokens.remove("");
		return tokens;
	}
	
}
