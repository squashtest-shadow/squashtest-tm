insert into `CUSTOM_FIELD` (`CF_ID`, `FIELD_TYPE`, `NAME`, `LABEL`, `OPTIONAL`, `DEFAULT_VALUE`, `INPUT_TYPE`, `CODE`) values
(1, 'CF', 'cuf-text1', 'cuf-Text1', 0, 'defaultt1', 'PLAIN_TEXT', 't1'),
(2, 'CF', 'cuf-text2', 'cuf-text2', 1, '', 'PLAIN_TEXT', 't2'),
(3, 'SSF', 'cuf-deroulante1', 'cuf-deroulante1', 0, 'opt11', 'DROPDOWN_LIST', 'd1'),
(4, 'SSF', 'cuf-deroulante2', 'cuf-deroulante2', 1, '', 'DROPDOWN_LIST', 'd2'),
(5, 'CF', 'cuf-check', 'cuf-check', 1, 'false', 'CHECKBOX', 'cuf_check');


insert into `CUSTOM_FIELD_BINDING` (`CFB_ID`, `CF_ID`, `BOUND_ENTITY`, `BOUND_PROJECT_ID`, `POSITION`) values
(1, 1, 'REQUIREMENT_VERSION', 4, 1),
(2, 2, 'REQUIREMENT_VERSION', 4, 2),
(3, 3, 'REQUIREMENT_VERSION', 4, 3),
(4, 4, 'REQUIREMENT_VERSION', 4, 4),
(5, 5, 'REQUIREMENT_VERSION', 4, 5);


insert into `CUSTOM_FIELD_OPTION` (`CF_ID`, `LABEL`, `POSITION`, `CODE`) values
(3, 'opt11', 0, 'opt11'),
(3, 'opt12', 1, 'opt12'),
(4, 'opt21', 0, 'opt21'),
(4, 'opt22', 1, 'opt22');


insert into `CUSTOM_FIELD_VALUE` (`CFV_ID`, `BOUND_ENTITY_ID`, `BOUND_ENTITY_TYPE`, `CFB_ID`, `VALUE`) values
(1, 176, 'REQUIREMENT_VERSION', 1, ''),
(2, 177, 'REQUIREMENT_VERSION', 1, 'defaultt1'),
(3, 178, 'REQUIREMENT_VERSION', 1, 'defaultt1'),
(4, 179, 'REQUIREMENT_VERSION', 1, 'magical'),
(5, 176, 'REQUIREMENT_VERSION', 2, ''),
(6, 177, 'REQUIREMENT_VERSION', 2, ''),
(7, 178, 'REQUIREMENT_VERSION', 2, ''),
(8, 179, 'REQUIREMENT_VERSION', 2, 'exception'),
(9, 176, 'REQUIREMENT_VERSION', 3, 'opt12'),
(10, 177, 'REQUIREMENT_VERSION', 3, 'opt11'),
(11, 178, 'REQUIREMENT_VERSION', 3, 'opt11'),
(12, 179, 'REQUIREMENT_VERSION', 3, 'opt11'),
(13, 176, 'REQUIREMENT_VERSION', 4, 'opt21'),
(14, 177, 'REQUIREMENT_VERSION', 4, ''),
(15, 178, 'REQUIREMENT_VERSION', 4, ''),
(16, 179, 'REQUIREMENT_VERSION', 4, ''),
(17, 176, 'REQUIREMENT_VERSION', 5, 'true'),
(18, 177, 'REQUIREMENT_VERSION', 5, 'false'),
(19, 178, 'REQUIREMENT_VERSION', 5, 'false'),
(20, 179, 'REQUIREMENT_VERSION', 5, 'false');
