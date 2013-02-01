package org.squashtest.tm.web.internal.model.viewmapper;




/**
 * @param <KEY>
 */
public interface DatatableMapper<KEY> {

	/**
	 * Returns the name of the attribute indexed by KEY in the mapper.
	 * 
	 * @param key
	 * @return
	 */
	String attrAt(KEY key);	
	
	
	/**
	 * Returns the path of that attribute. Contrary to {@link #attrAt(Object)}, you will also know of which class it belongs :
	 * the returned data is &lt;simpleclassname&gt;.&lt;attributename&gt;. Example : the name of a TestCase will be returned 
	 * as "TestCase.name".
	 * 
	 * @param key
	 * @return
	 */
	String pathAt(KEY key);
	
	
	/**
	 * will register an attribute named 'attributeName' of a class 'ownerType', of which the type is 'attributeType' and that will be refered to as 'key'.
	 * 
	 * @param ownerType
	 * @param attributeName
	 * @param attributeType
	 * @param key
	 * @return this
	 */
	public DatatableMapper<KEY> mapAttribute(Class<?> ownerType, String attributeName, Class<?> attributeType, KEY key);

}
