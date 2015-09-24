package org.squashtest.tm.domain.chart;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.users.User;

@Entity
@Table(name = "CHART_DEFINITION")
public class ChartDefinition {

	@Id
	@Column(name = "CHART_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "chart_def_id_seq")
	@SequenceGenerator(name = "chart_def_id_seq", sequenceName = "chart_def_id_seq")
	private long Id;

	@NotBlank
	@Size(min = 0, max = 30)
	private String name;

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@Enumerated(EnumType.STRING)
	private Visibility visibility;

	@Enumerated(EnumType.STRING)
	private ChartType type;

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_FILTER", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "FILTER_ID") )
	private List<Filter> filters;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_AXIS", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "AXIS_ID") )
	private List<AxisColumn> axis;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CHART_MEASURE", joinColumns = @JoinColumn(name = "CHART_ID") , inverseJoinColumns = @JoinColumn(name = "MEASURE_ID") )
	private List<MeasureColumn> measure;


}
