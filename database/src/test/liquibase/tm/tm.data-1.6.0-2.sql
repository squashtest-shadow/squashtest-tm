-- let's botch some test suite test plans that were created in 1.4.1.sql then migrated to 1.5.x schema
-- for suite 1, tpi 9 and 11 will be shifted by two on the right
-- for suite 2, tpi 3, 5 and 6 will be shifted by one and 7, 10 by two
-- for suite 5, tpi 20 will be shifted by one.
-- this is done by incrementing one by one the concerned items.

update TEST_SUITE_TEST_PLAN_ITEM 
set test_plan_order = test_plan_order + 1
where tpi_id in (9, 11, 3, 5, 6, 7, 10, 20);

update TEST_SUITE_TEST_PLAN_ITEM
set test_plan_order = test_plan_order + 1
where tpi_id in (9, 11, 7, 10);