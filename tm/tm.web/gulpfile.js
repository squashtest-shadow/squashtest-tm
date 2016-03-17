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
var gulp = require('gulp');
var path = require('path');
var less = require('gulp-less');
var concatCss = require('gulp-concat-css');
var csso = require('gulp-csso');
var environments = require('gulp-environments');
var plumber = require('gulp-plumber');
var rename = require("gulp-rename");
var runSequence = require('run-sequence');
var gulpCopy = require('gulp-copy');
var spritesmith = require('gulp.spritesmith');

var source = './src/main/webapp'; // work directory
// Maven target directory
// Please note that spring boot is configured to reload from src/webapp/style but it cannot process less sources...
var destination = './target';

function concatFilesNameAndPaths (files,path){
	return files.map(function(fileName){return path + '/' + fileName});
};

function concatFileNameAndPaths (file,destination){
	return (destination + '/' + file);
};


//###################################### ENV SETTINGS ##############################################

var development = environments.development;
var production = environments.production;

//env tasks
gulp.task('dev', function () {development.task()});
gulp.task('prod', function () {production.task()});

//###################################### /ENV SETTINGS #############################################

//###################################### PROCESSING STYLES #########################################

var styleSource = source + '/styles'
var styleDestination = destination + '/wro4j-spring-boot/styles'
var styleProdDestination = destination + '/styles'

gulp.task('css',['coreCss','themesCss','squashTree','squashCoreOveride','squashSubPageOveride','squashPrint']);
gulp.task('themesCss',['squashBlue','squashGreen','squashGrey','squashPurple','squashWine','squashBlueGreen','squashGreenBlue']);
//squash.core.css

var squashCoreSources = ['structure.css','ckeditor.override.css','bootstrap.override.css']
var squashCorePaths = concatFilesNameAndPaths(squashCoreSources,styleSource);
gulp.task('coreCss', function () {
  return gulp.src(squashCorePaths)
  	.pipe(plumber())
    .pipe(less())
    .pipe(concatCss("squash.core.css"))
    .pipe(production(csso("squash.core.css")))
    .pipe(gulp.dest(styleDestination))
});

function makeLessFile(src, targetName){
	var source = concatFileNameAndPaths(src,styleSource);
	return gulp.src(source)
    	.pipe(plumber())
      .pipe(less())
      .pipe(production(csso(targetName)))
      .pipe(rename(targetName))
      .pipe(gulp.dest(styleDestination))
};

var squashThemeSources = ['master.blue.less','master.green.css','master.grey.css','master.purple.css','master.wine.css','master.blue-green.css','master.green-blue.css']

gulp.task('squashBlue', function () {
  return makeLessFile('master.blue.less','squash.blue.css');
});

gulp.task('squashGreen', function () {
  return makeLessFile('master.green.less','squash.green.css');
});

gulp.task('squashGrey', function () {
  return makeLessFile('master.grey.less','squash.grey.css');
});

gulp.task('squashPurple', function () {
  return makeLessFile('master.purple.less','squash.purple.css');
});

gulp.task('squashWine', function () {
  return makeLessFile('master.wine.less','squash.wine.css');
});

gulp.task('squashBlueGreen', function () {
  return makeLessFile('master.blue-green.less','squash.blue-green.css');
});

gulp.task('squashGreenBlue', function () {
  return makeLessFile('master.green-blue.less','squash.green-blue.css');
});

gulp.task('squashTree', function () {
  return makeLessFile('squashtree.css','squash.tree.css');
});

gulp.task('squashCoreOveride', function () {
  return makeLessFile('structure.override.css','squash.core.override.css');
});

gulp.task('squashSubPageOveride', function () {
  return makeLessFile('structure.subpageoverride.css','squash.subpage.override.css');
});

var squashPrintSources = ['master.css','print.css'];
var squashPrintPaths = concatFilesNameAndPaths(squashPrintSources,styleSource);

gulp.task('squashPrint', function () {
	return makeLessFile('print.less','squash.print.css');
});

gulp.task('copyCss', function () {
	return gulp.src(styleDestination + '/*.css')
    	.pipe(plumber())
        .pipe(gulpCopy(styleProdDestination,{prefix: 3}))
});

//###################################### /PROCESSING STYLES #########################################



//###################################### WATCHER FOR CSS ############################################
gulp.task('watch', function() {
		var toWatch =  styleSource + '/*';
    gulp.watch(toWatch, ['css']);  // Watch all the .less files, then run the less task
});
//###################################### /WATCHER FOR CSS ###########################################


//###################################### PROCESSING ICON IMAGES #####################################
var sourceImage = [source + '/images/**/*.png',]
var destImage = destination + '/test/'

gulp.task('sprites', function () {
	return gulp.src(sourceImage)
    	.pipe(plumber())
        .pipe(spritesmith({
            imgName: 'sprite.png',
            cssName: 'sprite.css'
        }))
        .pipe(gulp.dest(destImage));
});

//###################################### /PROCESSING ICON IMAGES ####################################


//###################################### MAIN BUILD TASK ############################################
//By default perform a full prod build
gulp.task('default', function () {
    runSequence('prod', 'css', 'copyCss');
});

//###################################### /MAIN BUILD TASK ###########################################
