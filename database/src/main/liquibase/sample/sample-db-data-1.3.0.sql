Insert into core_user(ID, LOGIN, FIRST_NAME, LAST_NAME, EMAIL, ACTIVE, CREATED_BY, CREATED_ON, LAST_MODIFIED_ON, LAST_MODIFIED_BY) values 
 (33, 'taserver', 'TA', 'Server', 'no@where', true, 'admin', '2011-09-30 10:26:23.0', null, null);
Insert into auth_user(LOGIN, PASSWORD, ACTIVE) values 
 ('taserver', 'eb0fd24715f33bc94b1754263d1d431422aba649', true); 
Insert into core_group_member(USER_ID, GROUP_ID) values 
 (33, 4);
Update test_case
set PREREQUISITE='';