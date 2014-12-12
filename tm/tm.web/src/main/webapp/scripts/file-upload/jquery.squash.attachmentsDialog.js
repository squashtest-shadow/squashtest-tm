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
/*
 * This section handles the transition between phase 1 and phase 2, ie all the operations between 
 * the moment the user presses "submit" and the end of the download :
 *   - prelude to the upload : we'll need a 'ticket' for later use (see below),
 *   - close the uploader popup, open the progress bar,
 *   - start the uploading request (still stuck to HttpRequests, no XHR for this one)
 *   - during the upload, poll the server for the upload using the ticket obtained in step 1
 *   - once it's done it will open the uploadSummary popup 
 *
 *  
 * Notes : 
 *		- tickets are like sEcho for datatable : an identifier used by the server to know which upload to watch for.	 
 *		- we need the instruction flow to run as if the Ajax calls were synchronous. Since they aren't, we put the next steps of the 
 *		code in the ajax success handlers.
 */

define(
		[ "jquery", "squash.attributeparser", "handlebars", "jquery.squash.formdialog",
				"./jquery.squash.multi-fileupload" ],
		function($, attrparser, Handlebars) {

			if (($.squash !== undefined) && ($.squash.attachmentsDialog !== undefined)) {
				// plugin already loaded
				return;
			}

			$
					.widget(
							"squash.attachmentsDialog",
							$.squash.formDialog,
							{

								options : {
									width : 400,
									url : undefined
								},

								_create : function() {

									this._super();

									// main form init
									var template = this.element.find('.add-attachments-templates > .attachment-item');
									this.options._form = this.element.find('.attachment-upload-form').multiFileupload(
											template);

									// progressbar init
									this.options.bar = this.element.find('.attachment-progressbar').progressbar({
										value : 0
									});
									this.options.percent = this.element.find('.attachment-progress-percentage');

									// summary init
									var summaryItemTpl = '<div class="display-table-row" >'
											+ '<label class="display-table-cell" style="font-weight:bold;">{{name}}</label>'
											+ '<span class="display-table-cell">{{status}}</span>' + '</div>';
									this.options.summaryitem = Handlebars.compile(summaryItemTpl);

									// error init
									var errSpan = this.element.find('.attachment-upload-error-message');
									this.options._sizeexceeded = errSpan.text();
									errSpan.text('');

									this._bindEvents();
								},

								_bindEvents : function() {
									var self = this;

									this.onOwnBtn('cancel', function() {
										self.close();
									});

									this.onOwnBtn('done', function() {
										self.close();
									});

									this.onOwnBtn('submit', function() {
										self.submitAttachments();
									});
								},

								open : function() {
									this._super();
									this.options._form.clear();
									this.setState('selection');
								},

								close : function() {
									this._super();
									if (this.options._xhr) {
										this.options._xhr.abort();
									}
								},

								// ****************** files submission ***********************

								supportProgressBar : function supportAjaxUploadWithProgress() {
									return supportFileAPI() && supportAjaxUploadProgressEvents();

									function supportFileAPI() {
										var fi = document.createElement('INPUT');
										fi.type = 'file';
										return 'files' in fi;
									}

									function supportAjaxUploadProgressEvents() {
										var xhr = new XMLHttpRequest();
										return !!(xhr && ('upload' in xhr) && ('onprogress' in xhr.upload));
									}
								},

								submitAttachments : function() {
									var self = this;
									var url = this.options.url;

									$.post(url).done(function(ticket) {
										function onprogressHandler(evt) {
											var percent = evt.loaded / evt.total * 100;
											console.log('Upload progress: ' + percent.toFixed(0) + '%');
											self.refreshBar(percent);
										}
										self.options.ticket = ticket;
										self.setState('uploading');
										self.refreshBar(0);

										if (self.supportProgressBar()) {

											url = url + "?upload-ticket=" + ticket;
											var xhr = new XMLHttpRequest();
											self.options._xhr = xhr;
											xhr.upload.addEventListener('progress', onprogressHandler, false);
											xhr.addEventListener('readystatechange', function(e) {
												if (this.readyState === 4) {
													self.submitComplete();
												}
											});

											xhr.open('POST', url, true);

											var attach = $('input:file[name="attachment[]"]');
											var formData = new FormData();

											if (attach.length > 2) {
												for (i = 1; i < attach.length - 1; i++) {
													var file = attach[i].files[0];
													formData.append("attachment[]", file);
												}
											}
											
											// Catch if there's a JDBC Exception
												
											xhr.onreadystatechange=function() {
										    if (xhr.readyState === 4){   //if complete
										        if(xhr.status === 200){  //check if "OK" (200)
										            //success
										        } else {								        
										        	$(".attachment-progressbar").hide();
															$(".attachment-progress-percentage").hide();
															$(".attachment-progress-message").hide();
																							
										        }
										    } 
										}										
												xhr.send(formData);
										
										} else {
											// for browser that don't support xhr2, like IE9.

											self.options._form.ajaxSubmit({
												url : url + "?upload-ticket=" + ticket,
												dataType : "text/html",
												beforeSend : function(xhr) {
													self.options._xhr = xhr;
													$(".attachment-progressbar").hide();
													$(".attachment-progress-percentage").hide();

												},
												success : function() {
												},
												error : function() {
												},
												complete : function(json) {
													self.submitComplete(json);
												},
												target : "#dump"

											});
										}
									});
								},

								/*
								 * because some browsers find it clever to wrap the raw response inside html tags (no,
								 * it's not IE for once) we need to 'unwrap' our nested json response.
								 * 
								 * in our case, if the json response has an attribute maxSize, then we got an error.
								 */
								submitComplete : function() {
									var xhr = this.options._xhr;
									var text;

									try {
										text = $(xhr.responseText).text();
									} catch (e) {
										text = xhr.responseText;
									}

									// if text contains html, and HTML ERROR, display an error
									if (text.indexOf("HTTP ERROR") >= 0) {
										this.displayErrorJDBC(text);
									}
									
									
									var json = $.parseJSON(text);

									if (json.maxUploadError === undefined) {
										this.displaySummary();
									} else {
										this.displayError(json.maxUploadError.maxSize);
										
										
									}
								},

								// ********************* upload progress ******************

								refreshBar : function(percentage) {
									this.options.bar.progressbar('option', 'value', percentage);
									this.options.percent.text(percentage.toFixed(0).toString() + '%');

								},

								// ******************** upload summary ********************

								displaySummary : function() {
									var ticket = this.options.ticket, url = this.options.url, self = this;

									$.ajax({
										url : url + '?upload-ticket=' + ticket,
										type : 'DELETE',
										dataType : 'json'
									}).done(function(json) {
										if (json !== null && !self.allSuccessful(json)) {
											self.populateSummary(json);
											self.setState('summary');
										} else {
											self.close();
											self._trigger('done');
										}
									});

								},

								allSuccessful : function(json) {
									var summaries = json[0];
									var i = 0;
									for (i = 0; i < summaries.length; i++) {
										if (summaries[i].iStatus !== 0) {
											return false;
										}
									}
									return true;
								},

								populateSummary : function(json) {
									var summaries = json[0], i = 0, summarydiv = this.element
											.find('.attachment-upload-summary'), summaryItemTpl = this.options.summaryitem;

									summarydiv.empty();
									for (i = 0; i < summaries.length; i++) {
										var item = summaries[i];
										var line = summaryItemTpl(item);
										summarydiv.append(line);
									}

								},

								// ***************** errors ***********************

								displayError : function(size) {

									var s = (size / 1048576).toFixed(3);

									var errMessage = this.options._sizeexceeded.replace('#size#', s);
									this.element.find('.attachment-upload-error-message').text(errMessage);
									this.setState('error');
								},
								
								displayErrorJDBC : function(text) {

									var errMessage = text;
									this.element.find('.attachment-upload-error-message').text(errMessage);
									this.setState('error');
								}

							});

		});