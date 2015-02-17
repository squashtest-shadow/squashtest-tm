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


/**
 * 
 * The structure of the table should be the following (copy/paste if you like). Pay attention
 * to the css classes 'bind-milestone-dialog-X" 
 * 
 * <div class="bind-milestone-dialog popup-dialog" >
 * 
 * 	<div>
 * 		<table class="bind-milestone-dialog-table">
 * 			<thead>
 * 				<th data-def="sClass=bind-milestone-dialog-check"></th>
 * 				// et autre headers 
 * 			</thead>
 * 			<tbody>
 * 
 * 			</tbody>
 * 		</table>
 * 
 * 		<span class="bind-milestone-dialog-selectall cursor-pointer"/>select all</span>
 * 		<span class="bind-milestone-dialog-selectnone cursor-pointer"/>select none</span>
 * 		<span class="bind-milestone-dialog-invertselect cursor-pointer"/>invert selection</span>
 * 	
 * 	</div>
 * 
 * 	<div class="popup-dialog-buttonpane">
 * 		<input type="button" class="bind-milestone-dialog-confirm" data-def="evt=confirm, mainbtn" value="confirm"/>
 * 		<input type="button" class="bind-milestone-dialog-cancel" data-def="evt=cancel" value="cancel"/>
 * 	</div>
 * 
 * </div>
 * 
 * 
 * 
 * available options : 
 * 
 * {
 *	multilines : boolean, whether the table allows for multilines selection
 *	tableSource : the URL from where the data should be fetch to 
 *  
 *  milestonesURL : which URL the bindings should be posted to 
 *  identity : the identity of the entity to which we add milestones
 *  
 * }
 * 
 * 
 * events : 
 * 	- node.bindmilestones : the dialog committed some new milestone belongings.
 * 							The event comes with a companion data : { identity : identity, milestones : [array, of, milestoneids] } 
 * 							
 * 
 * Notes : the datatable row model should provide at least the attribute 'entity-id' 
 * 
 * 
 */
define(["jquery", "workspace.event-bus", "jqueryui", "jquery.squash.formdialog", "squashtable"], 
		function($, eventBus){
	
	$.widget("squash.milestoneDialog", $.squash.formDialog, {
	
		options : {
			multilines : true
		},
		
		_create : function(){
			
			this._super();
			
			var self = this,
				element = $(this.element[0]);
			
			
			this.onOwnBtn('confirm', $.proxy(self.confirm, self));
			this.onOwnBtn('cancel', $.proxy(self.cancel, self));
			
			var table = element.find('.bind-milestone-dialog-table');
			
			element.on('click', '.bind-milestone-dialog-selectall', function(){
				table.find('>tbody>tr>td.bind-milestone-dialog-check input').prop('checked', true);
			});			
			
			element.on('click', '.bind-milestone-dialog-selectnone', function(){
				table.find('>tbody>tr>td.bind-milestone-dialog-check input').prop('checked', false);				
			});			
			
			element.on('click', '.bind-milestone-dialog-invertselect', function(){
				table.find('>tbody>tr>td.bind-milestone-dialog-check input').each(function(){
					this.checked = ! this.checked;					
				});				
			});
			
			
			
		},
		
		/*
		 * The table must init at first opening
		 */
		open : function(){
			this._super();
			
			var url = this.options.tableSource;
			var table = $(this.element[0]).find('.bind-milestone-dialog-table');
			
			// if initialized -> refresh
			if (!! table.data('squashtableInstance')){
				table.squashTable()._fnAjaxUpdate();
			}
			// else -> init
			else{
				this._configureTable();
			}
				
		},
		
		/*
		 * One major aspect of this table is that, if no initialData is provided,
		 * the table initialization must be defered to when the dialog opens.
		 * 
		 */
		_configureTable : function(){
			
			var table = $(this.element[0]).find('.bind-milestone-dialog-table');	
			
			table.on('click', '>tbody>tr', function(evt){
				
				// don't trigger if the clicked element is 
				// the checkbox itself 
				if (! $(evt.target).is('input')){
					var chk = $(evt.currentTarget).find('.bind-milestone-dialog-check input');								
					var newstate = ! chk.is(':checked');
					chk.prop('checked', newstate);
				}
			});
			
			
			var tblCnf = {
					sAjaxSource : this.options.tableSource, 
					bServerSide : false,
					fnDrawCallback : function(){
						table.find('>tbody>tr>td.bind-milestone-dialog-check').each(function(){
							$(this).html('<input type="checkbox"/>');
						});
						table.find('>tbody>tr').addClass('cursor-pointer');
					}
				},

				squashCnf = {
					
				};
			
			table.squashTable(tblCnf, squashCnf);
			
			table.squashTable().refresh();
		},
				
		confirm : function(){
			var self = this;
			var table = $(this.element[0]).find('.bind-milestone-dialog-table').squashTable();
			var checks = table.find('>tbody>tr>td.bind-milestone-dialog-check input:checked');
			var ids = [];
			
			checks.each(function(){
				var r = this.parentNode.parentNode;
				var id = table.fnGetData(r)['entity-id'];
				ids.push(id);
			});
			
			var url = this.options.milestonesURL + '/'+ ids.join(',');
			
			$.ajax({
				url : url,
				type : 'POST'
			})
			.success(function(){
				self.close();
				eventBus.trigger('node.bindmilestones', {
					identity : self.options.identity,
					milestones : ids
				});
			});
			
		},
		
		cancel : function(){
			this.close();
		}
		
	});
});