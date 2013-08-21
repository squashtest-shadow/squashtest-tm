package org.squashtest.tm.domain.testcase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class TestCaseBridgeAttachments implements FieldBridge{

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		
		TestCase testcase = (TestCase) value;
		
		Field field = new Field(name, String.valueOf(testcase.getAttachmentList().size()), luceneOptions.getStore(),
	    luceneOptions.getIndex(), luceneOptions.getTermVector() );
	    field.setBoost( luceneOptions.getBoost());
	    document.add(field);
	}
}
