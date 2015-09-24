package org.squashtest.tm.domain.chart;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "CHART_AXIS_COLUMN")
public class AxisColumn {

	@Id
	@Column(name = "AXIS_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_axis_id_seq")
	@SequenceGenerator(name = "chart_axis_id_seq", sequenceName = "chart_axis_id_seq")
	private long Id;

	private int dimension;

	@NotBlank
	@Size(min = 0, max = 30)
	private String label;

	@JoinColumn(name = "CHART_COLUMN_ID")
	@ManyToOne
	private ColumnPrototype column;

	@Enumerated(EnumType.STRING)
	private Operation operation;

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ColumnPrototype getColumn() {
		return column;
	}

	public void setColumn(ColumnPrototype column) {
		this.column = column;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

}
