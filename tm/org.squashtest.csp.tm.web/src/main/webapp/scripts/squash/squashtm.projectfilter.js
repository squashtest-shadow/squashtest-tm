var squashtm = squashtm || {};

squashtm.projectfilter = (function($) {
	var popupSelector = '#project-filter-popup';
	var projectFilterUrl;

	/** initializes the project filter popup */
	function selectAllProjects() {
		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");

		if (boxes.length == 0)
			return;

		$(boxes).each(function() {
			setCheckBox($(this), true);
		});
	}

	function deselectAllProjects() {
		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");

		if (boxes.length == 0)
			return;

		$(boxes).each(function() {
			setCheckBox($(this), false);
		});
	}

	function invertAllProjects() {
		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");

		if (boxes.length == 0)
			return;

		$(boxes).each(function() {
			setCheckBox($(this), !$(this).is(":checked"));
		});
	}

	function loadFilterProject() {
		clearFilterProject();

		$.get(projectFilterUrl, populateFilterProject, "json");
	}

	/**
	 * Code managing the loading phase of the popup. It expects the server to
	 * send the data as a json object, see
	 * tm.web.internal.model.jquery.FilterModel
	 * 
	 * note : each project in the array is an array made of the following : {
	 * Long , String , Boolean )
	 */
	function clearFilterProject() {
		$("#dialog-settings-filter-projectlist").empty();
	}

	function populateFilterProject(jsonData) {
		var cssClass = "odd";
		var i = 0;
		for (i = 0; i < jsonData.projectData.length; i++) {
			appendProjectItem("dialog-settings-filter-projectlist",
					jsonData.projectData[i], cssClass);
			cssClass = swapCssClass(cssClass);
		}

	}

	function swapCssClass(cssClass) {
		if (cssClass == "odd")
			return "even";
		return "odd";
	}

	function setCheckBox(jqCheckbox, isEnabled) {
		if (isEnabled) {
			jqCheckbox.attr('checked', 'checked');
		} else {
			jqCheckbox.removeAttr('checked');
		}
	}

	function appendProjectItem(containerId, projectItemData, cssClass) {
		var jqNewItem = $(
				popupSelector + " .project-item-template .project-item")
				.clone();
		jqNewItem.addClass(cssClass);

		var jqChkBx = jqNewItem.find(".project-checkbox");
		jqChkBx.attr('id', 'project-checkbox-' + parseInt(projectItemData[0]));

		var jqName = jqNewItem.find(".project-name");
		jqName.html(projectItemData[1]);

		setCheckBox(jqChkBx, projectItemData[2]);

		$("#" + containerId).append(jqNewItem);
	}

	/**
	 * code managing the data transmissions
	 */
	function sendNewFilter() {
		var isEnabled = $("#dialog-settings-isselected-checkbox")
				.is(":checked");

		var ids = getSelectedProjectIds("dialog-settings-filter-projectlist");
		$.post(projectFilterUrl, {
			projectIds : ids
		}, newFilterSuccess);

	}

	function getSelectedProjectIds(containerId) {
		var selectedBoxes = $("#" + containerId + " .project-checkbox:checked");
		var zeids = new Array();
		var i;

		for (i = 0; i < selectedBoxes.length; i++) {
			var jqBox = $(selectedBoxes[i]);

			zeids.push(extractId(jqBox.attr('id')));
		}

		return zeids;
	}

	function extractId(strDomId) {
		var idTemplate = "project-checkbox-";
		var templateLength = idTemplate.length;
		var extractedId = strDomId.substring(templateLength);
		return extractedId;
	}

	function newFilterSuccess() {
		$(popupSelector).dialog('close');
		window.location.reload();
	}

	function initPopup(conf) {
		projectFilterUrl = conf.url;

		var params = {
			selector : popupSelector,
			title : conf.title,
			openedBy : '#menu-project-filter-link',
			closeOnSuccess : false,
			buttons : [ {
				text : conf.confirmLabel,
				click : sendNewFilter
			}, {
				text : conf.cancelLabel,
				click : function() {
					$(this).dialog('close');
				}
			} ],
			width : 400,
			open : loadFilterProject
		}

		squashtm.popup.create(params);

		$("#dialog-settings-filter-selectall").click(selectAllProjects);
		$("#dialog-settings-filter-deselectall").click(deselectAllProjects);
		$("#dialog-settings-filter-invertselect").click(invertAllProjects);
	}

	/**
	 * public module
	 */
	return {
		init : initPopup
	}
}(jQuery));
