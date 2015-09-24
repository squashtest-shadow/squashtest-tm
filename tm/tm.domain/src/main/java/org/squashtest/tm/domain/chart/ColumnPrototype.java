package org.squashtest.tm.domain.chart;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "CHART_COLUMN_PROTOTYPE")
public class ColumnPrototype {

	@Id
	@javax.persistence.Column(name = "CHART_COLUMN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_column_id_seq")
	@SequenceGenerator(name = "chart_column_id_seq", sequenceName = "chart_column_id_seq")
	private Long id;

	@NotBlank
	@Size(min = 0, max = 30)
	private String label;

	@Enumerated(EnumType.STRING)
	private EntityType entityType;

	@Enumerated(EnumType.STRING)
	private DataType dataType;

	@CollectionTable(name = "COLUMN_ROLE", joinColumns = @JoinColumn(name = "CHART_COLUMN_ID") )
	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<ColumnRole> role;

	public String getLabel() {
		return label;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public DataType getDataType() {
		return dataType;
	}

	public Set<ColumnRole> getRole() {
		return role;
	}

}
