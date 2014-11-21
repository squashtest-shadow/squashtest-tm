define([ "underscore", "app/squash.handlebars.helpers", "moment" ], function(_, Handlebars, moment) {
	"use strict";

	/**
	 * default handlebars sources
	 */
	var defaultSrc = {
		radio : '<input type="checkbox" name="{{name}}" value="{{value}}" {{checked}} />'
	};

	/**
	 * default compiled handlebars templates
	 */
	var defaultTpl = {};

	/**
	 * default renderers
	 */
	var defaultRenderer = {};

	defaultRenderer.radio = function defaultRadio(data, type, row, meta) {
		if (!_.has(defaultTpl, "radio")) {
			defaultTpl.radio = Handlebars.compile(defaultSrc.radio);
		}

		console.log("radioRenderer", arguments)

		var name = colDef.has("name") ? colDef.name : "radio-" + meta.col;
		var value = name + "-" + meta.row;

		return defaultTpl.radio({
			checked : data,
			name : name,
			value : value
		});
	};

	/**
	 * defines a DSL for building colDefs configuration. usage looks like :
	 * builder.button(0).button(1).build() or
	 * builder.button([0, 1]).build() or
	 * builder.button({targets: [0,1]}).build() or
	 * builder.button(0, "propname").build()
	 */
	function ColDefsBuilder() {
		this._colDefs = [];
	}

	function makeColDef(defaults) {
		return function defineColumn() {
			var targets = arguments[0];
			var data = arguments[1];
			var colDef;

			if (_.isArray(targets) || _.isNumber(targets)) {
				colDef = {
					targets : targets,
					data: data
				};
			} else {
				colDef = targets;
			}

			this._colDefs.push(_.defaults(colDef, defaults));

			return this;
		};
	}

	ColDefsBuilder.prototype.build = function() {
		return this._colDefs;
	};

	ColDefsBuilder.prototype.button = makeColDef({
			sortable : false,
			width : "2em"
		});

	ColDefsBuilder.prototype.index = makeColDef({
			sortable : false,
			width : "2em",
			class : "centered ui-state-default drag-handle select-handle",
			render: function indexRenderer(data, type, row, meta) {
				return meta.row + 1;
			}
		});

	ColDefsBuilder.prototype.std = makeColDef({});

	ColDefsBuilder.prototype.hidden = makeColDef({ visible : false });

	ColDefsBuilder.prototype.radio = makeColDef({
			sortable : false,
			width : "2em",
			render : defaultRenderer.radio
		});

	/**
	 * Creates a "calendar formatted" cell. Data should be an ISO 8601 timestamp
	 */
	ColDefsBuilder.prototype.calendar = makeColDef({
		render : function calendarRenderer(data, type, row, meta) {
			var mom = moment(data);
			return mom.isValid() ? mom.calendar() : "--";
		}
	});

	/**
	 * Creates a date-time cell formatted using the browser locale. Data should be an ISO 8601 timestamp
	 */
	ColDefsBuilder.prototype.datetime = makeColDef({
		render : function datetimeRenderer(data, type, row, meta) {
			var mom = moment(data);
			return mom.isValid() ? mom.format("L LT") : "--";
		}
	});

	/**
	 * golbals for datatable config using 1.10 api
	 */
	function SquashTable() {
	}

	SquashTable.prototype.colDefs = function() {
		return new ColDefsBuilder();
	};

	/**
	 * Creates a Handlebars-based renderer
	 *
	 * @param source
	 *          the HTML source for handlebars
	 * @returns {Function} which creates a renderer when passed a mapper function
	 *
	 * example (kind of stupid) : var stringRenderer = renderer("{{label}}")(function(data, type, row, meta) { return {
	 * label: data } })
	 */
	SquashTable.prototype.renderer = function(source) {
		var template = Handlebars.compile(source);

		return function(mapper) {
			return function(data, type, row, meta) {
				return template(mapper(data, type, row, meta));
			};
		};
	};

	return new SquashTable();
});
