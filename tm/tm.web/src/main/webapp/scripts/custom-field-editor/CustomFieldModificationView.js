/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
define([ "jquery", "./NewCustomFieldOptionDialog", "backbone", "underscore",
	"jeditable.simpleJEditable", "jeditable.selectJEditable",
	"app/util/StringUtil", "app/lnf/Forms", "jquery.squash.oneshotdialog" , "jquery.squash", "jqueryui",
	"jquery.squash.togglepanel", "squashtable", "jquery.squash.messagedialog", "jquery.squash.confirmdialog",
	"jeditable.datepicker", "datepicker/jquery.squash.datepicker-locales", "squashtest/jquery.squash.popuperror" ],
	function($, NewCustomFieldOptionDialog, Backbone, _, SimpleJEditable,SelectJEditable, StringUtil, Forms, oneshot) {
		var cfMod = squashtm.app.cfMod;
		/*
		 * Defines the controller for the custom fields table.
		 */
		var CustomFieldModificationView = Backbone.View.extend({
			el : "#information-content",
			initialize : function() {
				this.inputType = $("#cuf-inputType").data("type");

				this.optionalCheckbox = this.$("#cf-optional").get(0);

				this.configureTogglePanels();
				this.configureEditables();
				this.configureRenamePopup();
				this.configureRenameOptionPopup();
				this.configureChangeOptionCodePopup();
				this.configureOptionTable();
				this.configureButtons();
				// this line below is here because toggle panel
				// buttons
				// cannot be bound with the 'events' property of
				// Backbone.View.
				// my guess is that the event is bound to the button
				// before it is moved from it's "span.not-displayed"
				// to the toggle panel header.
				// TODO change our way to make toggle panels buttons
				this.$("#add-cuf-option-button").on("click",
						$.proxy(this.openAddOptionPopup, this));

				// dialog is moved from DOM when widgetized => we
				// need to store it
				this.confirmDeletionDialog = this.$(
						"#delete-warning-pane").confirmDialog();
				// ...and we cannot use the events hash
				this.confirmDeletionDialog.on(
						"confirmdialogconfirm", $.proxy(
								this.deleteCustomField, this));
			},

			events : {
				"click #cf-optional" : "confirmOptional",
				"click .is-default>input:checkbox" : "changeDefaultOption",
				"click .opt-label" : "openRenameOptionPopup",
				"click .opt-code" : "openChangeOptionCodePopup",
				"click #delete-cuf-button" : "confirmCustomFieldDeletion"
			},

			confirmCustomFieldDeletion : function(event) {
				this.confirmDeletionDialog.confirmDialog("open");
			},

			deleteCustomField : function(event) {
				var self = this;

				$.ajax({
					type : "delete",
					url : document.location.href
				}).done(function() {
					self.trigger("customfield.delete");
				});

			},

			confirmOptional : function(event) {
				var self = this;
				var checked = event.target.checked;

				if (checked) {
					self.sendOptional(checked);

				} else {
					var defaultValue = self.findDefaultValue();
					if (StringUtil.isBlank(defaultValue) ||
						defaultValue === cfMod.richEditPlaceHolder ||
						defaultValue === cfMod.noDateLabel) {
							$.squash.openMessage(cfMod.popupErrorTitle,
									cfMod.mandatoryNeedsDefaultMessage,
									350);
							event.target.checked = true;
							return;
					}
					var message = cfMod.confirmMandatoryMessage;
					message = self.replacePlaceHolderByValue(0,
							message, defaultValue);
					oneshot.show(cfMod.confirmMandatoryTitle,
							message, { width : '500px'} )
							.done(function() {
								self.sendOptional(checked);
							})
							.fail(function() {
								event.target.checked = true;
							});
				}
			},

			findDefaultValue : function() {
				var defaultValueDiv = this.$('#cuf-default-value');

				if (defaultValueDiv && defaultValueDiv.length > 0) {
					return $(defaultValueDiv[0]).text();

				} else if (this.optionsTable) {
					var checkedDefault = this.optionsTable
							.find('td.is-default input:checked');
					if (checkedDefault) {
						return checkedDefault.val();
					}
				}
				return "";
			},

			replacePlaceHolderByValue : function(index, message,
					replaceValue) {
				var pattern = /\{[\d,\w,\s]*\}/;
				var match = pattern.exec(message);
				var pHolder = match[index];
				return message.replace(pHolder, replaceValue);
			},

			sendOptional : function(optional) {
				var self = this;
				return $.ajax({
					url : cfMod.customFieldUrl + "/optional",
					type : "post",
					data : {
						'value' : optional
					},
					dataType : "json"
				})
				.fail(function(){
					self.cancelOptionalChange();
				});

			},

			changeDefaultOption : function(event) {
				var self = this;
				var checkbox = event.currentTarget;
				var option = checkbox.value;
				var defaultValue = checkbox.checked ? option : "";
				if (defaultValue === "" && this.isFieldMandatory()) {
					checkbox.checked = true;
					$.squash.openMessage(cfMod.popupErrorTitle,
							cfMod.defaultOptionMandatoryMessage);
					return;
				}
				var uncheckSelector = ".is-default>input:checkbox" + (checkbox.checked ? "[value!='" + option + "']" : "");

				this.sendDefaultValue(defaultValue).done(
						function() {
							self.optionsTable.find(uncheckSelector)
									.attr("checked", false);
						}).fail(function() {
					checkbox.checked = !checkbox.checked;
				});

			},

			sendDefaultValue : function(defaultValue) {
				return $.ajax({
					url : cfMod.customFieldUrl + "/defaultValue",
					type : 'POST',
					data : {
						'value' : defaultValue
					},
					dataType : 'json'
				});
			},

			openRenameOptionPopup : function(event) {
				var self = this;
				var labelCell = event.currentTarget;
				var previousValue = $(labelCell).text();

				self.renameCufOptionPopup.find(
						"#rename-cuf-option-previous").text(
						previousValue);
				self.renameCufOptionPopup.find(
						"#rename-cuf-option-label").val(
						previousValue);
				self.renameCufOptionPopup.dialog("open");
			},

			openChangeOptionCodePopup : function(event) {
				var self = this;
				var codeCell = event.currentTarget;
				var previousValue = $(codeCell).text();
				var label = $(codeCell).parent("tr").find(
						"td.opt-label").text();
				self.changeOptionCodePopup.find(
						"#change-cuf-option-code-label")
						.text(label);
				self.changeOptionCodePopup.find(
						"#change-cuf-option-code").val(
						previousValue);
				self.changeOptionCodePopup.dialog("open");
			},

			renameOption : function() {
				var self = this;
				var previousValue = self.renameCufOptionPopup.find(
						"#rename-cuf-option-previous").text();
				var newValue = self.renameCufOptionPopup.find(
						"#rename-cuf-option-label").val();
				$.ajax({
					type : 'POST',
					data : {
						'value' : newValue
					},
					dataType : "json",
					url : cfMod.optionsTable.ajaxSource	+ "/" + previousValue + "/label"
				}).done(function(data) {
					self.optionsTable.refresh();
				});

			},

			changeOptionCode : function() {
				var self = this;
				var label = self.changeOptionCodePopup.find(
						"#change-cuf-option-code-label").text();
				var newValue = self.changeOptionCodePopup.find(
						"#change-cuf-option-code").val();
				$.ajax(
						{
							type : 'POST',
							data : {
								'value' : newValue
							},
							dataType : "json",
							url : cfMod.optionsTable.ajaxSource	+ "/" + label + "/code"
						}).done(function(data) {
					self.optionsTable.refresh();
				});

			},

			configureButtons : function() {
				$.squash.decorateButtons();
			},

			configureTogglePanels : function() {
				var informationSettings = {
					initiallyOpen : true,
					title : cfMod.informationPanelLabel
				};
				this.$("#cuf-info-panel").togglePanel(
						informationSettings);
				var optionSettings = {
					initiallyOpen : true,
					title : cfMod.optionsPanelLabel
				};
				this.$("#cuf-options-panel").togglePanel(
						optionSettings);
			},

			configureEditables : function() {
				var self = this;
				this.makeSimpleJEditable("cuf-label");
				this.makeSimpleJEditable("cuf-code");

				if (this.inputType === "PLAIN_TEXT") {
					this.makeDefaultSimpleJEditable();
					$("#cuf-default-value").click(
							self.disableOptionalChange);
				} else if (this.inputType === "CHECKBOX") {
					this.makeDefaultSelectJEditable();
					$("#cuf-default-value").click(
							self.disableOptionalChange);
				} else if (this.inputType === "DATE_PICKER") {
					this.makeDefaultDatePickerEditable();
				}

			},
			makeDefaultSimpleJEditable : function() {
				var self = this;
				new SimpleJEditable(
						{
							targetUrl : function(value, settings) {
								if (self
										.changeDefaultValueText(value)) {
									return value;
								} else {
									return this.revert;
								}
							},
							componentId : "cuf-default-value",
							jeditableSettings : {
								callback : self.enableOptionalChange
							}
						});
			},
			makeDefaultSelectJEditable : function() {
				var self = this;
				new SelectJEditable(
						{
							language : {
								richEditPlaceHolder : cfMod.richEditPlaceHolder,
								okLabel : cfMod.okLabel,
								cancelLabel : cfMod.cancelLabel
							},
							target : cfMod.customFieldUrl,
							componentId : "cuf-default-value",
							jeditableSettings : {
								callback : self.enableOptionalChange,
								data : JSON
										.stringify(cfMod.checkboxJsonDefaultValues)
							}
						});
			},
			makeDefaultDatePickerEditable : function(inputId) {
				var self = this;
				var datepick = this.$("#cuf-default-value");

				// configure editable datepicker settings :
				var dateSettings = {
					dateFormat : cfMod.dateFormat
				};
				var locale = datepick.data('locale');
				var confLocale = $.datepicker.regional[locale];
				if (!!confLocale) {
					$.extend(dateSettings, confLocale);
				}

				// function to be called by the editable
				var onDatepickerChanged = function(value) {
					var localizedDate = value;
					var postDateFormat = $.datepicker.ATOM;
					var date = $.datepicker.parseDate(
							cfMod.dateFormat, localizedDate);
					var postDate = $.datepicker.formatDate(
							postDateFormat, date);

					if (self.changeDefaultValueText(postDate)) {
						if (value === "") {
							return cfMod.noDateLabel;
						} else {
							return value;
						}
					} else {
						return this.revert;
					}
				};

				// make editable
				datepick.editable(function(value, settings) {
					return onDatepickerChanged.call(this, value);
				}, {
					type : 'datepicker',
					tooltip : cfMod.richEditPlaceHolder,
					datepicker : dateSettings
				});

			},
			disableOptionalChange : function() {
				$("#cf-optional").attr("disabled", true);
			},

			enableOptionalChange : function() {
				$("#cf-optional").removeAttr("disabled");
			},

			cancelOptionalChange : function(){
				var opt = $("#cf-optional"),
					active = opt.prop("checked");

				opt.prop("checked", ! active);
			},

			changeDefaultValueText : function(value) {
				if (this.isFieldMandatory()	&& StringUtil.isBlank(value)) {
					$.squash.openMessage(cfMod.popupErrorTitle,
							cfMod.defaultValueMandatoryMessage);
					return false;
				} else {
					this.sendDefaultValue(value);
					return true;
				}
			},

			isFieldMandatory : function() {
				return !this.optionalCheckbox.checked;
			},

			renameCuf : function() {
				var newNameVal = $("#rename-cuf-input").val();
				$.ajax({
					type : 'POST',
					data : {
						'value' : newNameVal
					},
					dataType : "json",
					url : cfMod.customFieldUrl + "/name"

				}).done(function(data) {
					$('#cuf-name-header').html(data.newName);
					$('#rename-cuf-popup').dialog('close');
				});
			},

			configureRenamePopup : function() {
				var params = {
					selector : "#rename-cuf-popup",
					title : cfMod.renameCufTitle,
					openedBy : "#rename-cuf-button",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons : [ {
						'text' : cfMod.renameLabel,
						'click' : this.renameCuf
					}, {
						'text' : cfMod.cancelLabel,
						'click' : this.closePopup
					} ]
				};
				squashtm.popup.create(params);
				$("#rename-cuf-popup").bind(
						"dialogopen",
						function(event, ui) {
							var name = $.trim($('#cuf-name-header')
									.text());
							$("#rename-cuf-input")
									.val($.trim(name));
						});

			},

			closePopup : function() {
				$(this).data("answer", "cancel");
				$(this).dialog('close');
			},

			makeSimpleJEditable : function(inputId) {
				var self = this;

				var onerror = function(settings, original, xhr) {
					var json = JSON.parse(xhr.responseText);
					xhr.errorIsHandled = true;
					if (!!json.fieldValidationErrors && !!json.fieldValidationErrors[0].errorMessage) {
						Forms.input(self.$("#" + inputId)).setState("error", json.fieldValidationErrors[0].errorMessage);
					}
					return ($.editable.types[settings.type].reset || $.editable.types.defaults.reset).apply(this, arguments);
				};

				new SimpleJEditable({
					targetUrl : cfMod.customFieldUrl,
					componentId : inputId,
					jeditableSettings : {
						onerror: onerror,
						onsubmit: function() { Forms.input(self.$("#" + inputId)).clearState(); }
					}
				});
			},

			configureOptionTable : function() {
				var self = this;
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}
				var config = $
						.extend(
								{
									"oLanguage" : {
										"sUrl" : cfMod.optionsTable.languageUrl
									},
									"bJQueryUI" : true,
									"bAutoWidth" : false,
									"bFilter" : false,
									"bPaginate" : true,
									"sPaginationType" : "squash",
									"iDisplayLength" : cfMod.optionsTable.displayLength,
									"bServerSide" : true,
									"sAjaxSource" : cfMod.optionsTable.ajaxSource,
									"bDeferRender" : true,
									"bRetrieve" : true,
									"sDom" : 't<"dataTables_footer"lp>',
									"iDeferLoading" : 0,
									"fnRowCallback" : function() {
									},
									"aoColumnDefs" : [
											{
												'bSortable' : false,
												'sWidth' : '2em',
												'sClass' : 'centered ui-state-default drag-handle select-handle',
												'aTargets' : [ 0 ],
												'mDataProp' : 'entity-index'
											},
											{
												'bSortable' : false,
												"aTargets" : [ 1 ],
												"sClass" : "opt-label linkWise",
												"mDataProp" : "opt-label"
											},
											{
												'bSortable' : false,
												"aTargets" : [ 2 ],
												"sClass" : "opt-code linkWise",
												"mDataProp" : "opt-code"
											},
											{
												'bSortable' : false,
												'aTargets' : [ 3 ],
												'sClass' : "is-default",
												'mDataProp' : 'opt-default'
											},
											{
												'bSortable' : false,
												'sWidth' : '2em',
												'sClass' : 'delete-button',
												'aTargets' : [ 4 ],
												'mDataProp' : 'empty-delete-holder'
											} ]
								}, squashtm.datatable.defaults);

				var squashSettings = {
					enableHover : true,
					enableDnD : true,
					confirmPopup : {
						oklabel : cfMod.confirmLabel,
						cancellabel : cfMod.cancelLabel
					},

					deleteButtons : {
						url : cfMod.optionsTable.ajaxSource	+ "/{opt-label}",
						popupmessage : "<div class='display-table-row'><div class='display-table-cell warning-cell'><div class='delete-node-dialog-warning'></div></div><div class='display-table-cell'>"+cfMod.optionsTable.deleteConfirmMessageFirst+"<span class='red-warning-message'>"+cfMod.optionsTable.deleteConfirmMessageSecond+"</span>"+cfMod.optionsTable.deleteConfirmMessageThird+"<span class='bold-warning-message'>"+cfMod.optionsTable.deleteConfirmMessageFourth+"</span></div></div>",
						tooltip : cfMod.optionsTable.deleteTooltip,
						success : function(data) {
							self.optionsTable.refresh();
						},
						dataType : "json"
					},

					functions : {
						dropHandler : function(dropData) {
							var url = cfMod.optionsTable.ajaxSource + '/positions';
							$.post(url,	dropData, function() {
								self.optionsTable.refresh();
							});
						},
						getODataId : function(arg) {
							return this.fnGetData(arg)['opt-label'];
						}
					}

				};

				this.optionsTable = this.$("table");
				this.optionsTable.squashTable(config,
						squashSettings);
			},

			openAddOptionPopup : function() {
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}
				var self = this;

				function discard() {
					self.newOptionDialog
							.off("newOption.cancel newOption.confirm");
					self.newOptionDialog.undelegateEvents();
					self.newOptionDialog = null;
				}

				function discardAndRefresh() {
					discard();
					self.optionsTable.refresh();
				}

				self.newOptionDialog = new NewCustomFieldOptionDialog(
						{
							model : {
								label : "",
								code : ""
							}
						});

				self.newOptionDialog
						.on("newOption.cancel", discard);
				self.newOptionDialog.on("newOption.confirm",
						discardAndRefresh);
			},

			configureRenameOptionPopup : function() {
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}
				var self = this;
				var params = {
					selector : "#rename-cuf-option-popup",
					title : cfMod.optionsTable.renameCufOptionTitle,
					openedBy : "#rename-cuf-option-popup",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons : [
							{
								'text' : cfMod.optionsTable.renameOptionLabel,
								'click' : function() {
									self.renameOption.call(self);
								}
							}, {
								'text' : cfMod.cancelLabel,
								'click' : this.closePopup
							} ]
				};
				squashtm.popup.create(params);
				this.renameCufOptionPopup = $("#rename-cuf-option-popup");
			},

			configureChangeOptionCodePopup : function() {
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}

				var self = this;
				var params = {
					selector : "#change-cuf-option-code-popup",
					title : cfMod.optionsTable.changeOptionCodeTitle,
					openedBy : "#change-cuf-option-code-popup",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons : [
							{
								'text' : cfMod.optionsTable.changeOptionCodeLabel,
								'click' : function() {
									self.changeOptionCode
											.call(self);
								}
							}, {
								'text' : cfMod.cancelLabel,
								'click' : this.closePopup
							} ]
				};
				squashtm.popup.create(params);
				this.changeOptionCodePopup = $("#change-cuf-option-code-popup");
			}

	});
	return CustomFieldModificationView;
});