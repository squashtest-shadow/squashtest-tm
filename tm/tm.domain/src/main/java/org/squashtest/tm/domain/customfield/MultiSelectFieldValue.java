package org.squashtest.tm.domain.customfield;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

@Entity
@DiscriminatorValue("MFV")
public class MultiSelectFieldValue extends CustomFieldValue {
	
	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_VALUE_OPTION", joinColumns = @JoinColumn(name = "CFV_ID"))
	@OrderColumn(name = "POSITION")
	@Valid
	private List<CustomFieldValueOption> options;
	
	public List<CustomFieldValueOption> getOptions() {
		return options;
	}

	public void addCUFieldValueOption(CustomFieldValueOption cufVO){
		options.add(cufVO);
	}
	
	public void removeCUFValueOption(CustomFieldValueOption cufVO){
		options.remove(cufVO);
	}

	
	@Override
	public String getValue(){
		// TODO : return concatenated tag separated by semicolons.
		return null;
	}
	
	@Override
	public CustomFieldValue copy(){
		CustomFieldValue copy = new MultiSelectFieldValue();
		copy.setBinding(getBinding());
		copy.setValue(getValue());
		return copy;
	}
}
