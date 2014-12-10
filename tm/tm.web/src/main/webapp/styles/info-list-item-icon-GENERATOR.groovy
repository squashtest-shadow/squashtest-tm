import java.util.List;

import groovy.io.FileType
// get path of the script
def scriptPath = getClass().protectionDomain.codeSource.location.path
// get parent path
def scriptDir = scriptPath.substring(0, scriptPath.lastIndexOf("/"))
// get parent path again
def webappDir = scriptDir.substring(0, scriptDir.lastIndexOf("/"))
// add to the path the icon path of info-list
def iconDir= webappDir + "/images/info-list"


def list = []
def dir = new File(iconDir)
// get all .png file in the directory
dir.eachFileRecurse (FileType.FILES) { file ->
	if(file.name.endsWith('.png')) {
		list << file
	}
}

//name of the generated css
def fileName = scriptDir + '/info-list-item-icon.css'

//CSS file
File f = new File(fileName)
boolean isNew = f.createNewFile()
if(!isNew){
	f.delete()
	println("delete existing cssfile")
}
println("create new css file : "+fileName)
f = new File(fileName)
f.append ("/** \n * smartsprites directive :\n */\n /** sprite: sprites-icons; sprite-image: url('../images/sprites-icons.png'); sprite-layout: vertical */\n\n")


def printIcon(name, File f){
	f.append('.info-list-icon-')
	f.append(name.lastIndexOf('.').with {it != -1 ? name[0..<it] : name})
	f.append(' {\n')
	f.append('background-image :url(../images/info-list/')
	f.append(name)
	f.append('); /** sprite-ref: sprites-icons; */\n')
	f.append('background-repeat : no-repeat;\n')
	f.append('}\n')
}

list.each {
	printIcon(it.getName(), f)
}


// get main dir
def mainDir =  webappDir.substring(0,  webappDir.lastIndexOf("/"))
def javaDir =  mainDir + "/java/org/squashtest/tm/web/internal/util/"
def javaFileName = javaDir + "InfoListItemList.java"

File javaFile = new File(javaFileName)
boolean isJavaFileNew = javaFile.createNewFile()
if(!isJavaFileNew){
	javaFile.delete()
	println("delete existing java file")
}
println("create new java file : "+ javaFileName)
javaFile = new File(javaFileName)

javaFile.append('package org.squashtest.tm.web.internal.util;\n import java.util.Arrays;\n import java.util.List;\n\n public class InfoListItemList {\n');
javaFile.append('private static List<String> infoListItems = Arrays.asList(');
list.each {
	def name = it.getName()
	javaFile.append('"')
	javaFile.append('info-list-icon-')
	javaFile.append(name.lastIndexOf('.').with {it != -1 ? name[0..<it] : name})
	javaFile.append('"')
	if(it != list.last()) {
	javaFile.append(',')
 }
	
}	
javaFile.append(');\n')
javaFile.append('public static List<String> getInfoListItems() {\n')	
javaFile.append('return infoListItems;\n')
javaFile.append('}\n')
javaFile.append('}')




	
	


