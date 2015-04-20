/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define(
		[ "jquery", "backbone", "underscore", "../squashtable/squashtable.options", "jquery.squash.oneshotdialog", "squash.translator", "app/ws/squashtm.notification"],
		function($, Backbone, _, SquashTable, oneshot, messages, notif) {
			"use strict";

			messages.load([
				"message.infoList.remove.first",
				"message.infoList.remove.second",
				"message.infoList.remove.third",
				"message.infoList.remove.fourth",
				"message.infoList.bound.remove.first",
				"message.infoList.bound.remove.second",
				"message.infoList.bound.remove.third",
				"message.infoList.bound.remove.fourth",
				"message.infoList.batchRemove.first",
				"message.infoList.batchRemove.second",
				"message.infoList.batchRemove.third",
				"message.infoList.batchRemove.fourth",
				"message.infoList.batchRemove.first",
				"message.infoList.bound.batchRemove.second",
				"message.infoList.bound.batchRemove.third",
				"message.infoList.bound.batchRemove.fourth",
				"message.noLinesSelected",
				"dialog.info-list.warning.reindex.before",
				"dialog.info-list.warning.reindex.after"
			]);

			var itemsTableConf = window.squashtm.app.itemsTable;
			console.log("table conf" ,itemsTableConf);
			
			var gotoIndexButton = {
					'text' : messages.get("label.GotoIndex"),
					'click' : function() {
						var jqDialog = $(this);
						jqDialog.dialog('close');
						jqDialog.dialog('destroy');
						this.remove();
						document.location.href=  squashtm.app.contextRoot + "/administration/indexes";
					}
				};
			
			var closeButton = {
					'text' : messages.get("label.Close"),
					'click' : function() {
						var jqDialog = $(this);
						jqDialog.dialog('close');
						jqDialog.dialog('destroy');
						this.remove();
					}
				};
			
			var isAdmin = window.squashtm.app.isAdmin;
			
			function selectTr($tr) {
				$tr.removeClass("ui-state-highlight").addClass("ui-state-row-selected");
			}

			function itemIdMapper(data) {
				return data.id;
			}

			function removeTemplate() {
				removeTemplate.tpl = removeTemplate.tpl || Handlebars.compile($("#confirm-remove-tpl").html());
				return removeTemplate.tpl;
			}
			
			function reindexTemplate(){
				reindexTemplate.tpl = reindexTemplate.tpl || Handlebars.compile($("#confirm-remove-reindex").html())({ 'warn-index': messages.get("dialog.info-list.warning.reindex.after")});
				return reindexTemplate.tpl;
				
			}
			
			function popupReindex(){
                var indxTpl = reindexTemplate();
				var buttonConf = [ ];
				if (isAdmin){
				buttonConf.push(gotoIndexButton);
				}
				buttonConf.push(closeButton);
				var conf =  {'buttons' : buttonConf};
				oneshot.show(messages.get("label.Delete"), indxTpl, conf);
			}

			function removeProps(batch) {
				var flavor = batch ? "batchRemove." : "remove.";

				return function(bound) {
					var binding = bound ? "bound." : "";
					var memo = bound ? {'warn-index':  messages.get("dialog.info-list.warning.reindex.before")} : {'warn-index': ""};

					return ["first", "second", "third", "fourth"].reduce(function(memo, item) {
						memo[item] = messages.get("message.infoList." + binding + flavor + item);
						return memo;
					}, memo);
				};
			}

			/*
			 * Defines the controller for the custom fields table.
			 */
			var View = Backbone.View.extend({
				el : "#items-table",
				initialize : function() {
					var self = this;

					_.bindAll(this, "refresh", "onInitTable", "removeSelectedItems");

					this.apiRoot = this.$el.data("api-url");
					this.viewUrl = this.$el.data("view-url");
					this.selectedIds = [];

					this.listenTo(squashtm.vent, "newinfolist:confirmed", function(event) {
						self.refresh();
					});

					this.$el.on("init.dt", function(event) { self.onInitTable(event); });

					var remove = SquashTable.renderer($("#remove-cell-tpl").html())(function(data, type, row) {
						return { value: row.id, name: "list-delete" };
					});

					var editLink = SquashTable.renderer($("#name-cell-tpl").html())(function(data, type, row) {
						return { url:  self.viewUrl + "/" + row.id, text: data };
					});

					var colDefs = SquashTable.colDefs()
						.hidden(0, "id")
						.index(1)
						.std({ targets: 2, data: "label", render: editLink })
						.std(3, "description")
						.std(4, "defaultLabel")
						.std(5, "code")
						.calendar(6, "createdOn")
						.std(7, "createdBy")
						.datetime(8, "lastModifiedOn")
						.std(9, "lastModifiedBy")
						.button({ targets: 10, render: remove })
						.hidden(11, "bound")
						.build();

					var tableConfig = {
							jQueryUI: true,
							searching: true,
							pagingType: "squash",
							pageLength: itemsTableConf.pageLength,
							dom: '<"dataTables_header"fr>t<"dataTables_footer"lp>',
							order: [ [ 2, "asc" ] ],
							columnDefs: colDefs,
							// we cannot init ajax with deferred fetch and client-side processing,
							// so ajax is configured later on "init.dt" event
//							ajax: this.viewUrl,
							deferLoading: this.$("tbody > tr").size()
					};

					this.$el.DataTable(tableConfig);
				},

				events: {
					"click button[name='list-delete']": "onClickListDelete",
					"click td.select-handle": "onClickSelectHandle",
					"draw.dt": "onDrawTable",
					"order.dt" : "fixorder",
					"search.dt" : "fixorder",
				},

				fixorder : function(){
					//fix order
					var self = this;
				    
					 $.each(self.$el.dataTable().fnGetNodes(), function(index, cell){
						 cell._DT_RowIndex= index;
						 cell.firstElementChild.innerHTML = index + 1;
					 });
				    
				    
				},
				onInitTable: function onInitTable(event) {
					this.$el.DataTable().ajax.url(this.viewUrl);
				},

				onDrawTable: function(event) {
					var self = this;

					var selector = function(idx, data, node) {
						return self.selectedIds.indexOf(data.id) > -1;
					};

					var $trs = self.$el.DataTable().rows(selector).nodes().to$();
					selectTr($trs);
				},

				/**
				 * refreshes this view / refetches table content. Context is bound to this object.
				 * @param event
				 */
				refresh: function() {
					this.$el.DataTable().ajax.reload();
				},

				onClickSelectHandle: function(event) {
					// had to copy from function buried inside squashtable-main.
					// this is sort of generic code, it should be factored out somewhere reachable
					var c = event.ctrlKey;
					var s = event.shiftKey;
					var $tr = $(event.currentTarget).closest("tr");
					var self = this;

					var toggleSelection = function($tr) {
						var $trs = self.$("tr");
						$trs.removeClass("ui-state-row-selected");
						self.selectedIds = [];
						addToSelection($tr);
					};

					var addToSelection = function($tr) {
						selectTr($tr);
						var row = self.$el.DataTable().row($tr);
						self.lastSelectedRow = row.index();
						self.selectedIds.push(row.data().id);
					};

					var growSelection = function($tr) {
						var range = computeSelectionRange($tr);
						self.$el.DataTable().rows(range).nodes().each(function(row) {
							addToSelection($(row));
						});
					};

					var computeSelectionRange = function($tr) {
						var base = self.lastSelectedRow || 0;
						var current = self.$el.DataTable().row($tr).index();

						var min = Math.min(base, current);
						min = Math.max(min, 0);

						var max = Math.max(base, current);
						max = Math.min(max, self.$("tr").length - 1);

						return _.range(min, max + 1);
					};

					if (!c && !s) {
						toggleSelection($tr);
					} else if (c & !s) {
						addToSelection($tr);
					} else if (!c & s) {
						growSelection($tr);
					} else {
						growSelection($tr);
					}
				},

				onClickListDelete: function onClickListDelete(event) {
					var self = this;
					var tgt =  event.currentTarget;
					var $tr = $(tgt).closest("tr");
					var isBound = this.$el.DataTable().row($tr).data().bound === true;
					var props = removeProps(false /* not batch */);
					var tpl = removeTemplate()(props(isBound));
					oneshot.show(messages.get("label.Delete"), tpl).done(function() {
						if (isBound){
						popupReindex();
						}						
						$.ajax(self.apiRoot + "/" + $(tgt).data("value"), { type: "DELETE" })
							.done(self.refresh);
					});
				},

				removeSelectedItems: function removeSelectedItems() {
					var self = this;
					var $sel = this.$(".ui-state-row-selected");
					var rows = this.$el.DataTable().rows($sel);

					if (rows.data().length === 0) {
						notif.showWarning(messages.get("message.noLinesSelected"));
						return;
					}

					var hasBound = _.some(rows.data(), function(data) { return data.bound === true; });
					var props = removeProps(true /* batch */);
					var tpl = removeTemplate()(props(hasBound));

					oneshot.show(messages.get("label.Delete"), tpl).done(function() {
						if (hasBound){
							popupReindex();
							}
						var ids = rows.data().map(itemIdMapper).join(",");
						$.ajax(self.apiRoot + "/" + ids, { type: "DELETE" })
							.done(self.refresh);
					});
				}
			});

			return View;
		});