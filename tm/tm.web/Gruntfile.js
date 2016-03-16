/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
'use strict';

module.exports = function(grunt) {
	require('load-grunt-tasks')(grunt);
	require('time-grunt')(grunt);

	grunt
			.initConfig({

				src : {
					styles : "src/main/webapp/styles",
					scripts : "src/main/webapp/scripts",
					images : "src/main/webapp/images",
					templates : "src/main/webapp/WEB-INF",
				},

				out : {
					css : "target/generated-resources/styles",
					js : "target/generated-resources/scripts",
					sprites : "target/generated-resources/sprites",
					spritesImgWorkDir : "target/smartsprites/in/images",
					spritesCssWorkDir : "target/smartsprites/in/css",
					lessWorkDir : "target/less/in"
				},

				// watch triggers tasks when files are changed
				watch : {
					scripts : {
						files : [
						          'src/main/webapp/scripts/**/*.js',
						          '!src/main/webapp/scripts/ckeditor/**/*.js',
						          '!src/main/webapp/scripts/lib/**/*.js'
						          ],
						tasks : [ 'copy:js' ]
					},

					webinf : {
						files : [ '<%= src.templates %>/**/*.{html,jsp,tag}' ],
						tasks : [ 'copy:webinf' ]
					},
				},

				jshint : {
					options : {
						jshintrc : '.jshintrc'
					},
					all : [ 'Gruntfile.js', 'src/main/webapp/scripts/{,*/}*.js' ]
				},

				// Put files not handled in other tasks here
				copy : {
					// copies css for eclipse usage
					css : {
						files : [ {
							expand : true,
							dot : true,
							cwd : '<%= out.css %>',
							dest : 'styles',
							src : [ '**/*.*' ]
						} ]
					},
					// copies js for eclipse usage
					js : {
						files : [ {
							expand : true,
							dot : true,
							cwd : '<%= src.scripts %>',
							dest : 'scripts',
							src : [ '**/*.*' ]
						} ]
					},
					// copies html and such for eclipse usage
					webinf : {
						files : [ {
							expand : true,
							dot : true,
							cwd : '<%= src.templates %>',
							dest : 'WEB-INF',
							src : [ '**/*.{html,jsp,tag}' ]
						} ]
					},

					eclipse : {
						files : [ {
							expand : true,
							dot : true,
							cwd : '<%= src.images %>',
							dest : 'images',
							src : [ '**/*.*' ]
						}, {
							expand : true,
							dot : true,
							cwd : '<%= out.sprites %>/../images',
							dest : 'images',
							src : [ '**/*.*' ]
						}, {
							expand : true,
							cwd : '<%= out.css %>',
							dest : 'styles',
							src : [ '**/*.*' ]
						}, {
							expand : true,
							cwd : '<%= src.scripts %>',
							dest : 'scripts',
							src : [ '**/*.*' ]
						}, {
							expand : true,
							cwd : '<%= src.scripts %>',
							dest : '<%= out.js %>',
							src : [ '**/*.*' ]
						}, {
							expand : true,
							cwd : '<%= src.templates %>',
							dest : 'WEB-INF',
							src : [ '**/*.{html,jsp,tag}' ]
						} ]
					},

					dist : {
						files : [ {
							expand : true,
							dot : true,
							cwd : 'src/main/webapp',
							dest : '<%= yeoman.dist %>',
							src : [ '*.html', 'views/*.html', 'images/**/*.{png,gif,webp}', 'fonts/*' ]
						}, {
							expand : true,
							cwd : '.tmp/images',
							dest : '<%= yeoman.dist %>/images',
							src : [ 'generated/*' ]
						} ]
					},

					styles : {
						expand : true,
						cwd : 'src/main/webapp/styles',
						dest : '.tmp/styles/',
						src : '{,*/}*.css'
					},
				},

			});

	grunt.registerTask('sprites', 'Builds spritemap and processes css accordingly', [ 'copy:sprites', 'smartsprites', 'copy:less' ]);

	grunt.registerTask('test', []);

	grunt.registerTask('build', 'Processes assets for distribution packaging', [ 'clean:dist', 'sprites', 'less:dist', 'requirejs:dist' ]);

	grunt.registerTask('eclipse', 'Minimal asset processing, copies files for eclipse', [ 'clean:dist', 'less:dev', 'copy:eclipse' ]);

	grunt.registerTask('noassets', "Only copies files for eclipse. Dont whine if shings break.", [ 'copy:eclipse' ]);

	grunt.registerTask('default', [ /* 'test', */'build' ]);
};
