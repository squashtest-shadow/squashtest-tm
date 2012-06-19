#! /bin/sh
#
#     This file is part of the Squashtest platform.
#     Copyright (C) 2010 - 2012 Henix, henix.fr
#
#     See the NOTICE file distributed with this work for additional
#     information regarding copyright ownership.
#
#     This is free software: you can redistribute it and/or modify
#     it under the terms of the GNU Lesser General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     this software is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU Lesser General Public License for more details.
#
#     You should have received a copy of the GNU Lesser General Public License
#     along with this software.  If not, see <http://www.gnu.org/licenses/>.
#


#That script will :
#- check that the java environnement exists,
#- the version is adequate,
#- will run the application


## do not configure a third digit here
REQUIRED_VERSION=1.6


#tests if java exists

echo -n "$0 : checking java environment...";

java_exists=`java -version 2>&1`;

if [ $? -eq 127 ]
then
        echo;
        echo "$0 : Error : java not found. Please ensure that \$JAVA_HOME points to the correct directory.";
        echo "If \$JAVA_HOME is correctly set, try exporting that variable and run that script again. Eg : ";
        echo "\$ export \$JAVA_HOME";
        echo "\$ ./$0";
        exit -1;
fi

echo "done";

#tests if the version is high enough

echo -n "checking version...";

NUMERIC_REQUIRED_VERSION=`echo $REQUIRED_VERSION |sed 's/\./0/g'`;
java_version=`echo $java_exists | grep version |cut -d " " -f 3  |sed 's/\"//g' | cut -d "." -f 1,2 | sed 's/\./0/g'`;

if [ $java_version -lt $NUMERIC_REQUIRED_VERSION ]
then
        echo;
        echo "$0 : Error : your JRE does not meet the requirements. Please install a new JRE, required version ${REQUIRED_VERSION}.";
        exit -2;
fi

echo  "done";

#let's go !

echo "$0 : starting Felix...";

exec java -Dfelix.config.properties=file:../conf/felix.config.properties -Dfelix.system.properties=file:../conf/felix.system.properties -jar org.apache.felix.main-3.2.1.jar
