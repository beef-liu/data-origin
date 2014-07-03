CREATE TABLE `DOAdmin` (
  `admin_id` char(32) NOT NULL COMMENT 'Admin ID',
  `priviledge_roles` varchar(4000) DEFAULT NULL COMMENT 'roles(tableA:rw|tableB:rw|...|tableN:rw)',
  `name` varchar(45) DEFAULT NULL COMMENT 'name',
  `update_time` bigint(20) DEFAULT NULL COMMENT 'update time(UTC)',
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Admin';

