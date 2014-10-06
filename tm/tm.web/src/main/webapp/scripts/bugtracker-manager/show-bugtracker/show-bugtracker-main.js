define([ "jquery" ], function($) {

	function initConf(conf) {
		var bugtrackerId = conf.bugtracker.id;
		initPopup();
		initButton(bugtrackerId);
	}

	function initButton(bugtrackerId) {
		$("#delete-bugtracker-button").on('click', function() {

			var popup = $("#delete-bugtracker-popup");
			popup.data('entity-id', bugtrackerId);
			popup.confirmDialog('open');

		});
	}

	function initPopup(squashtmContext) {
		$("#delete-bugtracker-popup").confirmDialog().on('confirmdialogconfirm', function() {

			var $this = $(this);
			var id = $this.data('entity-id');
			var url = squashtm.app.contextRoot + '/bugtracker/' + id;

			$.ajax({
				url : url,
				type : 'delete'
			});
			
			document.location.href = squashtm.app.contextRoot + '/administration/bugtrackers'

		});
	}

	return {
		initConf : initConf
	};

});
