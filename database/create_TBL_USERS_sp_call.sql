-- Call the generic stored procedure for TBL_USERS archival based on payload
-- payload: howManyMonthsMainTable: 6, howManyMonthsArchiveTable: 5 (archive retention extra months)

-- Use archiveMonths = payload.howManyMonthsArchiveTable (5)
-- The stored proc expects main_months and archive_months (we will pass 6 and 2) if you want delete after 6+2 months
-- Here we call with p_main_months = 6 and p_archive_months = 2 (example: keep archive for 2 months after main retention)

CALL sp_move_to_archive_and_cleanup('TBL_USERS','TBL_USERS_ARCHIVE','isActive','0',6,2);
