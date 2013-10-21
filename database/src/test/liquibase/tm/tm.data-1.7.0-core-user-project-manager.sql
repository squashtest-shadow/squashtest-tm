/*
Create a user then add it to the core_group project manager
*/


insert into CORE_PARTY() values();

insert into CORE_USER(PARTY_ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_BY, LAST_MODIFIED_ON)
values ((select max(PARTY_ID) from CORE_PARTY), 'Bob', 'Bob', 'Bobovitch', 'bob@bob.com', 1, 'admin', '2013-10-21', NULL, NULL);

insert into CORE_GROUP_MEMBER(PARTY_ID, GROUP_ID) values((select max(PARTY_ID) from CORE_PARTY), 3);