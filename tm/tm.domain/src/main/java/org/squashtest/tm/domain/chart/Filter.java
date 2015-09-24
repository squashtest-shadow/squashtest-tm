package org.squashtest.tm.domain.chart;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

@Entity
@Table(name = "CHART_FILTER")
public class Filter {

	@Id
	@Column(name = "FILTER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_filter_id_seq")
	@SequenceGenerator(name = "chart_filter_id_seq", sequenceName = "chart_filter_id_seq")
	private long Id;

	@JoinColumn(name = "CHART_COLUMN_ID")
	@ManyToOne
	private ColumnPrototype column;

	@Enumerated(EnumType.STRING)
	private Operation operation;

	@ElementCollection
	@CollectionTable(name = "CHART_FILTER_VALUES", joinColumns = @JoinColumn(name = "FILTER_ID") )
	private List<String> values;

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public ColumnPrototype getColumn() {
		return column;
	}

	public void setColumn(ColumnPrototype column) {
		this.column = column;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
