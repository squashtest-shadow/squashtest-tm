/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
println('hello groovy')

fileName = 'src/main/webapp/styles/testCaseTreeIcons.css'
statuses = ["approved", "obsolete", "to_be_updated", "under_review", "work_in_progress"]
importances = ["high", "low", "medium", "very_high"]
requirement = ["true", "false"]

File f = new File(fileName)
boolean isNew = f.createNewFile()
if(!isNew){
    f.delete()
    println("delete existing file")
}
println("create new file : "+fileName)
f = new File(fileName)
f.append ("/** \n * smartsprites directive :\n */\n /** sprite: sprites-icons; sprite-image: url('../images/sprites-icons.png'); sprite-layout: vertical */\n\n")
for(status in statuses){
    for(importance in importances){
        for (req in requirement){
            println('process combination : '+status+' '+importance+' '+req)
            iconName = ""+status+importance+req+".png"            
            f.append('li[rel="test-case"][status="'+status+'"][importance="'+importance+'"][isreqcovered="'+req+'"] > a > .jstree-icon {\n')
            f.append('background-image: url("../images/tc_icons/'+iconName+'");  /** sprite-ref: sprites-icons; */\n')
			f.append('width: 42px;\n')
			f.append('height: 14px;\n')
            f.append('}\n')
        }
    }
}
