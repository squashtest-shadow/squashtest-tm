<?xml version="1.0" encoding="ISO-8859-1"?>
<!--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

-->

<xsl:stylesheet version="2.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:lb="http://www.liquibase.org/xml/ns/dbchangelog">

	<xsl:output method="xml" indent="yes"/>
	



	<xsl:template match="lb:comment">
		<!-- for some reason if you don't specify how to handle <comment> or else tags, saxon will print them
			by default. So we need to explain it that it should catch and ignore them unless we told him to.
		-->
	</xsl:template>



	
	<xsl:template match="lb:createTable/lb:column">
		<li>
			<label class="column-descr"><xsl:value-of select="@name" /> : </label>
			<p class="column-descr"><xsl:value-of select="@remarks" /></p>
		</li> 
	</xsl:template>
	

	<xsl:template match="lb:createTable">
		<h2><xsl:value-of select="@tableName"/></h2>
		<xsl:apply-templates select="lb:comment" />

		<label class="table-descr">description : </label>
		<p class="table-descr">
			<xsl:value-of select="preceding-sibling::lb:comment[1]"/>
		</p>
		
		<h3 class="table-column">Colonnes</h3>
		
		<ul>
			<xsl:apply-templates select="./lb:column">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
		</ul>

		<hr/>
		
	</xsl:template>
	


	<xsl:template match="/">

		<html>

		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		
		  <TITLE>dictionaire donnees Squash TM version ${project.version}</TITLE>
		  
		  <style type="text/css">
			label.table-descr {
				font-weight:bold;
			}
			
			p.table-descr {
				text-align:justify;
			}
			
			h3.table-column{
			}
			
			label.column-descr{
				font-weight:bold;
			}
			
			p.column-descr{
				text-align:justify;
			}
			
			
		  </style>

		</head>

		<body BGCOLOR="#FFFFFF">
		<h1>Squash Tm version ${project.version} : Dictionnaire de donnees</h1>		
		<!--<xsl:variable name="liquiFiles" select="collection('file:/D:/bsiri/helios_workspace/squashtest-csp/database/src/main/liquibase/tm/?select=*.xml')">			
			</xsl:variable>
		-->
 			<xsl:variable name="liquiFiles" select="collection('../../../../../database/src/main/liquibase/tm/?select=*.xml') | collection('../../../../../database/src/main/liquibase/core/?select=*.xml') ">			
			</xsl:variable>		
			<xsl:for-each select="$liquiFiles//lb:createTable">
				<xsl:sort select="@tableName" />
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		

		</body>

		</html>

	</xsl:template >



</xsl:stylesheet>