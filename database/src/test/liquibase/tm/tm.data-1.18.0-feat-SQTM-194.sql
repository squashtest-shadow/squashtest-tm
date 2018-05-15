
-- Feature 7183 when ordered lists have duplicate indices for order column

-- create test cases

INSERT INTO TEST_CASE_LIBRARY_NODE (TCLN_ID, NAME, CREATED_BY, CREATED_ON, PROJECT_ID, ATTACHMENT_LIST_ID) VALUES
(1, 'tc-test', 'admin', '2018-03-18', -25, -907),
(2, 'tc-test-2', 'admin', '2018-03-18', -25, -907),
(3, 'tc-test-3', 'admin', '2018-03-18', -25, -907),
(4, 'tc-test-4', 'admin', '2018-03-18', -25, -907);

INSERT INTO TEST_CASE (TCLN_ID, VERSION, TC_NATURE, TC_TYPE, PREREQUISITE) VALUES
(1, 1, -12, -20, ''),
(2, 1, -12, -20, ''),
(3, 1, -12, -20, ''),
(4, 1, -12, -20, '');

INSERT INTO DATASET(DATASET_ID, TEST_CASE_ID, NAME) VALUES
(1,1,'TC1-DS1'),
(2,1,'TC1-DS2'),
(3,1,'TC1-DS3'),
(4,2,'TC2-DS1'),
(5,4,'TC4-DS1'),
(6,4,'TC4-DS2'),
(8,3,'TC3-DS1'),
(12,4,'TC4-DS3'),
(13,2,'TC2-DS2'),
(18,2,'TC2-DS3'),
(25,1,'TC1-DS4');

INSERT INTO PARAMETER(PARAM_ID, TEST_CASE_ID, NAME, DESCRIPTION) VALUES
(1,1,'TC1-PARAM1',''),
(2,1,'TC1-PARAM2',''),
(3,1,'TC1-PARAM3',''),
(6,2,'TC2-PARAM1',''),
(9,3,'TC3-PARAM1',''),
(15,3,'TC3-PARAM2',''),
(17,3,'TC3-PARAM3',''),
(22,1,'TC1-PARAM4','');

