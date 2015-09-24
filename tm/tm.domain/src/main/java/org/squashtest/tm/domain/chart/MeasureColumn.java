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
@Table(name = "CHART_MEASURE_COLUMN")
public class MeasureColumn {

	@Id
	@Column(name = "MEASURE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_measure_id_seq")
	@SequenceGenerator(name = "chart_measure_id_seq", sequenceName = "chart_measure_id_seq")
	private long Id;

	@JoinColumn(name = "CHART_COLUMN_ID")
	@ManyToOne
	private ColumnPrototype column;

	@NotBlank
	@Size(min = 0, max = 30)
	private String label;

	@Enumerated(EnumType.STRING)
	private Operation operation;

	private int displayOrder;

	public ColumnPrototype getColumn() {
		return column;
	}

	public void setColumn(ColumnPrototype column) {
		this.column = column;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

}
