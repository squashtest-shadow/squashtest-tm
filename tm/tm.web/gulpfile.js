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
var sprite = require('gulp-sprite-generator');
var del = require('del');
var merge = require('merge-stream');
var buffer = require('vinyl-buffer')

var source = './src/main/webapp'; // source directory of web assets in tm.web project
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
var wro4jDestination =  destination + '/wro4j-spring-boot'
var styleDestination = wro4jDestination + '/styles'
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
    .pipe(gulp.dest(styleDestination))
});

function makeLessFile(src, targetName){
	var source = concatFileNameAndPaths(src,styleSource);
    console.log('Make Less File');
    console.log(source);
    console.log(styleDestination);
	return gulp.src(source)
    	.pipe(plumber())
      .pipe(less())
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

gulp.task('copyCssToProdDirectory', function () {
	return gulp.src(styleSource + '/**/*')
    	.pipe(plumber())
        .pipe(gulpCopy(styleProdDestination,{prefix: 4}))
});

gulp.task('minifyCss', function () {
	return gulp.src(styleDestination + '/*.css')
    	.pipe(plumber())
        .pipe(csso())
        .pipe(gulp.dest(styleProdDestination))
});



//###################################### /PROCESSING STYLES #########################################



//###################################### WATCHER FOR CSS ############################################
// Watch all the files in style, if change, copy all file to working directory and process them
gulp.task('watch', function() {
	var toWatch =  styleSource + '/*';
    gulp.watch(toWatch, function () {
        runSequence('copyCssToProdDirectory','css');
    });  
});
//###################################### /WATCHER FOR CSS ###########################################


//###################################### PROCESSING ICON IMAGES #####################################

var sourceImages = source + '/images/**/*.png';
var destinationImage = wro4jDestination;
//only spriting the images in /images. We don't want to proccess de Jquery image or worst.. the ugly ckeditor
var sourceImageToBeSprited = [wro4jDestination + '/images/**/*.png',wro4jDestination + '/images/*.png'];
var sourceCssToBeSprited = styleDestination + '/*.css';

gulp.task('copyImagesToWro4j', function () {
	return gulp.src(sourceImages)
    	.pipe(plumber())
        .pipe(gulpCopy(wro4jDestination,{prefix: 3}))
});

gulp.task('copyImages', function () {
	return gulp.src(sourceImageToBeSprited)
    	.pipe(plumber())
        .pipe(gulpCopy(destination,{prefix: 2}))
});


function spriteCss(sourceCssSprited, prefix){
    var spriteOutput;
    var path = concatFileNameAndPaths(sourceCssSprited, styleDestination);
	spriteOutput = gulp.src(path)
		.pipe(sprite({
            spriteSheetName: 'sprite_'+ prefix +'.png',
            spriteSheetPath: '../images',
            filter: [
                function(image) {
                    return !image.meta.skip;
                }
            ],
		}));

    var imgStream = spriteOutput.img
    .pipe(plumber())
    .pipe(gulp.dest(wro4jDestination + '/images'));
 
    var cssStream = spriteOutput.css
    .pipe(plumber())
    .pipe(gulp.dest(styleDestination));
 
    return merge(imgStream, cssStream);
}

gulp.task('sprites',function(){
    runSequence('spritesBlue','spritesBlueGreen','spritesGreen','spritesGreenBlue','spritesGrey','spritesPurple','spritesWine','spritesCore','spritesPrint','spritesTree');
});
/*
gulp.task('sprites',function(){
     var spriteOutput;
	spriteOutput = gulp.src(sourceCssToBeSprited)
		.pipe(sprite({
            spriteSheetName: 'sprite.png',
            spriteSheetPath:'../images',
            accumulate :true,
            filter: [
                function(image) {
                    return !image.meta.skip;
                }
            ],
		}));

    var imgStream = spriteOutput.img
    .pipe(plumber())
    .pipe(gulp.dest(wro4jDestination + '/images'));
 
    var cssStream = spriteOutput.css
    .pipe(plumber())
    .pipe(gulp.dest(styleDestination));
 
    return merge(imgStream, cssStream);
    
});*/

gulp.task('spritesBlue', function () {
	return spriteCss('squash.blue.css','blue');
});

gulp.task('spritesBlueGreen', function () {
	return spriteCss('squash.blue-green.css','blue_green');
});

gulp.task('spritesGreen', function () {
	return spriteCss('squash.green.css','green');
});

gulp.task('spritesGreenBlue', function () {
	return spriteCss('squash.green-blue.css','green_blue');
});

gulp.task('spritesGrey', function () {
	return spriteCss('squash.grey.css','grey');
});

gulp.task('spritesPurple', function () {
	return spriteCss('squash.purple.css','purple');
});

gulp.task('spritesWine', function () {
	return spriteCss('squash.wine.css','wine');
});

gulp.task('spritesCore', function () {
	return spriteCss('squash.core.css','core');
});

gulp.task('spritesPrint', function () {
	return spriteCss('squash.print.css','print');
});

gulp.task('spritesTree', function () {
	return spriteCss('squash.tree.css','tree');
});


//###################################### /PROCESSING ICON IMAGES ####################################



//###################################### MAIN BUILD TASK ############################################
//By default perform a full prod build, use dev task to perform a dev build.
gulp.task('default', function () {
    runSequence('copyCssToProdDirectory','css','copyImagesToWro4j','sprites','copyImages','minifyCss');
});

//###################################### /MAIN BUILD TASK ###########################################
