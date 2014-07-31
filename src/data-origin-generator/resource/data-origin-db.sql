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

