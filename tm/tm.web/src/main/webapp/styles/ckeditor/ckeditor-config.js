CKEDITOR.editorConfig = function( config ){
	config.skin = 'squashtest,../../styles/ckeditor/skin/';
	config.toolbar = 'Squash';
	config.toolbar_Squash =  
		[   
		['Bold','Italic','Underline','NumberedList','BulletedList'], 
		['Link'],  
		['JustifyLeft','JustifyCenter','JustifyRight'],  
		['TextColor'],['Font'],['FontSize'],
		['Scayt'],
		['Table', 'Image'],
		];  
	config.height = '10em';
	config.resize_minHeight = 	175;
	config.resize_minWidth = 200;
	config.removePlugins = 'elementspath';
	config.extraPlugins='onchange'; 
}
