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
define(["jquery.squash.bindviewformdialog","./NewTemplateFromProjectDialogModel","squash.translator"],
		function(BindViewFormDialog, NewTemplateFromProjectDialogModel, translator) {

	var View = BindViewFormDialog.extend({
		el : "#add-template-from-project-dialog-tpl",
		popupSelector : "#add-template-from-project-dialog",

		initialize : function(){
			this.activateCheckBox();
			this.initializeFields();
			this.templateWithProjectName();
			this.templateWithProjectDescAndLabel();
		},

		//overriding callConfirm method of BindViewFormDialog to have redirection after the save success
		callConfirm : function(){
			this.updateModelFromCKEditor();
			this.model.save().success(function(response, status, options){
				document.location.href = response.Location[0];
			});
		},

		onConfirmSuccessAndResetDialog : function(){
			this.trigger("newtemplate.confirm");
			BindViewFormDialog.prototype.onConfirmSuccessAndResetDialog.call(this);
		},

		inactivateCheckBox : function(){
			this.$el.find("input:checkbox").prop("disabled",true);
			this.$el.find("input:checkbox").prop("checked",true);
		},

		activateCheckBox : function(){
			this.$el.find("input:checkbox").prop("disabled",false);
			this.$el.find("input:checkbox").prop("checked",true);
		},

		//used to store original name, desc and label.
		// As these elements are purely reusable view elements, they shouldn't be saved in clearable model
		initializeFields : function (originalProjectName, originalProjectDesc, originalProjectLabel) {
			if (this.originalProjectName===undefined) {
				this.originalProjectName = this.model.get("originalProjectName");
				this.model.unset("originalProjectName");//unsetting this value in model, so save() will not send it in request
			}
			if (this.originalProjectDesc===undefined) {
				this.originalProjectDesc = this.concatWithTemplatePrefix(this.model.get("description"));
			}
			if (this.originalProjectLabel===undefined) {
				this.originalProjectLabel = this.concatWithTemplatePrefix(this.model.get("label"));
			}
			//now setting model because it could be cleared with addAnother
			this.model.set("description",this.originalProjectDesc);
			this.model.set("label",this.originalProjectLabel);
		},

		templateWithProjectName : function () {
			var sentence = translator.get("dialog.message.templateFromProject");
			sentence = sentence + " " + this.originalProjectName;
			this.$el.find("#templateFromProjectMessage").html(sentence);
		},

		templateWithProjectDescAndLabel : function () {
			this.$el.find("#add-template-from-project-description").val(this.model.get("description"));
			this.$el.find("#add-template-from-project-label").val(this.model.get("label"));
		},

		concatWithTemplatePrefix : function (message) {
			if (this.prefix===undefined) {
				this.prefix=translator.get("dialog.templateFromProject.prefix");
			}
			return this.prefix + " " + message;
		}

	});

	return View;
});
