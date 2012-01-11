package org.squashtest.csp.tm.domain.requirement;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Data holder for requirement version creation. We cannot use a requirement version because of its contrained
 * relationship with a requirement.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NewRequirementVersionDto {
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@NotBlank
	private String name;
	private String description;
	@NotNull
	private RequirementCriticality criticality;

	private String reference;

	public RequirementVersion toRequirementVersion() {
		RequirementVersion version = new RequirementVersion();
		version.setName(name);
		if (criticality != null) {
			version.setCriticality(criticality);
		}
		version.setReference(reference);
		version.setDescription(description);

		return version;
	}
}
