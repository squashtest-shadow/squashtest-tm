/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

define([ "jquery", "../domain/FieldValue" ], function($, FieldValue) {

	return {

		options : {
			rendering : {
				inputType : {
					name : "timetracker"
				}

			},
		},
		
		_create : function(){
			var self = this;
			this.element.bind('focusout', function(){
				self.validate();
			});
		},
		
		fieldvalue : function(fieldvalue) {
			if (fieldvalue === null || fieldvalue === undefined) {
				var text = this.evaluateToMinutes(this.element.eq(0).val());
				var typename = this.options.rendering.inputType.dataType;

				return new FieldValue("--", typename, text);
			} else {
				this.element.val(fieldvalue.scalar);
			}
		},
		createDom : function(field) {
			var input = $('<input />', {
				'type' : 'text',
				'data-widgetname' : 'timetracker',
				'data-fieldid' : field.id 
			});
			return input;
		},

		isDigit : function(character) {
			var digits = "0123456789";
			return digits.indexOf(character) != -1;
		},

		evaluateToMinutes : function(){

			var result = this.evaluateField();
			var totalMinutes = 0;
			
			if(!!result){
				var totalDays = result.days + (result.weeks*5);
				var totalHours = result.hours + (totalDays*8);
				var totalMinutes = result.minutes + (totalHours*60);
			}
			
			return totalMinutes;
		}, 
		
		validate : function(){
		
			var messages = []
			var result = this.evaluateField();
			if(!result){
				messages[0] = "validation.error.illformedTimetrackingExpression"
			}
			return messages;
		},
		
		evaluateField : function() {

			// get the string and split it into charachters
			var text = this.element.eq(0).val();
			var array = text.split("");
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
										currentNumber = currentNumber
												+ array[index];
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
			
			var result = new Object();
			result.weeks = weeks;
			result.days = days;
			result.hours = hours;
			result.minutes = minutes;
			
			if(isIllformed){
				result = null;
			}
			
			return result;
		}
	}

})
