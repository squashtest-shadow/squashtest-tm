Squash TM database schema management
====================================

This module contains liquibase changesets which define database schema and upgrade.
**Changesets of released versions SHOULD NEVER BE MODIFIED** ! It would break debian / redhat packages incremental upgrade.

When you need to create a DB upgrade for a new version, use the groovy script : 
    cd etc
    groovy createNewChangelogs.goovy -DnewVersion="1.2.3"

It should create a bunch of new changelog files and tell you which other files you need to manually modify.