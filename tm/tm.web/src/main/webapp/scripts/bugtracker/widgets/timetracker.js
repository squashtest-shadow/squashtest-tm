/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define([ "jquery", "../domain/FieldValue", "squash.translator", "handlebars" ], function($, FieldValue, translator, Handlerbars) {

	return {

		options : {
			rendering : {
				inputType : {
					name : "timetracker"
				}

			}
		},
		
		_create : function(){
			var self = this;
			this.element.bind('focusout', function(){
				self.autovalidate();
			});
		},
		
		fieldvalue : function(fieldvalue) {
			if (fieldvalue === null || fieldvalue === undefined) {
				
				var field = this.options;
				var original = this.evaluateToMinutes($($("input", this.element.eq(0))[0]).val());
				var remaining = this.evaluateToMinutes($($("input", this.element.eq(0))[1]).val());
				var typename = this.options.rendering.inputType.dataType;

				var allValues = [];
				
				var originalValue = new FieldValue("originalEstimate", "string", original);
				var remainingValue = new FieldValue("remainingEstimate", "string", remaining);
				
				allValues.push(originalValue);
				allValues.push(remainingValue);
				 
				return new FieldValue(field.id, "composite", allValues);
				
			} else {
				this.element.val(fieldvalue.scalar);
			}
		},
		createDom : function(field) {
			
			var div = $('<span/>', {
				'type' : 'text',
				'data-widgetname' : 'timetracker',
				'data-fieldid' : field.id,
				'class' : 'full-width issue-field-control'
			});
			
			var label1 = $('<label/>', {'class' : 'issue-field-label'});
			var input1 = $('<input />');
			
			var label2 = $('<label/>', {'class' : 'issue-field-label'});
			var input2 = $('<input />');
			
			label1.text(translator.get("widget.timetracker.original-estimate"));
			label2.text(translator.get("widget.timetracker.remaining-estimate"));
			
			div.append(label1);
			div.append(input1);
			div.append("<br/>");
			div.append(label2);
			div.append(input2);
			
			return div;
		},

		isDigit : function(character) {
			var digits = "0123456789";
			return digits.indexOf(character) != -1;
		},

		evaluateToMinutes : function(expression){

			var result = this.evaluateField(expression);
			var totalMinutes = "";
			
			if(!!result){
				var totalDays = parseInt(result.days,10) + (parseInt(result.weeks,10)*5);
				var totalHours = parseInt(result.hours,10) + (totalDays*8);
				totalMinutes = parseInt(result.minutes,10) + (totalHours*60);
			}
			
			return totalMinutes;
		}, 
		
		validate : function(){
		
			var messages = [];
			
			var result1 = this.evaluateField($($("input", this.element.eq(0))[0]).val());
			var result2 = this.evaluateField($($("input", this.element.eq(0))[1]).val());
			
			if(!result1 || !result2){
				messages[0] = "validation.error.illformedTimetrackingExpression";
			}

			return messages;
		},
		
		autovalidate : function(){
			
			var messages = this.validate();
			
			$(".issue-field-message-holder", this.element.parent().parent()).text("");
			for(var i=0; i<messages.length; i++){
				$(".issue-field-message-holder", this.element.parent().parent()).append(translator.get(messages[i]));	
			}
			if(!!messages.length){
				$(".issue-field-message-holder", this.element.parent().parent()).show();
			}
		},
		
		evaluateField : function(expression) {

			// get the string and split it into charachters
			var array = expression.split("");
			var index = 0;

			// current number
			var currentNumber = "";

			var weeks = "";
			var days = "";
			var hours = "";
			var minutes = "";

			var hasWeeks = false;
			var hasDays = false;
			var hasHours = false;
			var hasMinutes = false;

			var isIllformed = false;

			while (index < array.length) {

				if (array[index] == "w") {
					if (!!currentNumber && !hasWeeks) {
						weeks = currentNumber;
						hasWeeks = true;
						currentNumber = "";
					} else {
						isIllformed = true;
					}
				} else {
					if (array[index] == "d") {
						if (!!currentNumber && !hasDays) {
							days = currentNumber;
							hasDays = true;
							currentNumber = "";
						} else {
							isIllformed = true;
						}
					} else {
						if (array[index] == "h") {
							if (!!currentNumber && !hasHours) {
								hours = currentNumber;
								hasHours = true;
								currentNumber = "";
							} else {
								isIllformed = true;
							}
						} else {
							if (array[index] == "m") {
								if (!!currentNumber && !hasMinutes) {
									minutes = currentNumber;
									hasMinutes = true;
									currentNumber = "";
								} else {
									isIllformed = true;
								}
							} else {
								if (this.isDigit(array[index])) {

									if (!!currentNumber && !hasMinutes) {
										minutes = currentNumber;
										hasMinutes = true;
										currentNumber = "";
									}
									if (!!currentNumber && hasMinutes) {
										isIllformed = true;
									}

									do {
										currentNumber = currentNumber + array[index];
										index++;
									} while (this.isDigit(array[index]));
									index--;
								} else {

									if (array[index] == " ") {

									} else {
										isIllformed = true;
									}
								}
							}
						}
					}
				}
				index++;
			}

			if (!!currentNumber && !hasMinutes) {
				minutes = currentNumber;
				hasMinutes = true;
				currentNumber = "";
			}
			if (!!currentNumber && hasMinutes) {
				isIllformed = true;
			}
			
			var result = {};
			result.weeks = weeks;
			result.days = days;
			result.hours = hours;
			result.minutes = minutes;
			
			if(isIllformed){
				result = null;
			}
			
			return result;
		}
	};

});
