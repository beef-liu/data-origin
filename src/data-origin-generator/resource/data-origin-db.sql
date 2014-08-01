CREATE TABLE `DOAdmin` (
  `admin_id` char(32) NOT NULL COMMENT 'Admin ID',
  `priviledge_roles` varchar(6000) DEFAULT NULL COMMENT 'roles(tableA:rw|tableB:rw|...|tableN:rw)',
  `name` varchar(45) DEFAULT NULL COMMENT 'name',
  `password` varchar(45) DEFAULT NULL COMMENT 'md5 of password',
  `update_time` bigint(20) DEFAULT NULL COMMENT 'update time(UTC)',
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Admin';

CREATE  TABLE `DOUploadFileMeta` (
  `file_id` CHAR(32) NOT NULL  COMMENT 'File ID',
  `content_type` VARCHAR(255) NULL COMMENT 'Content Type',
  `content_length` BIGINT NULL DEFAULT 0 COMMENT 'Content Length',
  `file_ext` CHAR(10) NULL COMMENT 'file extension name',
  `download_url` VARCHAR(255) NULL COMMENT 'Download Url',
  `thumbnail_download_url` VARCHAR(255) NULL COMMENT 'Download Url for thumbnail',
  `content_hash_code` VARCHAR(64) NULL COMMENT 'Content Hash Code',
  `file_tag` VARCHAR(255) NULL COMMENT 'file tag (for searching through keyword)',
  `update_time` BIGINT NULL DEFAULT 0 COMMENT 'update time(UTC)',
  PRIMARY KEY (`file_id`),
  KEY `SEARCH_IDX` (`file_tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Upload File';

CREATE TABLE `DODataModificationCommitTask` (
  `task_id` char(32) NOT NULL,
  `table_name` char(64) NOT NULL,
  `schedule_commit_time` bigint(20) NOT NULL COMMENT 'time(utc) to commit data into production DB',
  `sql_primary_key` varchar(45) NOT NULL COMMENT 'SQL condition clause of primary key (e.g, k1 = ''a'' and k2 = ''b'')',
  `commit_time` bigint(20) DEFAULT '0',
  `retried_count` int(11) DEFAULT '0',
  `max_retry` int(11) DEFAULT '0',
  `commit_status` int(11) NOT NULL DEFAULT '0' COMMENT '0:wait to commit 1:success -1:fail',
  `error_msg` varchar(6000) DEFAULT NULL COMMENT 'error msg at last committing',
  `update_time` bigint(20) DEFAULT NULL COMMENT 'task updated time',
  `update_admin` char(32) DEFAULT NULL COMMENT 'admin',
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `uniq_idx` (`table_name`,`schedule_commit_time`,`sql_primary_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Data Modification Commit Task';


