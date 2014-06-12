
-- 
-- This file stacks up with tm.data-1.7.0-add-automated-tests.sql. and also tm.data-1.7.1-orphan-entities.sql
-- 
-- Here we do the following 
-- 1) add more test_automation_server etc, that aren't referenced by a project, so we can test the cleanup 
-- 2) add more test automation stuffs referenced by the TM project #1 (nodes 148 and 149) and check that the data
--	migrated correctly
-- 3) also bind TM project 2 to TA projects 1 and 3
--

-- 
-- ACTION 1  
-- build a bunch of TA stuffs that aren't called by a project, that will be cleaned up in the migration. 
-- 
-- 

INSERT INTO TEST_AUTOMATION_SERVER (SERVER_ID, BASE_URL, LOGIN, PASSWORD, KIND) VALUES
(2, 'SOMEURL', 'notcalled', 'notcalled', 'jenkins');




INSERT INTO TEST_AUTOMATION_PROJECT (PROJECT_ID, NAME, SERVER_ID) VALUES
(2, 'blabla', 2);


INSERT INTO AUTOMATED_TEST (TEST_ID, NAME, PROJECT_ID) VALUES
(4, 'notcalledtest_1', 2);

UPDATE TEST_CASE
SET TA_TEST = 4
WHERE TCLN_ID = 220;


INSERT INTO AUTOMATED_SUITE (SUITE_ID) VALUES
(4);

INSERT INTO AUTOMATED_EXECUTION_EXTENDER (EXTENDER_ID, MASTER_EXECUTION_ID, TEST_ID, RESULT_URL, SUITE_ID, RESULT_SUMMARY) VALUES
(4, 47, 4, 'http://192.168.2.11:9080/jenkins/results', 4, 'summary');


-- 
-- ACTION 2  
-- add some tests and stuffs to nodes 148 and 149, that belong to TM project 1
--
-- Note  
-- TM project 1 is already bound to a TA project (#1) that itselfs belongs to TA server (#1). We now bind it 
-- to TA project #3 that depends on TA server #3 (to be created) 
--  
-- 


INSERT INTO TEST_AUTOMATION_SERVER (SERVER_ID, BASE_URL, LOGIN, PASSWORD, KIND) VALUES
(3, 'http://192.168.2.115:9080/jenkins', 'yeah', 'yeah', 'jenkins');

INSERT INTO TEST_AUTOMATION_PROJECT (PROJECT_ID, NAME, SERVER_ID) VALUES
(3, 'new auto project', 3);

INSERT INTO TM_TA_PROJECTS (TM_PROJECT_ID, TA_PROJECT_ID) VALUES
(1, 3);

INSERT INTO AUTOMATED_TEST (TEST_ID, NAME, PROJECT_ID) VALUES
(5, 'new test 1', 3),
(6, 'new test 2', 3);

UPDATE TEST_CASE
SET TA_TEST = 5
WHERE TCLN_ID = 148;

UPDATE TEST_CASE
SET TA_TEST = 6
WHERE TCLN_ID = 149;


INSERT INTO AUTOMATED_SUITE (SUITE_ID) VALUES
(5);

INSERT INTO AUTOMATED_EXECUTION_EXTENDER (EXTENDER_ID, MASTER_EXECUTION_ID, TEST_ID, RESULT_URL, SUITE_ID, RESULT_SUMMARY) VALUES
(5, 26, 5, 'http://192.168.2.115:9080/jenkins/result', 5, 'summary'),
(6, 45, 6, 'http://192.168.2.115:9080/jenkins/result', 5, 'summary');

-- 
-- ACTION 3  
-- 
-- bind TM project 2 to TA projects 1 and 3
--  
-- 

INSERT INTO TM_TA_PROJECTS (TM_PROJECT_ID, TA_PROJECT_ID) VALUES
(2, 3),
(2, 1);