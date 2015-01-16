
define(["module", "jquery", "app/pubsub", "squash.basicwidgets", "app/ws/squashtm.workspace", 
         "contextual-content-handlers", "workspace.event-bus", "jquery.squash.fragmenttabs", 
         "custom-field-values", "squash.configmanager", "app/ws/squashtm.notification", 
         "workspace.routing",  "squash.translator", "file-upload",
          "jquery.squash.confirmdialog", "jquery.squash.formdialog"], 	
 		function(module, $, pubsub, basicwidg, WS, contentHandlers, eventBus, Frag, 
 				cufvalues, confman, notification, routing, translator, upload){

		// event subscription
 		pubsub.subscribe('reload.requirement.toolbar', initToolbar);

 		pubsub.subscribe('reload.requirement.generalinfo', initGeneralinfos);
 		
 		pubsub.subscribe('reload.requirement.verifyingtestcases', initVerifyingtestcases);
 		
 		pubsub.subscribe('reload.requirement.audittrail', initAudittrail);
 		
 		pubsub.subscribe('reload.requirement.attachments', initAttachments);
 		
 		pubsub.subscribe('reload.requirement.popups', initPopups);
 		
 		pubsub.subscribe('reload.requirement.complete', initFinalize);

 		
 		// ********************** library *************************

 		function initToolbar(){
 			
 			// config 
 			var config = module.config();
 			
 			// name handler			
 			var nameHandler = contentHandlers.getNameAndReferenceHandler();
 			
 			nameHandler.identity = config.basic.identity;
 			nameHandler.nameDisplay = "#requirement-name";
 			nameHandler.nameHidden = "#requirement-raw-name";
 			nameHandler.referenceHidden = "#requirement-raw-reference";	
 			
 			// rename button			
 			if (config.permissions.writable){
 				$("#rename-requirement-button").on('click', function(){
 					$("#rename-requirement-dialog").formDialog('open');
 				});
 			}
 			
 			// new version button			
 			if (config.permissions.creatable){
 				$( "#new-version-button" ).on( "click", function() {
 					$( "#confirm-new-version-dialog" ).confirmDialog( "open" );
 				});
 			}
 			
 			// print button
 			$("#print-requirement-version-button").click(function(){
 				var url = routing.buildURL('requirementversions', config.basic.currentVersionId);
 				window.open(url+"?format=printable", "_blank");
 			});
 		}
 	
 		
 		function initGeneralinfos(){
 			// config 
 			var config = module.config(),
 				baseURL = config.urls.baseURL;
 			
 			if (config.permissions.writable){
 				
 				// ********** REFERENCE *****************

 				var refinput = $("#requirement-reference");
 				
 				var refconf = confman.getStdJeditable();
 				refconf.maxlength=50;
 				refconf.callback= function(reference){
 					eventBus.trigger('node.update-reference', { identity : config.basic.identity, newRef : reference});  
 				};
 				
 				refinput.editable(baseURL, refconf);
 				

 				// ********** CRITICALITY ***************
 				var criticSelect = $("#requirement-criticality");
 				var critconf = confman.getJeditableSelect();
 				critconf.data = config.basic.criticalities;
 				criticSelect.editable(baseURL, critconf).addClass('editable');
 				
 				
 				// ********* CATEGORIES *****************
 				var categSelect = $("#requirement-category");
 				var catconf = confman.getJeditableSelect();
 				catconf.data = confman.toJeditableSelectFormat(config.basic.categories.items, {'code' : 'friendlyLabel'});
 				
 				categSelect.editable(function(value, settings){ 						
 					var icon,
 						cats = config.basic.categories.items;
 					
 					// find the icon name
					for (var i=0;i<cats.length;i++){
						if (cats[i].code === value){
							icon = cats[i].iconName;
						}
					}
 					
					// trigger the event
 					$.post(baseURL, {id : 'requirement-category', value : value})
 					.done(function(response){
 						eventBus.trigger('node.attribute-changed', { 
 							identity : config.basic.identity,
 							attribute : 'category-icon', 
 							value : icon
 						});
 					});
     		      
 					//update icon
 					var requirement = $("#requirement-icon");
 					requirement.attr("class", "");	//reset
 				    requirement.addClass("small-icon info-list-icon-" + icon);
 					
 				    //in the mean time, must return immediately
 					return settings.data[value];
 					
 				}, catconf).addClass('editable');
 				
 				
 			}
 			
 			
 			// ********** STATUS CHANGE *************
 			if (config.permissions.status_editable){
 				
 				/* *********************************************************
 				 * 
 				 * STATUS CHANGE DROPWDOWN AND CONFIRM LOGIC
 				 * 
 				 * This combobox has an interplay with the 
 				 * status change dialog. Check what's happening 
 				 * in the function submitOrConfirm, 'OBSOLETE' branch.
 				 ********************************************************* */ 
 				
 				var statusChangeSelect = $("#requirement-status"),
 					statusSelectConf = confman.getJeditableSelect(),
 					statusChangeDialog = $("#requirement-status-confirm-dialog");
 				
 				var stsmessages = translator.get({
 					'cannot-set-status-allowed' 	: 'requirement.status.notAllowed.approved',
 					'cannot-set-status-defaultmsg'	: 'requirement.status.notAllowed.default'
 				});
 				
 				function submitOrConfirm(settings, widget){
 					var selected = this.find('select').val(),
 						cansubmit = true;
 					
 					// if disabled, tell the user and exit
 					if (selected.search(/disabled.*/)!=-1){
 						cansubmit = false;
 						var msg = ("disabled.APPROVED" === selected ) ? 
 									stsmessages['cannot-set-status-allowed'] : 
 									stsmessages['cannot-set-status-defaultmsg'];
 									
 						notification.showError(msg);
 						widget.reset();
 					}
 					
 					return cansubmit;
 				}
 				
 				
 				var finalStatusSelectConf = $.extend(true, statusSelectConf, 
 					{
 						loadurl : baseURL + '/next-status',
 						callback : function(){document.location.reload();},
 						onsubmit : submitOrConfirm
 					}	
 				);
 				
 				statusChangeSelect.editable(baseURL, finalStatusSelectConf)
 							.addClass('editable');
 				

 			}
 			
 			// ******** CUSTOM FIELDS *************************
 			
 			if (config.basic.hasCufs){
 				var cufurl =  routing.buildURL('customfield.values.get', config.basic.currentVersionId, 'REQUIREMENT_VERSION'),
 					mode = (config.permissions.writable) ? 'jeditable' : 'static';
 				$.getJSON(cufurl)
 				.success(function(jsonCufs){	
 					cufvalues.infoSupport.init("#requirement-attribut-table", jsonCufs, mode);
 				});
 			}
 			
 			
 			
 		}
 	
 		
 		function initVerifyingtestcases(){
 			var config = module.config();
			
			var table = $("#verifying-test-cases-table").squashTable({
				aaData : config.basic.verifyingTestcases
			}, {
				unbindButtons : {
					delegate : "#remove-verifying-test-case-dialog",
					tooltip : translator.get('dialog.unbind-ta-project.tooltip')
				}
			});
			
 			if (config.permissions.linkable){
 
 				
 				var removeDialog = $("#remove-verifying-test-case-dialog");
 			
 				$("#verifying-test-case-button").on('click', function(){
 					var url = routing.buildURL('requirements.testcases.manager', config.basic.currentVersionId);
 					document.location.href=url;	
 				});			
 				 				
 				 $( '#remove-verifying-test-case-button' ).click(function() {
 		            var ids = table.getSelectedIds();
 		            
 		            if (ids.length > 0) {
 		              removeDialog.confirmDialog('open');
 		            } else {
 		              notification.showError(translator.get('message.EmptyTableSelection'));
 		            }
 		         });
 				 
 				eventBus.onContextual('tc-req-links-updated', function(evt){
 					table.refresh();					
 				});
 					
 				
 			}
 		}
 		
 		function initAudittrail(){
 			
 			var config = module.config();
    			
 			// ************************** library ***********************************
 			
 			function auditTrailTableRowCallback(row, data, displayIndex) {
 				if (data['event-type'] == 'fat-prop') {
 					
 					var eventId = data['event-id'];
 					var proto = $( '#show-audit-event-details-template' ).clone();
 					var url = routing.buildURL('requirements.audittrail.change', eventId);
 					
 					proto.removeClass('not-displayed')
 						 .find( 'span' )
 						 .data('url', url)
 						 .attr( 'id', 'show-audit-event-detail:' + eventId )
 						 .click(function(){
 							 showPropChangeEventDetails(this);
 						 });
 					
 					$( 'td.event-message-cell', row ).append( proto ); 
 				}
 	
 				return row;
 			}
 			
 			function showPropChangeEventDetails(link) {
 				
 				$.getJSON( $(link).data('url'), function(data, textStatus, xhr) {
 					var dialog = $( "#audit-event-details-dialog" );
 					$( "#audit-event-old-value", dialog ).html(data.oldValue);
 					$( "#audit-event-new-value", dialog ).html(data.newValue);
 					dialog.messageDialog("open");
 				});
 			}
 			
 			// ********************* init ******************
 			
 			var conf = {
 				fnRowCallback : auditTrailTableRowCallback, 
 				aaData : config.basic.audittrail
 			};
 			
 			var table=$( "#requirement-audit-trail-table" ).squashTable(conf, {});
 	
 			$( "#audit-event-details-dialog" ).messageDialog();
 			
 			$(document).ajaxSuccess(function(event, xrh, settings) {
 				if (settings.type == 'POST' 
 						&& !(settings.data && settings.data.match(/requirement-status/g))
 						&& !settings.url.match(/versions\/new$/g)) {
 					//We refresh tble on POSTs which do not uptate requirement status or create a new version (these ones already refresh the whole page)
 					table.refresh();
 				}
 			});			
 		}
 	
 		function initAttachments(){
 			var config = module.config();
 			upload.initAttachmentsManager({
 				baseURL : config.urls.attachmentsURL,
 				aaData : config.basic.attachments
 			});			
 		}
 		
 		function initPopups(){
 			var config = module.config();
 			
 			// ******** version creation **********
 			
 			var confirmHandler = function() {
 				var url = routing.buildURL('requirements.versions.new', config.basic.requirementId);
 				$.ajax({
 					type : 'POST',
 					url : url
 				}).done( function() {
 					document.location.reload(true);
 				});
 			};
 			
 			var newversiondialog = $( "#confirm-new-version-dialog" );
 			newversiondialog.confirmDialog({confirm: confirmHandler});
 					
 			
 			// *********** status modification **********
 		
 			if (config.permissions.status_editable){
 			
 				var statusChangeDialog = $("#requirement-status-confirm-dialog");
 				statusChangeDialog.formDialog();
 				
 				statusChangeDialog.on('formdialogconfirm', function(){
 					statusChangeDialog.formDialog('close');
 					statusChangeDialog.data('confirmed', true);
 					var form = statusChangeDialog.data('form');
 					form.submit();
 				});
 				
 				statusChangeDialog.on('formdialogcancel', function(){
 					statusChangeDialog.formDialog('close');
 					statusChangeDialog.data('confirmed', false);
 					var form = statusChangeDialog.data('form');
 					form.submit();				
 				});
 			}
 			
 			
 	         if (config.permissions.writable){
 	        	 
 	        	// ****** unbind from multiple test cases ****************
 		          var removeDialog = $("#remove-verifying-test-case-dialog").confirmDialog();
 		          var verifurl = routing.buildURL('requirements.testcases', config.basic.currentVersionId) + '/';
 		          var table = $("#verifying-test-cases-table").squashTable();
 		          removeDialog.on('confirmdialogconfirm', function(){
 		            var ids = $("#verifying-test-cases-table").squashTable().getSelectedIds();
 		            $.ajax({
 		              url : verifurl + ids.join(','),
 		              type : 'DELETE',
 		              dataType : 'json'
 		            }).success(function() {
 		              table.refresh();
 		              eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
 		            });
 		          });
 		          
 		          
 				// ************ rename dialog ********
 				
 				var renameDialog = $("#rename-requirement-dialog");
 				renameDialog.formDialog();
 				
 				renameDialog.on('formdialogconfirm', function(){
 					var url = config.urls.baseURL,
 						params = { newName : $("#rename-requirement-input").val() };
 					
 					$.ajax({
 						url : url,
 						type : 'POST',
 						dataType : 'json',
 						data : params
 					}).success(function(json){
 						renameDialog.formDialog('close');
 						eventBus.trigger('node.rename', { identity : config.basic.identity, newName : json.newName});    
 					});
 					
 				});
 				
 				renameDialog.on('formdialogcancel', function(){
 					renameDialog.formDialog('close');
 				});
 				
 				renameDialog.on('formdialogopen', function(){
 					var name = $.trim($('#requirement-raw-name').text());
 					$("#rename-requirement-input").val(name);
 				});

 		          
 	         }
 	         
 				

 			
 		}
 		
 		function initFinalize(){
 			WS.init();
 			basicwidg.init();
 			Frag.init();
 		}
 	

 });
