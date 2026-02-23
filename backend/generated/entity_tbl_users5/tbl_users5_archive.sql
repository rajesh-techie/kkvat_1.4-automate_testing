-- Archive table for tbl_users5
CREATE TABLE IF NOT EXISTS `tbl_users5_archive` (
  username VARCHAR(10),
  userlogin VARCHAR(10) PRIMARY KEY,
  isactive DOUBLE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
CREATE INDEX idx_tbl_users5_username ON tbl_users5_archive(username);
CREATE INDEX idx_tbl_users5_userlogin ON tbl_users5_archive(userlogin);
CREATE INDEX idx_tbl_users5_isactive ON tbl_users5_archive(isactive);
