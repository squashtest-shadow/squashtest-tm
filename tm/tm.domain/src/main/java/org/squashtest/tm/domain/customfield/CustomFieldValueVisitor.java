package org.squashtest.tm.domain.customfield;

/**
 * Created by jthebault on 21/07/2016.
 */
public interface CustomFieldValueVisitor {

	void visit(CustomFieldValue customFieldValue);

	void visit(NumericValue customFieldValue);

	void visit(RichTextValue customFieldValue);

	void visit(TagsValue customFieldValue);
}
