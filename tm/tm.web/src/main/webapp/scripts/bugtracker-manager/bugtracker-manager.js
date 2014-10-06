require(["common"], function() {
	require(["jquery", "squash.translator", "squashtable"], function($, translator){					
		$(function() {		
			$('#new-bugtracker-button').button();				
			$("#bugtrackers-table").squashTable({},{});						
		});	
		

		$("#delete-bugtracker-popup").confirmDialog().on('confirmdialogconfirm', function(){
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = squashtm.app.contextRoot+'/bugtracker/'+ ids.join(",");
			var table = $("#bugtrackers-table").squashTable();
			
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table.refresh();
			});
			
			
		});
		
		function displayNothingSelected(){
			var warn = translator.get({
				errorTitle : 'popup.title.Info',
				errorMessage : 'message.EmptyTableSelection'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}

		
		$("#delete-bugtracker-button").on('click', function(){
			var ids = $("#bugtrackers-table").squashTable().getSelectedIds();

			if (ids.length>0){
				var popup = $("#delete-bugtracker-popup");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
			}
			else{

				displayNothingSelected();
			}
		});
	
	});			
});		