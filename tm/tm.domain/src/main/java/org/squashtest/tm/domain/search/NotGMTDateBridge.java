package org.squashtest.tm.domain.search;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.joda.time.LocalDateTime;
import org.springframework.util.ReflectionUtils;

public class NotGMTDateBridge implements FieldBridge {




	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value == null) {
			return;
		}

		
		Date date = (Date) value;
		Field miliField = ReflectionUtils.findField(LocalDateTime.class, "iLocalMillis");
		try {

			miliField.setAccessible(true);
			long numericDate = (long) ReflectionUtils.getField(miliField,
					new LocalDateTime(date.getTime()).withTime(0, 0, 0, 0));
			luceneOptions.addNumericFieldToDocument(name, numericDate, document);
			
		} finally{
			miliField.setAccessible(false);
		}
	

	
	}


}
