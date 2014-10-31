require(["common"], function() {
	require(["jquery", "squash.translator", "workspace.routing","squash.configmanager","app/ws/squashtm.notification", "jeditable.datepicker",  "squashtable", 
	         "app/ws/squashtm.workspace", 
	         "jquery.squash.formdialog", "jquery.squash.confirmdialog"], 
			function($, translator, routing, confman, notification){					
		


	   function getPostDate(localizedDate){
		try{
		var postDateFormat = $.datepicker.ATOM;   
		var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
		var postDate = $.datepicker.formatDate(postDateFormat, date);
		return postDate;
		} catch(err){ return null;}
		}

		
		$(function() {					
			$("#milestones-table").squashTable({},{});			
			$('#new-milestone-button').button();		
		});	
		
	
		var dateSettings = confman.getStdDatepicker(); 
		$("#add-milestone-end-date").editable(function(value){
			$("#add-milestone-end-date").text(value);
	    }, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});
		
		this.$textAreas = $("textarea");
		function decorateArea() {
			$(this).ckeditor(function() {
			}, {
				customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js",
				language : squashtm.app.ckeditorLanguage
			});
		}

		this.$textAreas.each(decorateArea);
		

		$("#delete-milestone-popup").confirmDialog().on('confirmdialogconfirm', function(){
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = squashtm.app.contextRoot+'/administration/milestones/'+ ids.join(",");
			var table = $("#milestones-table").squashTable();
			
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table.refresh();
			});
			
			
		});

		$("#delete-milestone-button").on('click', function(){
			var ids = $("#milestones-table").squashTable().getSelectedIds();

			if (ids.length>0){
				var popup = $("#delete-milestone-popup");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
			}
			else{
				displayNothingSelected();
			}
		});
		
		function displayNothingSelected(){
			var warn = translator.get({
				errorTitle : 'popup.title.Info',
				errorMessage : 'message.EmptyTableSelection'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}
		
		
		
		
	var addMilestoneDialog = $("#add-milestone-dialog");
		
	addMilestoneDialog.formDialog();
		
	
	
	
	addMilestoneDialog.on('formdialogconfirm', function(){
		var url = routing.buildURL('administration.milestones');
		var params = {
			label: $( '#add-milestone-label' ).val(),
			status: $( '#add-milestone-status' ).val(),
			endDate: getPostDate($( '#add-milestone-end-date' ).text()),
			description: $( '#add-milestone-description' ).val()
		};
		$.ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			data : params				
		}).success(function(){
			$('#milestones-table').squashTable().refresh();
			addMilestoneDialog.formDialog('close');
		});
	
	});
	
	addMilestoneDialog.on('formdialogcancel', function(){
		addMilestoneDialog.formDialog('close');
		});
		
	$('#new-milestone-button').on('click', function(){
		addMilestoneDialog.formDialog('open');
	});
	
	});			
});		