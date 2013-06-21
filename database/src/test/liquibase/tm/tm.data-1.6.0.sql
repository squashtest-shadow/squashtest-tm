INSERT INTO `CORE_PARTY` (`PARTY_ID`) VALUES
(10),
(11),
(12),
(20),
(21);

INSERT INTO `CORE_TEAM` (`PARTY_ID`, `NAME`, `DESCRIPTION`, `CREATED_BY`, `CREATED_ON`, `LAST_MODIFIED_BY`, `LAST_MODIFIED_ON`) VALUES
(10, 'team1', 'this is team1', 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00'),
(11, 'team2', 'this is team2', 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00'),
(12, 'team3', 'this is team3', 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00');

INSERT INTO `CORE_USER` (`PARTY_ID`, `LOGIN`, `FIRST_NAME`, `LAST_NAME`, `EMAIL`, `ACTIVE`, `CREATED_BY`, `CREATED_ON`, `LAST_MODIFIED_BY`, `LAST_MODIFIED_ON`) VALUES
(20, 'user20', 'this is user20', 'this is user20', 'u@u', 0, 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00'),
(21, 'user21', 'this is user21', 'this is user21', 'u@u', 0, 'guest_tpl', '2011-06-21 08:40:32', 'guest_tpl', '2011-06-21 08:41:00');

INSERT INTO `CORE_TEAM_MEMBER` (`TEAM_ID`, `USER_ID`) VALUES
(10, 20),
(10, 21),
(11, 21);

INSERT INTO `CORE_GROUP_MEMBER` (`PARTY_ID`, `GROUP_ID`) VALUES
(20,3),
(21,2);

INSERT INTO `ACL_RESPONSIBILITY_SCOPE_ENTRY` (`ID`, `PARTY_ID`, `ACL_GROUP_ID`, `OBJECT_IDENTITY_ID`) VALUES
(60,20,2,14),
(61,21,2,14);