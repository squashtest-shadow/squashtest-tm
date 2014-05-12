Squash TM database schema management
====================================

This module contains liquibase changesets which define database schema and upgrade.
**Changesets of released versions SHOULD NEVER BE MODIFIED** ! It would break debian / redhat packages incremental upgrade.

Creating a new database version
-------------------------------

When you need to create a DB upgrade for a new version, use the groovy script : 

    cd etc
    groovy createNewChangelogs.goovy -DnewVersion="1.2.3"

It should create a bunch of new changelog files and tell you which other files you need to manually modify.

Maven profiles
--------------

* default : performs a full import and data migrations against a _h2_ database.

* `acceptance-tests` : also performs a full import and data migration against a _mysql_ database. This profile requires the following properties to be set : 
	* `liquibase.mysql.url` : the jdbc url of the mysql database (`jdbc:mysql://host:3306/database`)
	* `liquibase.mysql.username` : a mysql user with DDL grants
	* `liquibase.mysql.password` : the username's password
	
* `mysql.update` : runs liquibase against a _mysql_ db. This is meant to be used as a CLI helper to provision a database :

    mvn package -Pmysql.update -Duser=foo -Dpwd=bar -Ddb=host:3306/database

