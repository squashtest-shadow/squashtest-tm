#
#     This file is part of the Squashtest platform.
#     Copyright (C) 2010 - 2014 Henix, henix.fr
#
#     See the NOTICE file distributed with this work for additional
#     information regarding copyright ownership.
#
#     This is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     this software is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with this software.  If not, see <http://www.gnu.org/licenses/>.
#

# This script performs a "private build". It should be run before any push to the upstream
# It creates a clone of the current repository in ../.privatebuild and runs a maven build against the repo's tip. 
# If this build fails, then the repository is not in a state suitable for a push.

#!/bin/bash

REPO_DIR=`pwd | sed  "s/\(.*\)\/\(.*\)$/\2/"`
#  REPO_DIR=${PWD##*/} should also work

if [ ! -d "../privatebuild" ]; then
  mkdir ../privatebuild
fi

if [ ! -d "../privatebuild/$REPO_DIR" ]; then
  hg clone . ../privatebuild/$REPO_DIR
fi

cd ../privatebuild/$REPO_DIR

hg pull
hg up -C -r tip

if [ -e "pom.xml" ]; then
  mvn -q clean install -Pprivate-build 

  if [ $? -eq 0 ]; then
    echo "Build OK - You can push to upstream"
  else
    echo "Build failed - Fix before push"
    exit $?
  fi
fi


